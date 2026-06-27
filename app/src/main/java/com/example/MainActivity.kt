package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.IPTVViewModel
import com.example.ui.screens.AddAccountScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SubscriptionScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) { // Always dark space cyber theme
                val viewModel: IPTVViewModel = viewModel()
                val showPaywall by viewModel.showSubscriptionPaywall.collectAsState()
                var currentScreen by remember { mutableStateOf("DASHBOARD") }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        when (currentScreen) {
                            "DASHBOARD" -> {
                                DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToAddAccount = { currentScreen = "ADD_ACCOUNT" },
                                    onShowPaywall = { viewModel.triggerPaywall() }
                                )
                            }
                            "ADD_ACCOUNT" -> {
                                AddAccountScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = "DASHBOARD" },
                                    onSuccess = { currentScreen = "DASHBOARD" }
                                )
                            }
                        }

                        // Premium subscription overlay paywall
                        if (showPaywall) {
                            SubscriptionScreen(
                                viewModel = viewModel,
                                onDismiss = { viewModel.dismissPaywall() }
                            )
                        }
                    }
                }
            }
        }
    }
}
