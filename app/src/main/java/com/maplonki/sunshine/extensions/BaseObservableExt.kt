package com.maplonki.sunshine.extensions

import android.databinding.BaseObservable

/**
 * Created by hugo on 1/20/18.
 */
/** Notifies multiple attributes that a value has changed*/
fun BaseObservable.notify(vararg fields: Int) {
    fields.forEach { notifyPropertyChanged(it) }
}