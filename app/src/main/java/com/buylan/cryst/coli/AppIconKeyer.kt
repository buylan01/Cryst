package com.buylan.cryst.coli

import coil3.key.Keyer

class AppIconKeyer : Keyer<AppIconRequest> {
    override fun key(data: AppIconRequest, options: coil3.request.Options): String {
        return "${data.packageName}_${data.lastUpdate}"
    }
}