package com.sijuru.login

import com.sijuru.core.data.local.entity.*
import com.sijuru.core.data.response.BaseResponse
import com.sijuru.core.data.response.BaseResponseList
import com.sijuru.core.data.response.BaseResponseObject

interface MainListener {
    fun onSuccessLogin(value: BaseResponseObject<User>) {}
    fun onFailedLogin(message:String){}

    fun onSuccessRecord(value: BaseResponseObject<VehicleRecord>) {}
    fun onSuccessUpdateRecord(value: BaseResponseObject<Vehicle>) {}
    fun onSuccessGetHistory(value: BaseResponseList<ResponseVehicleQRCodeItem>) {}
    fun onSuccessGetHistoryById(value: BaseResponseObject<ResponseVehicleQRCodeItem>) {}
    fun onSuccessUpdatePunishment(value: BaseResponse) {}
    fun onFailed(message:String){}
}