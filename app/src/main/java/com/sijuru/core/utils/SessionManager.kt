package com.sijuru.core.utils

import android.content.Context
import android.content.SharedPreferences
import com.sijuru.core.data.local.entity.User
import com.sijuru.core.data.local.entity.VehicleSijuruParkingTypeDetai


class SessionManager(internal var _context: Context) {

    internal var pref: SharedPreferences
    internal var editor: SharedPreferences.Editor
    internal var PRIVATE_MODE = 0

    /*val userDetails: HashMap<String, String>
        get() {
            val user = HashMap<String, String>()
            user[KEY_ID] = "${pref.getString(KEY_ID, null)}"
            user[KEY_NAME] = "${pref.getString(KEY_NAME, null)}"
            user[KEY_kode] = "${pref.getString(KEY_kode, null)}"
            user[KEY_phone] = "${pref.getString(KEY_phone, null)}"
            user[KEY_foto] = "${pref.getString(KEY_foto, null)}"
            user[KEY_level] = "${pref.getString(KEY_level, null)}"
            user[KEY_token] = "${pref.getString(KEY_token, null)}"

            return user
        }*/

    val isLoggedIn: Boolean
        get() = pref.getBoolean(IS_LOGIN, false)

    init {
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }

    fun setLogin(login: Boolean) {
        editor.putBoolean(IS_LOGIN, login)
        editor.commit()
    }
    /*fun setName(name: String) {
        editor.putString(KEY_NAME, name)
        editor.commit()
    }*/
    fun setTokenAccess(token_access:String){
        editor.putString(key_token_access,token_access)
        editor.commit()
    }
    fun setMerchantId(merchant_id:String){
        editor.putString(key_merchant_id,merchant_id)
        editor.commit()
    }
    fun setOperatorId(operator_id:String){
        editor.putString(key_operator_id,operator_id)
        editor.commit()
    }
    fun setOperatorLocation(operator_location:String){
        editor.putString(key_operator_location,operator_location)
        editor.commit()
    }
    fun setOperatorPhone(operator_phone:String){
        editor.putString(key_operator_phone,operator_phone)
        editor.commit()
    }
    fun setOperatorShift(operator_shift:String){
        editor.putString(key_operator_shift,operator_shift)
        editor.commit()
    }
    fun setUserId(user_id:String){
        editor.putString(key_user_id,user_id)
        editor.commit()
    }
    fun setPriceBase(price_base:String){
        editor.putString(key_price_base,price_base)
        editor.commit()
    }
    fun setPriceIncrement(price_increment:String){
        editor.putString(key_price_increment,price_increment)
        editor.commit()
    }
    fun setPriceIncrement_price(price_increment_price:String){
        editor.putString(key_price_increment_price,price_increment_price)
        editor.commit()
    }
    fun setPriceMaxPrice(price_max_price:String){
        editor.putString(key_price_max_price,price_max_price)
        editor.commit()
    }
    fun setType(type:String){
        editor.putString(key_type,type)
        editor.commit()
    }
    fun setPrinter(printer:String){
        editor.putString(key_printer,printer)
        editor.commit()
    }

    fun getLogin(): Boolean = pref.getBoolean(IS_LOGIN, false)
    fun getTokenAccess(): String = pref.getString(key_token_access, null).toString()
    fun getMerchantId(): String = pref.getString(key_merchant_id, null).toString()
    fun getOperatorId(): String = pref.getString(key_operator_id, null).toString()
    fun getOperatorName(): String = pref.getString(key_operator_name, null).toString()
    fun getOperatorLocation(): String = pref.getString(key_operator_location, null).toString()
    fun getOperatorPhone(): String = pref.getString(key_operator_phone, null).toString()
    fun getOperatorShift(): String = pref.getString(key_operator_shift, null).toString()
    fun getUserId(): String = pref.getString(key_user_id, null).toString()
    fun getPriceBase(): String = pref.getString(key_price_base, null).toString()
    fun getPriceIncrement(): String = pref.getString(key_price_increment, null).toString()
    fun getPriceIncrement_price(): String = pref.getString(key_price_increment_price, null).toString()
    fun getPriceMaxPrice(): String = pref.getString(key_price_max_price, null).toString()
    fun getType(): String = pref.getString(key_type, null).toString()
    fun getTypeDetail(): String = pref.getString(key_type_detail, null).toString()
    fun getName(): String = pref.getString(key_name, null).toString()
    fun getPrinter(): String = pref.getString(key_printer, null).toString()
    fun getParkingType(): String = pref.getString(key_parking_type, null).toString()


    /*fun getLogin(): Boolean = pref.getBoolean(IS_LOGIN, false)
    fun getName(): String = pref.getString(KEY_NAME, null).toString()
    fun getID(): String = pref.getString(KEY_ID, null).toString()
    fun getPhone(): String = pref.getString(KEY_phone, null).toString()*/

    fun createLoginSession(
        isLogin: Boolean,
        token_access: String?,
        merchant_id: String?,
        operator_id: String?,
        operator_name: String?,
        operator_phone: String?,
        operator_shift: String?,
        operator_location: String?,
        user_id: String?,
        price_base: String?,
        price_increment: String?,
        price_increment_price: String?,
        price_max_price: String?,
        type: String?,
        type_detail: String?,
        name: String?,
        parking_type: String?
    ) {
        editor.putBoolean(IS_LOGIN, isLogin)
        /*editor.putString(KEY_ID, id)
        editor.putString(KEY_NAME, nama)
        editor.putString(KEY_kode, kode)
        editor.putString(KEY_phone, phone)
        editor.putString(KEY_foto, foto)
        editor.putString(KEY_level, level)
        editor.putString(KEY_token, token)*/
        editor.putString(key_token_access, token_access)
        editor.putString(key_merchant_id, merchant_id)
        editor.putString(key_operator_id, operator_id)
        editor.putString(key_operator_name, operator_name)
        editor.putString(key_operator_phone, operator_phone)
        editor.putString(key_operator_shift, operator_shift)
        editor.putString(key_operator_location, operator_location)
        editor.putString(key_user_id, user_id)
        editor.putString(key_price_base, price_base)
        editor.putString(key_price_increment, price_increment)
        editor.putString(key_price_increment_price, price_increment_price)
        editor.putString(key_price_max_price, price_max_price)
        editor.putString(key_type, type)
        editor.putString(key_type_detail, type_detail)
        editor.putString(key_name, name)
        editor.putString(key_parking_type, parking_type)

        editor.commit()
    }

    fun logoutUser() {
        editor.putBoolean(IS_LOGIN, false)
        editor.clear()
        editor.commit()
    }

    fun checkLogin() {
        if (!this.isLoggedIn) {

        }
    }

    companion object {
        private val PREF_NAME = "hidupsehat"
        private val IS_LOGIN = "IsLoggedIn"

        /*val KEY_ID = "id"
        val KEY_NAME = "nama"
        val KEY_kode = "kode"
        val KEY_phone = "phone"
        val KEY_foto = "foto"
        val KEY_level = "level"
        val KEY_token = "token"*/

        val key_token_access = "token_access"
        val key_merchant_id = "merchant_id"
        val key_operator_id = "operator_id"
        val key_operator_name = "operator_name"
        val key_operator_location = "operator_location"
        val key_operator_phone = "operator_phone"
        val key_operator_shift = "operator_shift"
        val key_user_id = "user_id"
        val key_price_base = "price_base"
        val key_price_increment = "price_increment"
        val key_price_increment_price = "price_increment_price"
        val key_price_max_price = "price_max_price"
        val key_type = "type"
        val key_type_detail = "key_type_detail"
        val key_name = "name"
        val key_printer = "printer"
        val key_parking_type = "parking_type"
    }


}