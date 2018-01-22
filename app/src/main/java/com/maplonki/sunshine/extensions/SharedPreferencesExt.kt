package com.maplonki.sunshine.extensions

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

/**
 * Created by hugo on 1/17/18.
 */

fun applyPreferences(context: Context, editorBlock: SharedPreferences.Editor.() -> Unit) {
    val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = preferences.edit()
    editor.editorBlock()
    editor.apply()

}

val Context.preferences: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(this)