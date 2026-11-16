package com.nova.intelligence

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.Process
import android.os.StatFs
import android.view.Display
import java.io.File
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Datos que no cambian mientras la app está abierta. */
data class DeviceSpecs(
    val title: String,
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val apiLevel: Int,
    val securityPatch: String,
    val processor: String,
    val cores: Int,
    val is64Bit: Boolean,
    val ramNominalGb: Int,
    val storageNominalGb: Int,
    val batteryCapacityMah: Int?,
    val refreshRateHz: Int,
    val resolution: String,
    val densityDpi: Int,
)

/** Datos que cambian y se actualizan en vivo. */
data class LiveStats(
    val ramTotalBytes: Long = 0L,
    val ramAvailableBytes: Long = 0L,
    val storageTotalBytes: Long = 0L,
    val storageFreeBytes: Long = 0L,
    val batteryPercent: Int = 0,
    val batteryTempC: Float = 0f,
    val batteryVoltageV: Float = 0f,
    val batteryStatus: String = "Desconocido",
    val batteryHealth: String = "Desconocida",
)

object DeviceReader {

    private val ramSizesGb = intArrayOf(1, 2, 3, 4, 6, 8, 12, 16, 18, 24, 32)
    private val storageSizesGb = intArrayOf(8, 16, 32, 64, 128, 256, 512, 1024, 2048)

    // Nombres comerciales conocidos. Se ampliará en próximas versiones.
    private val marketingNames = mapOf(
        "SM-A045M" to "Galaxy A04",
        "SM-A045F" to "Galaxy A04",
    )

    fun readSpecs(context: Context): DeviceSpecs {
        val app = context.applicationContext
        val memory = readMemory(app)
        val display = app.getSystemService(DisplayManager::class.java)
            ?.getDisplay(Display.DEFAULT_DISPLAY)
        val mode = display?.mode
        val storageGb = StatFs(Environment.getDataDirectory().path).totalBytes / 1_000_000_000.0

        val resolution = if (mode != null) {
            val shortSide = min(mode.physicalWidth, mode.physicalHeight)
            val longSide = max(mode.physicalWidth, mode.physicalHeight)
            "$shortSide × $longSide"
        } else {
            "Desconocida"
        }

        return DeviceSpecs(
            title = marketingNames[Build.MODEL] ?: Build.MODEL,
            manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            securityPatch = Build.VERSION.SECURITY_PATCH.ifBlank { "Desconocido" },
            processor = readProcessor(),
            cores = Runtime.getRuntime().availableProcessors(),
            is64Bit = Process.is64Bit(),
            ramNominalGb = nominalSize(memory.totalMem / BYTES_PER_GIB, ramSizesGb),
            storageNominalGb = nominalSize(storageGb, storageSizesGb),
            batteryCapacityMah = readBatteryCapacityMah(app),
            refreshRateHz = display?.refreshRate?.roundToInt() ?: 60,
            resolution = resolution,
            densityDpi = app.resources.displayMetrics.densityDpi,
        )
    }

    fun readLive(context: Context): LiveStats {
        val app = context.applicationContext
        val memory = readMemory(app)
        val storage = StatFs(Environment.getDataDirectory().path)
        val battery = app.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percent = if (level >= 0 && scale > 0) (level * 100f / scale).roundToInt() else 0

        return LiveStats(
            ramTotalBytes = memory.totalMem,
            ramAvailableBytes = memory.availMem,
            storageTotalBytes = storage.totalBytes,
            storageFreeBytes = storage.availableBytes,
            batteryPercent = percent,
            batteryTempC = (battery?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f,
            batteryVoltageV = (battery?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0) / 1000f,
            batteryStatus = statusLabel(battery?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1),
            batteryHealth = healthLabel(battery?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1),
        )
    }

    private const val BYTES_PER_GIB = 1024.0 * 1024.0 * 1024.0

    private fun readMemory(context: Context): ActivityManager.MemoryInfo {
        val info = ActivityManager.MemoryInfo()
        context.getSystemService(ActivityManager::class.java)?.getMemoryInfo(info)
        return info
    }

    /** Android informa algo menos que lo anunciado (ej. 3.6 GB en un teléfono de 4 GB). */
    private fun nominalSize(value: Double, sizes: IntArray): Int =
        sizes.firstOrNull { it >= value } ?: ceil(value).toInt()

    private fun String?.usable(): String? =
        this?.trim()?.takeIf { it.isNotEmpty() && !it.equals(Build.UNKNOWN, ignoreCase = true) }

    private fun readProcessor(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val model = Build.SOC_MODEL.usable()
            if (model != null) {
                val maker = Build.SOC_MANUFACTURER.usable()
                return if (maker != null && !model.startsWith(maker, ignoreCase = true)) {
                    "$maker $model"
                } else {
                    model
                }
            }
        }
        return readCpuInfoHardware() ?: Build.HARDWARE.usable() ?: "Desconocido"
    }

    private fun readCpuInfoHardware(): String? = try {
        File("/proc/cpuinfo").useLines { lines ->
            lines.firstOrNull { it.startsWith("Hardware", ignoreCase = true) }
                ?.substringAfter(":")
                .usable()
        }
    } catch (e: Exception) {
        null
    }

    /** Android no expone la capacidad oficialmente; se intenta leer y, si no se puede, devuelve null. */
    private fun readBatteryCapacityMah(context: Context): Int? = try {
        val profileClass = Class.forName("com.android.internal.os.PowerProfile")
        val profile = profileClass.getConstructor(Context::class.java).newInstance(context)
        val capacity = profileClass.getMethod("getBatteryCapacity").invoke(profile) as? Number
        capacity?.toDouble()?.roundToInt()?.takeIf { it > 0 }
    } catch (e: Throwable) {
        null
    }

    private fun statusLabel(code: Int): String = when (code) {
        BatteryManager.BATTERY_STATUS_CHARGING -> "Cargando"
        BatteryManager.BATTERY_STATUS_FULL -> "Completa"
        BatteryManager.BATTERY_STATUS_DISCHARGING,
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Sin cargar"
        else -> "Desconocido"
    }

    private fun healthLabel(code: Int): String = when (code) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "Buena"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Sobrecalentada"
        BatteryManager.BATTERY_HEALTH_DEAD -> "Dañada"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Sobrevoltaje"
        BatteryManager.BATTERY_HEALTH_COLD -> "Fría"
        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Con fallo"
        else -> "Desconocida"
    }
}
