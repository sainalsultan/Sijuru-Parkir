package com.sijuru.main_menu

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.sijuru.R
import com.sijuru.core.data.local.entity.DataUser


class AdapterDetailList : RecyclerView.Adapter<AdapterDetailList.ViewHolder>() {

    interface AdapterDetailListListener{
        fun OnClickItemDetailList(id: Int?, name: String?)
    }

    private var mOnClickItemDetailListListener : AdapterDetailListListener? = null
    private val list: MutableList<DataUser> = mutableListOf()

    fun setAdapter(list: List<DataUser>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun OnClickItemAllProduct(mOnClickItemAllProductListener : AdapterDetailListListener){
        this.mOnClickItemDetailListListener = mOnClickItemAllProductListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_detail_list, parent, false))
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.itemView.run {

                val textview_title = this.findViewById<TextView>(R.id.textview_title)
                val line_separator = this.findViewById<View>(R.id.line_separator)
                val marginStart: Int
                val marginEnd: Int
                val marginTop: Int
                val marginBottom: Int

                marginStart = context.resources.getDimensionPixelSize(R.dimen._16dp)
                marginEnd = context.resources.getDimensionPixelSize(R.dimen._16dp)

                if (position == 0) {
                    marginTop = context.resources.getDimensionPixelSize(R.dimen._16dp)
                } else {
                    marginTop = context.resources.getDimensionPixelSize(R.dimen._8dp)
                }

                if (position == list?.size!! - 1) {
                    marginBottom = context.resources.getDimensionPixelSize(R.dimen._16dp)
                    line_separator?.visibility = View.GONE
                } else {
                    marginBottom = 0
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

                    textview_title?.text = name

                    setOnClickListener {
                        mOnClickItemDetailListListener?.OnClickItemDetailList(id,name)
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