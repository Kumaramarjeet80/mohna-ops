package com.mohna.ops

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.mohna.ops.data.repository.MohnaRepository
import com.mohna.ops.ui.screens.AdminDashboardScreen
import com.mohna.ops.ui.screens.AuthScreen
import com.mohna.ops.ui.screens.RiderHomeScreen
import com.mohna.ops.ui.theme.OpsTheme

class MainActivity : ComponentActivity() {

    private val repository = MohnaRepository.instance

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Permissions handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestAppPermissions()

        setContent {
            val currentRole by repository.currentRole.collectAsState()
            val isAdminMode by repository.isAdminMode.collectAsState()
            var isLoggedIn by remember { mutableStateOf(false) }

            OpsTheme(isAdminMode = isAdminMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (!isLoggedIn) {
                        AuthScreen(
                            onLoginSuccess = { role ->
                                isLoggedIn = true
                            }
                        )
                    } else {
                        if (isAdminMode) {
                            AdminDashboardScreen(
                                onSwitchToRider = {
                                    repository.setMode(false)
                                },
                                onLogout = {
                                    isLoggedIn = false
                                    repository.setCurrentRider(null)
                                }
                            )
                        } else {
                            RiderHomeScreen(
                                onSwitchToAdmin = {
                                    repository.setMode(true)
                                },
                                onLogout = {
                                    isLoggedIn = false
                                    repository.setCurrentRider(null)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun requestAppPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CALL_PHONE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        }
    }
}
