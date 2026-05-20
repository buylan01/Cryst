package com.buylan.cryst

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

class Application : Application(), ViewModelStoreOwner {
    private val appViewModelStore by lazy { ViewModelStore() }

    override val viewModelStore: ViewModelStore
        get() = appViewModelStore

    override fun onTerminate() {
        super.onTerminate()
        appViewModelStore.clear()
    }
}