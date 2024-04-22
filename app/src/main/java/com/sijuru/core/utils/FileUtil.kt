package com.sijuru.core.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object FileUtil {
    private fun getCurrentDate(): String {
        val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val date = Date()
        return simpleDateFormat.format(date)
    }

    fun savePicture(context: Context, data: ByteArray): File? {
        val fileName = "${getCurrentDate()}.png"
        val filePath = context.getExternalFilesDir(null)?.absolutePath
        val file = File(filePath, fileName)
        try {
            if (file.exists()) {
                file.delete()
            }
            file.createNewFile()
            val fos = FileOutputStream(file)
            fos.write(data)
            fos.flush()
            fos.close()
            return file
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    suspend fun savePicture(context: Context, bitmapCropped: Bitmap,orientation : Int): File? {
        val fileName = "${getCurrentDate()}.jpeg"
        val filePath = context.getExternalFilesDir(null)?.absolutePath
        val file = File(filePath, fileName)
        try {
            if (file.exists()) {
                file.delete()
            }

            file.createNewFile()
            val fos = FileOutputStream(file)
            val bos = ByteArrayOutputStream()
            bitmapCropped?.setHasAlpha(true)
            bitmapCropped?.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            fos.write(bos.toByteArray())
            fos.flush()
            fos.close()

//            val exifInterface = ExifInterface(file.path)
//            val orientationValue = CameraV1Util.getOrientationExifValue(orientation).toString()
//            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, orientationValue)
//            exifInterface.saveAttributes()
//
//            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
//            intent.data = Uri.fromFile(file)
//            context.sendBroadcast(intent)




            return file
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("Anjay","${e.message}")
        }
        return null
    }

    fun updateMediaScanner(context: Context, file: File?) {
        if (file != null) {
            return
        }
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        intent.data = Uri.fromFile(file)
        context.sendBroadcast(intent)
    }
}