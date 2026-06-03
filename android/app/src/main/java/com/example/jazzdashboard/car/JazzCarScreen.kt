package com.example.jazzdashboard.car
import com.example.jazzdashboard.DashboardViewModel
import android.app.Presentation
import android.content.Context
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.os.Bundle
import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.jazzdashboard.ui.DashboardApp
import com.example.jazzdashboard.ui.theme.JazzDashboardTheme

class JazzCarScreen(
    carContext: CarContext,
    private val session: JazzCarSession
) : Screen(carContext), SurfaceCallback {

    private var virtualDisplay: VirtualDisplay? = null
    private var presentation: Presentation? = null

    init {
        // Register surface callback to draw on the car screen
        carContext.getCarService(AppManager::class.java).setSurfaceCallback(this)
    }

    override fun onGetTemplate(): Template {
        // NavigationTemplate provides a full-screen map surface which we intercept
        return NavigationTemplate.Builder()
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(Action.APP_ICON)
                    .build()
            )
            .build()
    }

    override fun onSurfaceAvailable(surfaceContainer: SurfaceContainer) {
        val surface = surfaceContainer.surface ?: return
        val displayManager = carContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        
        virtualDisplay = displayManager.createVirtualDisplay(
            "JazzCarComposeDisplay",
            surfaceContainer.width,
            surfaceContainer.height,
            surfaceContainer.dpi,
            surface,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
        )

        val display = virtualDisplay?.display ?: return

        presentation = object : Presentation(carContext, display) {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                val composeView = ComposeView(context).apply {
                    // Inject the necessary owners for Compose to run outside an Activity
                    setViewTreeLifecycleOwner(session)
                    setViewTreeViewModelStoreOwner(session)
                    setViewTreeSavedStateRegistryOwner(session)

                    setContent {
                        JazzDashboardTheme {
                            val viewModel: com.example.jazzdashboard.DashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                            DashboardApp(viewModel)
                        }
                    }
                }
                setContentView(composeView)
            }
        }.apply {
            show()
        }
    }

    override fun onSurfaceDestroyed(surfaceContainer: SurfaceContainer) {
        presentation?.dismiss()
        presentation = null
        virtualDisplay?.release()
        virtualDisplay = null
    }
}



