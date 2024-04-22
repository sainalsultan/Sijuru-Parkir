package com.sijuru.core.data.network

import com.sijuru.core.data.local.entity.*
import com.sijuru.core.data.response.BaseResponse
import com.sijuru.core.data.response.BaseResponseList
import com.sijuru.core.data.response.BaseResponseObject
import okhttp3.ResponseBody
import retrofit2.http.*

/**
 * REST API access Point
 */

interface ApiInterface {

    @FormUrlEncoded
    @POST("login")
    suspend fun postLogin(
        @Field("identify_number") identifyNumber : String?,
        @Field("password") password : String?
    ): BaseResponseObject<User>

    @POST("vehicle/record")
    suspend fun postVehicleRecord(
        @Body vehicle: BodyVehicle
    ): BaseResponseObject<VehicleRecord>

    @POST("vehicle/record/punishment")
    suspend fun postVehicleRecordPunishment(
        @Body vehicle_sijuru_transaction_id: Vehicle
    ): BaseResponse

    @PUT("vehicle/{uuid}/record")
    suspend fun updateVehicleRecord(
        @Path("uuid") uuid : String?,
        @Body vehicle: BodyVehicle
    ): BaseResponseObject<Vehicle>

    @GET("vehicle/history")
    suspend fun getHistoryVehicle(): BaseResponseList<ResponseVehicleQRCodeItem>

    @GET("vehicle/history")
    suspend fun getHistoryVehicleById(@Query("id") id:String?): BaseResponseObject<ResponseVehicleQRCodeItem>

    /*@FormUrlEncoded
    @POST("vehicle/record")
    suspend fun postVehicleRecord(
        @Field("identify_number") identifyNumber : String?,
        @Field("vehicle_sijuru_operator_id") vehicle_sijuru_operator_id : String?,
        @Field("vehicle_sijuru_operator_shift") vehicle_sijuru_operator_shift : String?,
        @Field("vehicle_type") vehicle_type : String?,
        @Field("vehicle_plat_front") vehicle_plat_front : String?,
        @Field("vehicle_plat_middle") vehicle_plat_middle : String?,
        @Field("vehicle_plat_back") vehicle_plat_back : String?,
        @Field("vehicle_start_time_record") vehicle_start_time_record : String?,
        @Field("vehicle_end_time_record") vehicle_end_time_record : String?,
        @Field("vehicle_total") vehicle_total : String?
    ): BaseResponseObject<User>*/

    /*@GET("common/product/prefix")
    fun getAllPrefixs(): Response<Prefix>*/

    /*{
        "vehicle": [
        {
            "vehicle_sijuru_operator_id": "0953d806-9582-11ec-8567-ae3f7895271f",
            "vehicle_sijuru_operator_shift": "1",
            "vehicle_type": "motorcycle",
            "vehicle_plat_front": "DD",
            "vehicle_plat_middle": "9990",
            "vehicle_plat_back": "BAU",
            "vehicle_start_time_record": "2022-02-12 11:11:11",
            "vehicle_end_time_record" : "2022-02-12 11:11:11",
            "vehicle_total": "2000"
        },
        {
            "vehicle_sijuru_operator_id": "0953d806-9582-11ec-8567-ae3f7895271f",
            "vehicle_sijuru_operator_shift": "1",
            "vehicle_type": "car",
            "vehicle_plat_front": "DD",
            "vehicle_plat_middle": "9991",
            "vehicle_plat_back": "BAU",
            "vehicle_start_time_record": "2022-02-12 11:11:11",
            "vehicle_end_time_record" : "NULL",
            "vehicle_total": "3000"
        }
        ]
    }*/

}