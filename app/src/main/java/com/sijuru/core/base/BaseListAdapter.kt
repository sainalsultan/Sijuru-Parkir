package com.kioser.app.core.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*

/**
 * Created by Sainal Sultan on 03/11/2020.
 * kioser.com
 */

abstract class BaseListAdapter<T>(
        itemsSame: (T, T) -> Boolean,
        contentsSame: (T, T) -> Boolean
) : ListAdapter<T, RecyclerView.ViewHolder>(object : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = itemsSame(oldItem, newItem)
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = contentsSame(oldItem, newItem)
}) {

    abstract fun onCreateViewHolder(
            parent: ViewGroup,
            inflater: LayoutInflater,
            viewType: Int
    ): RecyclerView.ViewHolder


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            onCreateViewHolder(
                    parent = parent,
                    inflater = LayoutInflater.from(parent.context),
                    viewType = viewType
            )
}