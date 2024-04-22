package com.kioser.app.core.base

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Sainal Sultan on 03/11/2020.
 * kioser.com
 */
abstract class BaseViewHolder<T : ViewDataBinding>(
        val binding: T
) : RecyclerView.ViewHolder(binding.root)