package com.sijuru.core.data.repository

import com.sijuru.core.data.local.entity.*
import com.sijuru.core.data.network.ApiInterface
import com.sijuru.core.data.response.BaseResponse
import com.sijuru.core.data.response.BaseResponseList
import com.sijuru.core.data.response.BaseResponseObject
import com.sijuru.core.utils.ResultWrapper
import com.sijuru.core.utils.SafeApiCaller
import kotlinx.coroutines.Dispatchers
import okhttp3.Dispatcher
import okhttp3.ResponseBody
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val service : ApiInterface
) : SafeApiCaller() {

    suspend fun postLogin(identify_number:String?, password : String?) : ResultWrapper<BaseResponseObject<User>>{
        return safeApiCall(Dispatchers.IO){service.postLogin(identify_number,password)}
    }

    suspend fun postVehicleRecord(bodyVehicle: BodyVehicle) : ResultWrapper<BaseResponseObject<VehicleRecord>>{
        return safeApiCall(Dispatchers.IO){service.postVehicleRecord(bodyVehicle)}
    }

    suspend fun postVehicleRecordPunishment(bodyVehicle: Vehicle) : ResultWrapper<BaseResponse>{
        return safeApiCall(Dispatchers.IO){service.postVehicleRecordPunishment(bodyVehicle)}
    }

    suspend fun updateVehicleRecord(uuid:String?, bodyVehicle: BodyVehicle) : ResultWrapper<BaseResponseObject<Vehicle>>{
        return safeApiCall(Dispatchers.IO){service.updateVehicleRecord(uuid,bodyVehicle)}
    }

    suspend fun getHistoryVehicle() : ResultWrapper<BaseResponseList<ResponseVehicleQRCodeItem>>{
        return safeApiCall(Dispatchers.IO){service.getHistoryVehicle()}
    }

    suspend fun getHistoryVehicleById(id:String?) : ResultWrapper<BaseResponseObject<ResponseVehicleQRCodeItem>>{
        return safeApiCall(Dispatchers.IO){service.getHistoryVehicleById(id)}
    }

}