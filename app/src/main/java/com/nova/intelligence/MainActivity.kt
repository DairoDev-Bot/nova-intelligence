package com.nova.intelligence

import android.app.Application
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )
        setContent {
            NovaTheme {
                NovaApp()
            }
        }
    }
}

private enum class NovaTab(val label: String, val icon: ImageVector) {
    Home("INICIO", Icons.Outlined.Home),
    Hardware("HARDWARE", Icons.Outlined.Memory),
    Analysis("ANÁLISIS", Icons.Outlined.BarChart),
    Settings("AJUSTES", Icons.Outlined.Settings),
}

@Composable
private fun NovaApp() {
    val viewModel: DeviceViewModel = viewModel(
        factory = viewModelFactory {
            initializer { DeviceViewModel(this[APPLICATION_KEY] as Application) }
        }
    )
    val specs by viewModel.specs.collectAsStateWithLifecycle()
    val live by viewModel.live.collectAsStateWithLifecycle()
    var selectedIndex by rememberSaveable { mutableStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = NovaColors.Background,
        bottomBar = {
            NavigationBar(containerColor = NovaColors.Surface) {
                NovaTab.entries.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NovaColors.Purple,
                            selectedTextColor = NovaColors.Purple,
                            unselectedIconColor = NovaColors.TextSecondary,
                            unselectedTextColor = NovaColors.TextSecondary,
                            indicatorColor = Color.Transparent,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val tab = NovaTab.entries[selectedIndex]
            if (tab == NovaTab.Home) {
                HomeScreen(specs = specs, live = live)
            } else {
                ComingSoon(tab.label)
            }
        }
    }
}

@Composable
private fun ComingSoon(title: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "$title\nPróximamente",
            color = NovaColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}
