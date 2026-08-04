package com.buylan.cryst.coli

import coil3.key.Keyer

class ApkIconKeyer : Keyer<ApkIconRequest> {
    override fun key(data: ApkIconRequest, options: coil3.request.Options): String {
        return "${data.apkPath}_${data.lastModified}"
    }
}