package com.maplonki.sunshine.view.viewmodel

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import com.maplonki.sunshine.BR

/**
 * Created by hugo on 1/19/18.
 */
class ForecastViewModel : BaseObservable() {

    var isLoading: Boolean = true
        set(value) {
            field = value
            notifyPropertyChanged(BR.listVisible)
            notifyPropertyChanged(BR.progressVisible)
        }

    @Bindable
    fun getListVisible() = if (isLoading) INVISIBLE else VISIBLE

    @Bindable
    fun getProgressVisible() = if (isLoading) VISIBLE else INVISIBLE

}