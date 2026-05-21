package com.buylan.cryst

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.buylan.cryst.model.AppViewModel

class Application : Application() {
    lateinit var appViewModel: AppViewModel
        private set

    override fun onCreate() {
        super.onCreate()
        appViewModel = AppViewModel(this)
    }
}