package com.sijuru.main_menu

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sijuru.core.data.local.entity.*
import com.sijuru.core.data.repository.MainRepository
import com.sijuru.core.data.response.BaseResponseList
import com.sijuru.core.data.response.BaseResponseObject
import com.sijuru.core.utils.ResultWrapper
import com.sijuru.login.MainListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainMenuViewModel @Inject constructor(
    private val mainRepository: MainRepository
): ViewModel() {

    private val _dataVehicleRecord = MutableLiveData<BaseResponseObject<VehicleRecord>?>()
    val dataVehicleRecord : LiveData<BaseResponseObject<VehicleRecord>?>
        get() = _dataVehicleRecord

    private val _updateDataVehicleRecord = MutableLiveData<BaseResponseObject<Vehicle>?>()
    val updateDataVehicleRecord : LiveData<BaseResponseObject<Vehicle>?>
        get() = _updateDataVehicleRecord

    private val _dataVehicle = MutableLiveData<BaseResponseObject<ResponseVehicleQRCodeItem>?>()
    val dataVehicle : LiveData<BaseResponseObject<ResponseVehicleQRCodeItem>?>
        get() = _dataVehicle

    fun postVehicleRecord(bodyVehicle: BodyVehicle, mainListener: MainListener){
        viewModelScope.launch {
            val response = mainRepository.postVehicleRecord(bodyVehicle)
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
                        _dataVehicleRecord.value = response.value
                        mainListener.onSuccessRecord(response.value)
                    } catch (e: Exception) {
                        Log.e("SijuruJi", "$e")
                    }
                }
            }
        }
    }

    fun updateVehicleRecord(uuid : String?, bodyVehicle: BodyVehicle, mainListener: MainListener){
        viewModelScope.launch {
            val response = mainRepository.updateVehicleRecord(uuid, bodyVehicle)
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
                        _updateDataVehicleRecord.value = response.value
                        mainListener.onSuccessUpdateRecord(response.value)
                    } catch (e: Exception) {
                        Log.e("SijuruJi", "$e")
                    }
                }
            }
        }
    }

    fun getHistoryVehicleById(id : String?, mainListener: MainListener){
        viewModelScope.launch {
            val response = mainRepository.getHistoryVehicleById(id)
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
                        when(response.value.responseCode){
                            1000-> {
                                if (response.value.responseData == null) {
                                    mainListener.onFailed("Belum ada data")
                                } else {
                                    _dataVehicle.value = response.value
                                    mainListener.onSuccessGetHistoryById(response.value)
                                }
                            }
                            else->{
                                mainListener.onFailed(response.value.responseDescription)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("SijuruJi", "$e")
                        mainListener.onFailed("${e.message}")
                    }
                }
            }
        }
    }

}