package com.kioser.receipt.product

import android.content.ContentValues.TAG
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sijuru.PrinterListener
import com.sijuru.R
import com.sijuru.core.data.local.entity.Printer

class AdapterAvailablePrinter : RecyclerView.Adapter<AdapterAvailablePrinter.ViewHolder>() {

    private lateinit var mSetOnClickListener : PrinterListener
    private val list: MutableList<Printer> = mutableListOf()

    fun setOnPrintClickListener(mSetOnClickListener : PrinterListener){
        this.mSetOnClickListener = mSetOnClickListener
    }

    fun setAdapter(list: List<Printer>) {
        Log.e(TAG, "setAdapter: $list")
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_list_printer, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.itemView.run {

                val textview_printer_name = this.findViewById<TextView>(R.id.textview_printer_name)
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

                if (position == list.size - 1) {
                    marginBottom = context.resources.getDimensionPixelSize(R.dimen._16dp)
                    line_separator?.visibility = View.INVISIBLE
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

                    textview_printer_name?.text = nama_printer

                    setOnClickListener {
                        Log.e(TAG, "onBindViewHolder: ")
                        mSetOnClickListener.printClick(this)
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