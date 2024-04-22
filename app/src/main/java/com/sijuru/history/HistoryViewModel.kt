package com.sijuru.history

import android.service.controls.ControlsProviderService.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sijuru.core.data.local.entity.BodyVehicle
import com.sijuru.core.data.local.entity.ResponseVehicleQRCodeItem
import com.sijuru.core.data.local.entity.Vehicle
import com.sijuru.core.data.repository.MainRepository
import com.sijuru.core.data.response.BaseResponse
import com.sijuru.core.data.response.BaseResponseList
import com.sijuru.core.utils.ResultWrapper
import com.sijuru.login.MainListener
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import javax.inject.Inject

class HistoryViewModel @Inject constructor(
    private val mainRepository: MainRepository
): ViewModel() {

    private val _dataVehicleRecord = MutableLiveData<BaseResponseList<ResponseVehicleQRCodeItem>>()
    val dataVehicleRecord : LiveData<BaseResponseList<ResponseVehicleQRCodeItem>>
        get() = _dataVehicleRecord

    private val _dataVehicle = MutableLiveData<BaseResponse>()
    val dataVehicle : LiveData<BaseResponse>
        get() = _dataVehicle

    fun getHistoryVehicle(mainListener: MainListener){
        viewModelScope.launch {
            val response = mainRepository.getHistoryVehicle()
            when(response){
                is ResultWrapper.GenericError->{
                    Log.e("SijuruJi", "GenericError")
                    mainListener.onFailed("${response.error}")
                }
                is ResultWrapper.NetworkError->{
                    Log.e("SijuruJi", "NetworkError")
                    mainListener.onFailed("Bad Network")
                }
                is ResultWrapper.Success->{
                    try {
                        _dataVehicleRecord.value = response.value!!
                        mainListener.onSuccessGetHistory(response.value)
                    } catch (e: Exception) {
                        Log.e("SijuruJi", "$e")
                    }
                }
            }
        }
    }

    fun postVehicleRecordPunishment(bodyVehicle: Vehicle, mainListener: MainListener){
        viewModelScope.launch {
            val response = mainRepository.postVehicleRecordPunishment(bodyVehicle)
            when(response){
                is ResultWrapper.GenericError->{
                    Log.e("SijuruJi", "GenericError")
                    mainListener.onFailed("${response.error}")
                }
                is ResultWrapper.NetworkError->{
                    Log.e("SijuruJi", "NetworkError")
                    mainListener.onFailed("Bad Network")
                }
                is ResultWrapper.Success->{
                    try {
                        _dataVehicle.value = response.value!!
                        mainListener.onSuccessUpdatePunishment(response.value)
                        Log.e("SijuruJi", "postVehicleRecordPunishment: $_dataVehicle")
                    } catch (e: Exception) {
                        Log.e("SijuruJi", "$e")
                    }
                }
            }
        }
    }
}