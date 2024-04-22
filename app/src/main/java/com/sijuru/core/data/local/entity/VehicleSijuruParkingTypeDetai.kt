package com.sijuru.core.data.local.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VehicleSijuruParkingTypeDetai(
    val name: String,
    val price_base: String,
    val price_increment: String,
    val price_increment_price: String,
    val price_max_price: String,
    val type: String
):Parcelable