package com.sijuru

import com.sijuru.core.data.local.entity.Printer

interface PrinterListener {
    fun printClick(printer: Printer)
}