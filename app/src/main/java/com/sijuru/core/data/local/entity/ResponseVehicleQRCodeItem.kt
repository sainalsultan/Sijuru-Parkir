package com.sijuru.core.data.local.entity

data class ResponseVehicleQRCodeItem(
    val parking_type: String,
    val parking_type_detail: List<ParkingTypeDetail>,
    val vehicle_created_date: String,
    val vehicle_end_time_record: String,
    val vehicle_image: String,
    val vehicle_plat_back: String,
    val vehicle_plat_front: String,
    val vehicle_plat_middle: String,
    val vehicle_sijuru_operator: String,
    val vehicle_sijuru_operator_shift: String,
    val vehicle_start_time_record: String,
    val vehicle_status: Boolean,
    val vehicle_total: String,
    val vehicle_type: String,
    val vehicle_uuid: String
)