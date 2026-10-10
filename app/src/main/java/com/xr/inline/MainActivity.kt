package com.xr.inline

import android.os.Bundle
import android.net.Uri
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.xr.inline.ui.NewsScreen
import com.xr.inline.ui.PrivateSpaceScreen
import com.xr.inline.ui.theme.InLineTheme

class MainActivity : ComponentActivity() {
    private var isPrivateSpaceVisible by mutableStateOf(false)
    private var isLaunchingSecureLoginTab = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        setContent {
            InLineTheme {
                BackHandler(enabled = isPrivateSpaceVisible) {
                    closePrivateSpace()
                }

                if (isPrivateSpaceVisible) {
                    PrivateSpaceScreen(
                        onExit = ::closePrivateSpace,
                        onOpenLogin = ::openSecureLoginTab
                    )
                } else {
                    NewsScreen(onPrivateSpaceRequested = ::openPrivateSpace)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isLaunchingSecureLoginTab = false
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations && isPrivateSpaceVisible && !isLaunchingSecureLoginTab) {
            closePrivateSpace()
            finishAndRemoveTask()
        }
    }

    private fun openPrivateSpace() {
        isPrivateSpaceVisible = true
    }

    private fun closePrivateSpace() {
        isPrivateSpaceVisible = false
    }

    private fun openSecureLoginTab(url: String) {
        isLaunchingSecureLoginTab = true
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
            .build()
            .launchUrl(this, Uri.parse(url))
    }
}
