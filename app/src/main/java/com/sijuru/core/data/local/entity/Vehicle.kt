package com.sijuru.core.data.local.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Vehicle(
    val _id: String?=null,
    val vehicle_sijuru_transaction_id: String?,
    val vehicle_end_time_record: String?,
    val vehicle_plat_back: String?,
    val vehicle_plat_front: String?,
    val vehicle_plat_middle: String?,
    val vehicle_sijuru_operator_id: String?,
    val vehicle_sijuru_operator_shift: String?,
    val vehicle_start_time_record: String?,
    val vehicle_total: String?,
    val vehicle_type: String?,
    val vehicle_uuid: String?,
    val vehicle_created_date: String?,
    val vehicle_status: Boolean?
):Parcelable