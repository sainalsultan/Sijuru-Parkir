package com.sijuru.main_menu

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Build
import android.service.controls.ControlsProviderService.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.sijuru.R
import com.sijuru.core.data.local.entity.VehicleSijuruParkingTypeDetai


class AdapterListButton : RecyclerView.Adapter<AdapterListButton.ViewHolder>() {

    interface AdapterListButtonListener{
        fun OnClickItemListButton(data: VehicleSijuruParkingTypeDetai)
    }

    private var mOnClickItemListButtonListener : AdapterListButtonListener? = null
    private val list: MutableList<VehicleSijuruParkingTypeDetai> = mutableListOf()

    fun setAdapter(list: List<VehicleSijuruParkingTypeDetai>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun OnClickItem(mOnClickItemListButtonListener : AdapterListButtonListener){
        this.mOnClickItemListButtonListener = mOnClickItemListButtonListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_list_button, parent, false))

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.itemView.run {

                val button = this.findViewById<Button>(R.id.button)

                var marginStart: Int
                var marginEnd: Int
                var marginTop: Int
                var marginBottom: Int

                /*marginTop = 0
                marginBottom = 0
                if (position == 0){
                    marginTop = context.resources.getDimensionPixelSize(R.dimen._16dp)
                    marginBottom = context.resources.getDimensionPixelSize(R.dimen._8dp)
                }else{
                    marginTop = context.resources.getDimensionPixelSize(R.dimen._8dp)
                    marginBottom = context.resources.getDimensionPixelSize(R.dimen._16dp)
                }*/
                /*if (position.mod(2) == 0){
                    marginStart = context.resources.getDimensionPixelSize(R.dimen._16dp)
                    marginEnd = context.resources.getDimensionPixelSize(R.dimen._4dp)
                    if (position == 0) {
                        marginTop = context.resources.getDimensionPixelSize(R.dimen._16dp)
                        marginBottom = context.resources.getDimensionPixelSize(R.dimen._4dp)
                    }else{
                        marginTop = context.resources.getDimensionPixelSize(R.dimen._4dp)
                        marginBottom = context.resources.getDimensionPixelSize(R.dimen._16dp)
                    }
                }
                else{
                    marginStart = context.resources.getDimensionPixelSize(R.dimen._4dp)
                    marginEnd = context.resources.getDimensionPixelSize(R.dimen._16dp)
                    marginTop = context.resources.getDimensionPixelSize(R.dimen._16dp)
                    marginBottom = context.resources.getDimensionPixelSize(R.dimen._4dp)
                }

                if (position.mod(2)==0 && position!=0 && position==list.size-1){
                    marginStart = context.resources.getDimensionPixelSize(R.dimen._16dp)
                    marginEnd = context.resources.getDimensionPixelSize(R.dimen._16dp)
                    marginTop = context.resources.getDimensionPixelSize(R.dimen._4dp)
                    marginBottom = context.resources.getDimensionPixelSize(R.dimen._8dp)
                }*/

                if (list.size.mod(2)==0){
                    if (position.mod(2) == 0){
                        marginStart = context.resources.getDimensionPixelSize(R.dimen._16dp)
                        marginEnd = context.resources.getDimensionPixelSize(R.dimen._8dp)
                        marginTop = context.resources.getDimensionPixelSize(R.dimen._16dp)
                        if (position != list.size-2) marginBottom = 0 else marginBottom = context.resources.getDimensionPixelSize(R.dimen._16dp)
                    }else{
                        marginStart = context.resources.getDimensionPixelSize(R.dimen._8dp)
                        marginEnd = context.resources.getDimensionPixelSize(R.dimen._16dp)
                        marginTop = context.resources.getDimensionPixelSize(R.dimen._16dp)
                        if (position != list.size-1) marginBottom = 0 else marginBottom = context.resources.getDimensionPixelSize(R.dimen._16dp)
                    }
                }else{
                    if (position.mod(2) == 0){
                        marginStart = context.resources.getDimensionPixelSize(R.dimen._16dp)
                        marginEnd = context.resources.getDimensionPixelSize(R.dimen._8dp)
                        marginTop = context.resources.getDimensionPixelSize(R.dimen._16dp)
                        if (position == list.size-1) {
                            marginEnd = context.resources.getDimensionPixelSize(R.dimen._16dp)
                            marginBottom = context.resources.getDimensionPixelSize(R.dimen._16dp)
                        } else {
                            marginEnd = 0
                            marginBottom = 0
                        }
                    }
                    else{
                        marginStart = context.resources.getDimensionPixelSize(R.dimen._8dp)
                        marginTop = context.resources.getDimensionPixelSize(R.dimen._16dp)
                        if (position == list.size-1) {
                            marginEnd = context.resources.getDimensionPixelSize(R.dimen._16dp)
                            marginBottom = context.resources.getDimensionPixelSize(R.dimen._16dp)
                        } else {
                            marginEnd = context.resources.getDimensionPixelSize(R.dimen._16dp)
                            marginBottom = 0
                        }
                    }
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

                list.get(position).also { data ->

                    button?.text = data.name
                    button?.setOnClickListener {
                        Log.e(ContentValues.TAG, "OnClickItemListButton: $data")
                        mOnClickItemListButtonListener?.OnClickItemListButton(data)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        if (list.size.mod(2)==0){
            return 1
        }else{
            if (position.mod(2)==0 && position==list.size-1){
                Log.e(TAG, "getItemViewType: $position")
                return 2
            }else{
                Log.e(TAG, "getItemViewType: $position")
                return 1
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}