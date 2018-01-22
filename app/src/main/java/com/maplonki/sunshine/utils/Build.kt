package com.maplonki.sunshine.utils

import android.os.Build

/**
 * Created by hugo on 1/18/18.
 */

inline fun targetOreo(oreoBlock: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        oreoBlock()
    }
}