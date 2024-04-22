package com.sijuru.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sijuru.core.data.local.entity.User
import com.sijuru.core.data.repository.MainRepository
import com.sijuru.core.data.response.BaseResponseObject
import com.sijuru.core.utils.ResultWrapper
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {

    private val _dataLogin = MutableLiveData<BaseResponseObject<User>?>()
    val dataLogin : LiveData<BaseResponseObject<User>?>
        get() = _dataLogin

    fun postLogin(identify_number:String?, password : String?,mainListener: MainListener){
        viewModelScope.launch {
            val response = mainRepository.postLogin(identify_number,password)
            when(response){
                is ResultWrapper.GenericError->{
                    Log.e("SijuruJi", "GenericError")
                    mainListener.onFailedLogin("${response.error}")
                }
                is ResultWrapper.NetworkError->{
                    Log.e("SijuruJi", "NetworkError")
                    mainListener.onFailedLogin("Bad Network")
                }
                is ResultWrapper.Success->{
                    try {
                        _dataLogin.value = response.value
                        mainListener.onSuccessLogin(response.value)
                    } catch (e: Exception) {
                        Log.e("SijuruJi", "$e")
                    }
                }
            }
        }
    }

}