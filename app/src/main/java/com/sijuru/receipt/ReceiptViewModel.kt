package com.sijuru.receipt

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sijuru.core.data.local.entity.BodyVehicle
import com.sijuru.core.data.local.entity.VehicleRecord
import com.sijuru.core.data.repository.MainRepository
import com.sijuru.core.data.response.BaseResponseObject
import com.sijuru.core.utils.ResultWrapper
import com.sijuru.login.MainListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class ReceiptViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {

    private val _dataVehicleRecord = MutableLiveData<BaseResponseObject<VehicleRecord>?>()
    val dataVehicleRecord : LiveData<BaseResponseObject<VehicleRecord>?>
        get() = _dataVehicleRecord

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
}