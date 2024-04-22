package com.sijuru.history

import android.annotation.SuppressLint
import android.content.Context
import android.media.Image
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.sijuru.R
import com.sijuru.core.data.local.entity.DataUser
import com.sijuru.core.data.local.entity.ResponseVehicleQRCodeItem
import com.sijuru.core.data.local.entity.Vehicle


class AdapterListHistory : RecyclerView.Adapter<AdapterListHistory.ViewHolder>() {

    interface AdapterListHistoryListener{
        fun OnClickItemHistory(data : ResponseVehicleQRCodeItem)
    }

    private var mOnClickItemHistoryListener : AdapterListHistoryListener? = null
    private val list: MutableList<ResponseVehicleQRCodeItem> = mutableListOf()

    fun setAdapter(list: List<ResponseVehicleQRCodeItem>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun OnClickItemHistory(mOnClickItemHistoryListener : AdapterListHistoryListener){
        this.mOnClickItemHistoryListener = mOnClickItemHistoryListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_list_history, parent, false))
    }

    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.itemView.run {

                val constraintlayout_list_history = this.findViewById<ConstraintLayout>(R.id.constraintlayout_list_history)
                val textview_ticket_number_value = this.findViewById<TextView>(R.id.textview_ticket_number_value)
                val textview_plate_value= this.findViewById<TextView>(R.id.textview_plate_value)
                val textview_first_time_value= this.findViewById<TextView>(R.id.textview_first_time_value)
                val textview_price_value= this.findViewById<TextView>(R.id.textview_price_value)
                val imageview_vehicle= this.findViewById<ImageView>(R.id.imageview_vehicle)
                val line_separator = this.findViewById<View>(R.id.line_separator)

                val marginStart: Int
                val marginEnd: Int
                val marginTop: Int
                val marginBottom: Int

                marginStart = 0
                marginEnd = 0
                marginBottom = 0
                marginTop = 0

                if (position == list.size - 1) {
                    line_separator?.visibility = View.INVISIBLE
                } else {
                    line_separator?.visibility = View.VISIBLE
                }

                (this.layoutParams as RecyclerView.LayoutParams).also {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        it.leftMargin = marginStart
                        it.rightMargin = marginEnd
                        it.topMargin = marginTop
                        it.bottomMargin = marginBottom

                    } else {
                        it.marginStart = marginStart
                        it.marginEnd = marginEnd
                        it.topMargin = marginTop
                        it.bottomMargin = marginBottom
                    }
                }

                list.get(position).run {
                    textview_ticket_number_value?.text = parking_type_detail.filter { it.type == vehicle_type }.get(0).name
                    textview_plate_value?.text = "$vehicle_plat_front-$vehicle_plat_middle-$vehicle_plat_back"
                    textview_first_time_value?.text = vehicle_start_time_record
                    textview_price_value?.text = "Rp $vehicle_total"

                    if (parking_type.toLowerCase().equals("retribution")){
                        imageview_vehicle.visibility = View.INVISIBLE
                    }else {
                        if (vehicle_type?.contains("roda_4") == true) {
                            imageview_vehicle?.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.ic_car,
                                    null
                                )
                            )
                        } else {
                            imageview_vehicle?.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.ic_motorcycle,
                                    null
                                )
                            )
                        }
                    }

                    if (this.vehicle_status == true){
                        //selesai
                        constraintlayout_list_history.background = resources.getDrawable(R.color.colorGreenPlus2,null)
                    }else{
                        //belum
                        constraintlayout_list_history.background = resources.getDrawable(R.color.colorRedPlus2,null)
                    }

                    setOnClickListener {
                        mOnClickItemHistoryListener?.OnClickItemHistory(this)
                        /*if (this.vehicle_status == true) {
                            mOnClickItemHistoryListener?.OnClickItemHistory(this)
                        }else{
                            Toast.makeText(context, "Data belum tersedia", Toast.LENGTH_SHORT).show()
                        }*/
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}