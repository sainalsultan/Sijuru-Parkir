package com.kioser.app.core.base

import androidx.annotation.IdRes
import androidx.annotation.StringRes

/**
 * Created by Sainal Sultan on 23/09/2020.
 * kioser.com
 */
interface BaseView {
    fun showMessage(message : String)
    fun showMessage(@IdRes @StringRes resId : Int)
    fun isNetworkConnected() : Boolean
}