package com.example.jazzdashboard.car
import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

class JazzCarSession : Session(), SavedStateRegistryOwner, ViewModelStoreOwner {

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val mViewModelStore = ViewModelStore()

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                savedStateRegistryController.performRestore(null)
            }
        })
    }

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore
        get() = mViewModelStore

    override fun onCreateScreen(intent: Intent): Screen {
        return JazzCarScreen(carContext, this)
    }
}



