package com.maplonki.sunshine.extensions

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by hugo on 1/17/18.
 */
inline fun Context.startActivity(action: String, uriData: Uri? = null, cantResolve: () -> Unit) {
    val actionIntent = Intent(action).apply {
        uriData?.let {
            data = uriData
        }
    }
    if (actionIntent.resolveActivity(packageManager) != null) {
        startActivity(actionIntent)
    } else {
        cantResolve()
    }
}

fun Context.inflate(layoutRes: Int, parent: ViewGroup? = null, attachToRoot: Boolean = false): View {
    val attach = if (parent == null) {
        false
    } else {
        attachToRoot
    }
    return LayoutInflater.from(this).inflate(layoutRes, parent, attach)
}
