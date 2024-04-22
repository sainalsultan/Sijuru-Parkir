package com.kioser.app.core.base

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewParent
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Sainal Sultan on 03/11/2020.
 * kioser.com
 */
/*
abstract class BasePagedListAdapter<T>(
        itemsSame : (T, T) -> Boolean,
        contensSame : (T, T) -> Boolean
) : PagedListAdapter<T,RecyclerView.ViewHolder>(object : DiffUtil.ItemCallback<T>(){
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = itemsSame(oldItem,newItem)
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = contensSame(oldItem,newItem)
}) {

    internal var recyclerView: RecyclerView? = null

    init {
        super.setHasStableIds(true)
    }

    abstract fun onCreateViewHolder(
            parent: ViewGroup,
            inflater: LayoutInflater,
            viewType : Int
    ) : RecyclerView.ViewHolder


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder  =
            onCreateViewHolder(
                    parent = parent,
                    inflater = LayoutInflater.from(parent.context),
                    viewType = viewType
            )


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun submitList(pagedList: PagedList<T>?) {
        super.submitList(pagedList)
        if (pagedList.isNullOrEmpty()) {
            // Fix recycle view not scrolling to the top after refresh the data source.
            recyclerView?.scrollToPosition(0)
        }
    }
}*/
