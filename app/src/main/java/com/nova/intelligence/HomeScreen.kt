package com.nova.intelligence

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DeveloperBoard
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun HomeScreen(specs: DeviceSpecs?, live: LiveStats, modifier: Modifier = Modifier) {
    if (specs == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NovaColors.Purple)
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BrandCard(specs)
        OverviewCard(specs, live)
        StatusCard(specs, live)
        ExploreCard(specs, live)
    }
}

@Composable
private fun BrandCard(specs: DeviceSpecs) {
    NovaCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(NovaColors.PurpleDark),
                contentAlignment = Alignment.Center,
            ) {
                Text("N", color = NovaColors.Purple, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("NOVA", color = NovaColors.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("INTELLIGENCE", color = NovaColors.Purple, fontSize = 11.sp, letterSpacing = 0.5.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NovaColors.Green)
                )
                Spacer(Modifier.width(6.dp))
                Text("DETECTADO", color = NovaColors.Green, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 16.dp), color = NovaColors.Outline)
        Text("TU DISPOSITIVO", color = NovaColors.TextSecondary, fontSize = 12.sp, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(6.dp))
        Text(specs.title, color = NovaColors.TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text("${specs.manufacturer} · ${specs.model}", color = NovaColors.TextSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill("ANDROID ${specs.androidVersion}", NovaColors.GreenDark, NovaColors.Green)
            Pill("API ${specs.apiLevel}", NovaColors.PurpleDark, NovaColors.Purple)
        }
    }
}

@Composable
private fun OverviewCard(specs: DeviceSpecs, live: LiveStats) {
    val batteryMah = specs.batteryCapacityMah
    NovaCard {
        SectionTitle("RESUMEN DEL DISPOSITIVO")
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatTile(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                icon = Icons.Outlined.Memory,
                tint = NovaColors.Purple,
                label = "PROCESADOR",
                value = specs.processor,
                caption = "${specs.cores} núcleos",
            )
            StatTile(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                icon = Icons.Outlined.DeveloperBoard,
                tint = NovaColors.Cyan,
                label = "RAM",
                value = formatCapacity(specs.ramNominalGb),
                caption = "Memoria física",
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatTile(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                icon = Icons.Outlined.Storage,
                tint = NovaColors.Purple,
                label = "ALMACENAMIENTO",
                value = formatCapacity(specs.storageNominalGb),
                caption = "Capacidad total",
            )
            StatTile(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                icon = Icons.Outlined.BatteryFull,
                tint = NovaColors.Green,
                label = "BATERÍA",
                value = if (batteryMah != null) batteryMah.toString() else "${live.batteryPercent}%",
                caption = if (batteryMah != null) "mAh nominal" else "Nivel actual",
            )
        }
    }
}

@Composable
private fun StatusCard(specs: DeviceSpecs, live: LiveStats) {
    NovaCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SectionTitle("ESTADO DEL SISTEMA")
            Pill("EN VIVO", NovaColors.GreenDark, NovaColors.Green)
        }
        Spacer(Modifier.height(4.dp))
        InfoRow("Sistema operativo", "Android ${specs.androidVersion}")
        InfoRow("Arquitectura", if (specs.is64Bit) "64 bits" else "32 bits")
        InfoRow("Pantalla", "${specs.refreshRateHz} Hz")
        InfoRow("Batería", "${live.batteryPercent}% · ${live.batteryStatus}")
        InfoRow(
            "RAM libre",
            "${gb(live.ramAvailableBytes, false)} de ${gb(live.ramTotalBytes, false)} GB",
        )
        InfoRow(
            "Espacio libre",
            "${gb(live.storageFreeBytes, true)} de ${gb(live.storageTotalBytes, true)} GB",
            showDivider = false,
        )
    }
}

@Composable
private fun ExploreCard(specs: DeviceSpecs, live: LiveStats) {
    val arch = if (specs.is64Bit) "64 bits" else "32 bits"
    val ramUsedPercent = if (live.ramTotalBytes > 0) {
        ((live.ramTotalBytes - live.ramAvailableBytes) * 100 / live.ramTotalBytes).toInt()
    } else {
        0
    }

    NovaCard {
        SectionTitle("EXPLORA TU DISPOSITIVO")
        Spacer(Modifier.height(4.dp))
        ExploreRow(
            icon = Icons.Outlined.Memory,
            tint = NovaColors.Purple,
            title = "Detalles del procesador",
            details = listOf(
                "Procesador" to specs.processor,
                "Núcleos" to specs.cores.toString(),
                "Arquitectura" to arch,
            ),
        )
        ExploreRow(
            icon = Icons.Outlined.Storage,
            tint = NovaColors.Cyan,
            title = "Análisis de memoria",
            details = listOf(
                "RAM total" to "${gb(live.ramTotalBytes, false)} GB",
                "RAM libre" to "${gb(live.ramAvailableBytes, false)} GB",
                "RAM en uso" to "$ramUsedPercent %",
                "Almacenamiento" to "${gb(live.storageTotalBytes, true)} GB",
                "Espacio libre" to "${gb(live.storageFreeBytes, true)} GB",
            ),
        )
        ExploreRow(
            icon = Icons.Outlined.PhoneAndroid,
            tint = NovaColors.Purple,
            title = "Información de pantalla",
            details = listOf(
                "Resolución" to specs.resolution,
                "Densidad" to "${specs.densityDpi} dpi",
                "Frecuencia" to "${specs.refreshRateHz} Hz",
            ),
            showDivider = false,
        )
    }
}

// ---------- Piezas reutilizables ----------

@Composable
private fun NovaCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = NovaColors.Surface,
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = NovaColors.TextPrimary,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
    )
}

@Composable
private fun Pill(text: String, container: Color, content: Color) {
    Surface(shape = RoundedCornerShape(50), color = container) {
        Text(
            text = text,
            color = content,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun StatTile(
    modifier: Modifier,
    icon: ImageVector,
    tint: Color,
    label: String,
    value: String,
    caption: String,
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(20.dp), color = NovaColors.Tile) {
        Column(Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(10.dp))
            Text(label, color = NovaColors.TextSecondary, fontSize = 11.sp)
            Text(
                text = value,
                color = NovaColors.TextPrimary,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(caption, color = NovaColors.TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, showDivider: Boolean = true) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, color = NovaColors.TextSecondary, fontSize = 15.sp, modifier = Modifier.weight(1f))
            Text(
                text = value,
                color = NovaColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
        if (showDivider) HorizontalDivider(color = NovaColors.Outline)
    }
}

@Composable
private fun ExploreRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    details: List<Pair<String, String>>,
    showDivider: Boolean = true,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 90f else 0f, label = "chevron")

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Text(title, color = NovaColors.TextPrimary, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = NovaColors.TextSecondary,
                modifier = Modifier.rotate(rotation),
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(Modifier.padding(start = 34.dp)) {
                details.forEach { (label, value) ->
                    InfoRow(label, value, showDivider = false)
                }
            }
        }
        if (showDivider) HorizontalDivider(color = NovaColors.Outline)
    }
}

// ---------- Formato ----------

private fun formatCapacity(gb: Int): String =
    if (gb >= 1024) "${gb / 1024} TB" else "$gb GB"

/** decimal = true para almacenamiento (1 GB = 1000 MB); false para RAM (1 GB = 1024 MB). */
private fun gb(bytes: Long, decimal: Boolean): String {
    val divisor = if (decimal) 1_000_000_000.0 else 1_073_741_824.0
    return String.format(Locale.getDefault(), "%.1f", bytes / divisor)
}
