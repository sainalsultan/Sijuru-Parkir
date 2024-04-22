package com.sijuru.core.data.local.entity

data class User(
    val token_access: String,
    val vehicle_sijuru_parking_type_detai: List<VehicleSijuruParkingTypeDetai>,
    val vehicle_merchant_id: String,
    val vehicle_sijuru_operator_id: String,
    val vehicle_sijuru_operator_location: String,
    val vehicle_sijuru_operator_name: String,
    val vehicle_sijuru_operator_phone: String,
    val vehicle_sijuru_operator_role: String,
    val vehicle_sijuru_operator_shift: Int,
    val vehicle_sijuru_parking_type: String,
    val vehicle_user_id: String
)