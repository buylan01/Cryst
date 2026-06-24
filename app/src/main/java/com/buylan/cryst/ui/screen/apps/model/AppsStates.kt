package com.buylan.cryst.ui.screen.apps.model

import com.buylan.cryst.ui.screen.apps.AppsDestination
import com.buylan.cryst.ui.screen.apps.AppsSortType

data class AppsStates(
    val selectedDestination: AppsDestination = AppsDestination.USER,
    val sortType: AppsSortType = AppsSortType.UPDATE_TIME,
    val isLoading: Boolean = true,
    val showMenu: Boolean = false,
    val showSortDialog: Boolean = false,
    val searchActive: Boolean = false,
    val userApps: List<ApkInfo> = emptyList(),
    val systemApps: List<ApkInfo> = emptyList(),
    val appDialog: ApkInfo? = null,
    val locateDialog: String? = null
)