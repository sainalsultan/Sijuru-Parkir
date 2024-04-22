package com.sijuru.core.utils

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.os.Vibrator
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.sijuru.R
import java.text.NumberFormat
import java.util.*

open class FragmentHelper(val fragment: Fragment?) {
    private var dialog: AlertDialog? = null
    private var sessionManager: SessionManager? = null
    private var permissions = arrayOf(Manifest.permission.READ_CONTACTS)
    private var vibe: Vibrator? = null
    private val value = StringBuilder()

    fun rupiah(number: Int?): String {
        val localeID = Locale("in", "ID")
        val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
        return formatRupiah.format(number?.toDouble()).replace("Rp","Rp ").replace(",00","")
    }

    fun showLoading() {
        dialog = AlertDialog.Builder(fragment?.requireActivity()).setView(R.layout.view_load).show()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
//            val a = dialog?.findViewById<TextView>(R.id.textinputlayout_user_id)
    }

    fun dismissLoading() {
        fragment?.let {
            dialog?.let {
                it.dismiss()
            }

            if (dialog != null) {
                dialog?.dismiss()
            }
        }
    }

    /*fun dialogStatus(status: String, title: String, desc: String, autoDismiss : Boolean = true) {
        fragment?.let {
            if (dialog != null) {
                dialog?.dismiss()
            }

            val dialogView: View = fragment.layoutInflater.inflate(R.layout.dialog_snackbar, null)
            dialog = BottomSheetDialog(fragment.requireContext(), R.style.SheetDialogRemoveShadow)
            dialog?.setContentView(dialogView)
            dialog?.setCancelable(true)

            val imageview: ImageView? = dialog?.findViewById(R.id.imageview)
            val textview_title: TextView? = dialog?.findViewById(R.id.textview_title)
            val textview_desc: TextView? = dialog?.findViewById(R.id.textview_desc)
            val root: ConstraintLayout? = dialog?.findViewById(R.id.root)
            textview_title?.text = title
            textview_desc?.text = desc

            imageview?.let {
                when (status) {
                    Constants.STATUS_SUKSES -> {
                        root?.background = (ContextCompat.getDrawable(
                            fragment.requireContext(),
                            com.kioser.app.core.R.drawable.bg_half_round_green_top
                        ))
                        it.background = (ContextCompat.getDrawable(
                            fragment.requireContext(),
                            com.kioser.app.core.R.drawable.bg_circle_green
                        ))
                        it.setImageDrawable(
                            ContextCompat.getDrawable(
                                fragment.requireContext(),
                                com.kioser.app.core.R.drawable.ic_success
                            )
                        )
                    }
                    Constants.STATUS_GAGAL -> {
                        root?.background = (ContextCompat.getDrawable(
                            fragment.requireContext(),
                            com.kioser.app.core.R.drawable.bg_half_round_red_top
                        ))
                        it.background = (ContextCompat.getDrawable(
                            fragment.requireContext(),
                            com.kioser.app.core.R.drawable.bg_circle_red
                        ))
                        it.setImageDrawable(
                            ContextCompat.getDrawable(
                                fragment.requireContext(),
                                com.kioser.app.core.R.drawable.ic_failed
                            )
                        )
                    }
                    //status info
                    else -> {
                        root?.background = (ContextCompat.getDrawable(
                            fragment.requireContext(),
                            com.kioser.app.core.R.drawable.bg_half_round_blue_top
                        ))
                        it.background = (ContextCompat.getDrawable(
                            fragment.requireContext(),
                            com.kioser.app.core.R.drawable.bg_circle_blue
                        ))
                        it.setImageDrawable(
                            ContextCompat.getDrawable(
                                fragment.requireContext(),
                                com.kioser.app.core.R.drawable.ic_failed
                            )
                        )
                    }
                }
            }

            dialog?.show()

            //auto dismiss dialog after 3 seconds
            if (autoDismiss) {
                fragment.activity?.lifecycleScope?.launch {
                    delay(4000)
                    dialog?.dismiss()
                }
            }
        }
    }

    fun dialogYesNo(title: String, noText: String, yesText: String, l: View.OnClickListener) {
        fragment?.let {
            if (dialog != null) {
                dialog?.dismiss()
            }

            val dialogView: View = fragment.layoutInflater.inflate(R.layout.dialog_yes_no, null)
            dialog = BottomSheetDialog(fragment.requireContext(), R.style.SheetDialog)
            dialog?.setContentView(dialogView)
            dialog?.setCancelable(true)

            val textview_title: TextView? = dialog?.findViewById(R.id.textview_title)
            val button_yes: Button? = dialog?.findViewById(R.id.button_yes)
            val button_no: Button? = dialog?.findViewById(R.id.button_no)
            textview_title?.text = title
            button_yes?.text = yesText
            button_no?.text = noText
            button_yes?.setOnClickListener(l)
            button_no?.setOnClickListener {
                dialog?.dismiss()
            }

            dialog?.show()
        }
    }

    fun dialogYesNo(title: Spanned, noText: String, yesText: String, l: View.OnClickListener) {
        fragment?.let {
            if (dialog != null) {
                dialog?.dismiss()
            }

            val dialogView: View = fragment.layoutInflater.inflate(R.layout.dialog_yes_no, null)
            dialog = BottomSheetDialog(fragment.requireContext(), R.style.SheetDialog)
            dialog?.setContentView(dialogView)
            dialog?.setCancelable(true)

            val textview_title: TextView? = dialog?.findViewById(R.id.textview_title)
            val button_yes: Button? = dialog?.findViewById(R.id.button_yes)
            val button_no: Button? = dialog?.findViewById(R.id.button_no)
            textview_title?.text = title
            button_yes?.text = yesText
            button_no?.text = noText
            button_yes?.setOnClickListener(l)
            button_no?.setOnClickListener {
                dialog?.dismiss()
            }

            dialog?.show()
        }
    }

    fun dialogYesNo(title: Spanned, noText: String, yesText: String, yesAction: View.OnClickListener, noAction: View.OnClickListener) {
        fragment?.let {
            if (dialog != null) {
                dialog?.dismiss()
            }

            val dialogView: View = fragment.layoutInflater.inflate(R.layout.dialog_yes_no, null)
            dialog = BottomSheetDialog(fragment.requireContext(), R.style.SheetDialog)
            dialog?.setContentView(dialogView)
            dialog?.setCancelable(true)

            val textview_title: TextView? = dialog?.findViewById(R.id.textview_title)
            val button_yes: Button? = dialog?.findViewById(R.id.button_yes)
            val button_no: Button? = dialog?.findViewById(R.id.button_no)
            textview_title?.text = title
            button_yes?.text = yesText
            button_no?.text = noText
            button_yes?.setOnClickListener(yesAction)
            button_no?.setOnClickListener(noAction)

            dialog?.show()
        }
    }

    fun dialogYesOnly(title: Spanned, yesText: String, l: View.OnClickListener, dismissable: Boolean = true) {
        fragment?.let {
            if (dialog != null) {
                dialog?.dismiss()
            }

            val dialogView: View = fragment.layoutInflater.inflate(R.layout.dialog_yes, null)
            dialog = BottomSheetDialog(fragment.requireContext(), R.style.SheetDialog)
            dialog?.setContentView(dialogView)
            dialog?.setCancelable(dismissable)

            val textview_title: TextView? = dialog?.findViewById(R.id.textview_title)
            val button_yes: Button? = dialog?.findViewById(R.id.button_yes)
            textview_title?.text = title
            button_yes?.text = yesText
            button_yes?.setOnClickListener(l)

            dialog?.show()
        }
    }

    fun dialogYesOnly(title: String, yesText: String, l: View.OnClickListener, dismissable: Boolean = true) {
        fragment?.let {
            if (dialog != null) {
                dialog?.dismiss()
            }

            val dialogView: View = fragment.layoutInflater.inflate(R.layout.dialog_yes, null)
            dialog = BottomSheetDialog(fragment.requireContext(), R.style.SheetDialog)
            dialog?.setContentView(dialogView)
            dialog?.setCancelable(dismissable)

            val textview_title: TextView? = dialog?.findViewById(R.id.textview_title)
            val button_yes: Button? = dialog?.findViewById(R.id.button_yes)
            textview_title?.text = title
            button_yes?.text = yesText
            button_yes?.setOnClickListener(l)

            dialog?.show()
        }
    }

    fun dialogYesNo(title: String, noText: String, yesText: String, noClick: View.OnClickListener, yesClick: View.OnClickListener) {
        fragment?.let {
            if (dialog != null) {
                dialog?.dismiss()
            }

            val dialogView: View = fragment.layoutInflater.inflate(R.layout.dialog_yes_no, null)
            dialog = BottomSheetDialog(fragment.requireContext(), R.style.SheetDialog)
            dialog?.setContentView(dialogView)
            dialog?.setCancelable(true)

            val textview_title: TextView? = dialog?.findViewById(R.id.textview_title)
            val button_yes: Button? = dialog?.findViewById(R.id.button_yes)
            val button_no: Button? = dialog?.findViewById(R.id.button_no)
            textview_title?.text = title
            button_yes?.text = yesText
            button_no?.text = noText
            button_yes?.setOnClickListener(yesClick)
            button_no?.setOnClickListener(noClick)

            dialog?.show()
        }
    }

    fun dialogBadConnection(title: String, description: String, l: View.OnClickListener, cancle: DialogInterface.OnCancelListener, dismissable: Boolean = true) {
        fragment?.let {
            if (dialog != null) {
                dialog?.dismiss()
            }

            val dialogView: View = fragment.layoutInflater.inflate(R.layout.dialog_bad_connection, null)
            dialog = BottomSheetDialog(fragment.requireContext(), R.style.SheetDialog)
            dialog?.setContentView(dialogView)
            dialog?.setCancelable(dismissable)
            dialog?.setOnCancelListener(cancle)

            val bottomSheetBehavior =
                dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val a = BottomSheetBehavior.from(bottomSheetBehavior!!)
            a.state = BottomSheetBehavior.STATE_EXPANDED
            dialog?.setOnShowListener {
                a.skipCollapsed = true
                a.state = BottomSheetBehavior.STATE_EXPANDED
            }

            val imageview_error: ImageView? = dialog?.findViewById(R.id.imageview_error)
            val textview_error_title: TextView? = dialog?.findViewById(R.id.textview_error_title)
            val textview_error_description: TextView? = dialog?.findViewById(R.id.textview_error_description)
            val button_error: Button? = dialog?.findViewById(R.id.button_error)

            imageview_error?.setImageResource(R.drawable.img_bs_no_connection)
            textview_error_title?.text = title
            textview_error_description?.text = description
            button_error?.text = "Coba Lagi"
            button_error?.setOnClickListener(l)

            dialog?.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun dialogWithImage(img: String?, description: String?, code: String?, button_text: String?, l: View.OnClickListener, cancle: DialogInterface.OnCancelListener, dismissable: Boolean = true) {
        fragment?.let {
            if (dialog != null) {
                dialog?.dismiss()
            }

            val dialogView: View = fragment.layoutInflater.inflate(R.layout.dialog_event, null)
            dialog = BottomSheetDialog(fragment.requireContext(), R.style.SheetDialog)
            dialog?.setContentView(dialogView)
            dialog?.setCancelable(dismissable)
            dialog?.setOnCancelListener(cancle)

            val bottomSheetBehavior =
                dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val a = BottomSheetBehavior.from(bottomSheetBehavior!!)
            a.state = BottomSheetBehavior.STATE_EXPANDED
            dialog?.setOnShowListener {
                a.skipCollapsed = true
                a.state = BottomSheetBehavior.STATE_EXPANDED
            }

            val view_sliding: View? = dialog?.findViewById(R.id.view_sliding)
            val imageview_event: ImageView? = dialog?.findViewById(R.id.imageview_event)
            val textview_event: TextView? = dialog?.findViewById(R.id.textview_event)
            val framelayout_code: FrameLayout? = dialog?.findViewById(R.id.framelayout_code)
            val textview_code_event: TextView? = dialog?.findViewById(R.id.textview_code_event)
            val button_event: Button? = dialog?.findViewById(R.id.button_event)

            if (code?.isNullOrEmpty() == true) {
                view_sliding?.visibility = View.VISIBLE
                framelayout_code?.visibility = View.GONE
                button_event?.background = ContextCompat.getDrawable(fragment.requireContext(), com.kioser.app.core.R.drawable.selector_rounded_button_secondary)
                button_event?.setTextColor(ContextCompat.getColor(fragment.requireContext(), com.kioser.app.core.R.color.colorBlue0))
            }else{
                view_sliding?.visibility = View.GONE
                framelayout_code?.visibility = View.VISIBLE
                textview_code_event?.text = code
            }

            Glide.with(fragment)
                .load(img)
                .placeholder(R.drawable.bg_rounded_blue)
                .into(imageview_event!!)
            textview_event?.text = Html.fromHtml(description,HtmlCompat.FROM_HTML_MODE_LEGACY)
            button_event?.text = button_text
            button_event?.setOnClickListener(l)

            dialog?.show()
        }
    }

    fun copyText(textCopy: String?, message: String?, isShowDialog: Boolean) {
        val clipboard = fragment?.context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("textCopy", textCopy)
        clipboard.setPrimaryClip(clip)

        if (isShowDialog) message?.let { this.dialogStatus(Constants.STATUS_SUKSES, "Disalin", it) }
    }

    fun shareLink(text: String) {
        fragment?.let {
            ShareCompat.IntentBuilder.from(fragment.requireActivity())
                .setType("text/plain")
                .setChooserTitle("Chooser title")
                .setText(text)
                .startChooser()
        }
    }

    fun requestPermission(): Boolean {
        fragment?.let {
            var result: Int
            val listPermissionsNeeded: MutableList<String> = ArrayList()
            for (p in permissions) {
                result = ContextCompat.checkSelfPermission(fragment.requireActivity(), p)
                if (result != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(p)
                }
            }
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(fragment.requireActivity(), listPermissionsNeeded.toTypedArray(), 100)
                Log.e(ContentValues.TAG, "requestPermission 1: $listPermissionsNeeded")
                return false
            } else {
                Log.e(ContentValues.TAG, "requestPermission 2: $listPermissionsNeeded")
            }
        }

        return true
    }

    fun accessContactPermission() {
        fragment?.let {
            if (requestPermission() == true) {
                try {
                    fragment.startActivityForResult(Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), 1)
                }
                //tidak ada aplikasi yang bisa buka kontak
                catch (e : ActivityNotFoundException){
                    dialogStatus(Constants.STATUS_INFO,"Aplikasi kontak tidak ditemukan","silakan install aplikasi untuk membuka kontak")
                }
            } else {
                requestPermission()
            }
        }
    }

    //efek getar"
    @SuppressLint("MissingPermission")
    fun vibrate(duration : Long = 50) {
        fragment?.let {
            vibe = fragment.activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= 26) {
                vibe?.vibrate(VibrationEffect.createOneShot(duration, 50))
            } else {
                vibe?.vibrate(duration)
            }
        }
    }

    fun setWindowFlag(bits: Int, on: Boolean, activity: Activity) {
        val win = activity?.window
        val winParams = win?.attributes
        if (on) {
            winParams?.flags = winParams?.flags?.or(bits)
        } else {
            winParams?.flags = winParams?.flags?.and(bits.inv())
        }
        win?.attributes = winParams
    }

    fun statusBarIconWhite(activity: Activity, color: Boolean) {
        *//**
         *color true = statusbar icon white,
         * color false = statusbar icon black*//*

        if (color) {
            if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
                setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true, activity)
            }
            if (Build.VERSION.SDK_INT >= 19) {
                activity?.window?.decorView?.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
            if (Build.VERSION.SDK_INT >= 21) {
                setWindowFlag(
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                        false, activity
                )
                activity?.window?.statusBarColor = Color.TRANSPARENT
            }
        } else {
            if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
                setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true, activity)
            }
            if (Build.VERSION.SDK_INT >= 19) {
                activity?.window?.decorView?.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
            if (Build.VERSION.SDK_INT >= 21) {
                setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false, activity)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    activity?.window?.statusBarColor = Color.WHITE
                } else {
                    activity?.window?.statusBarColor = Color.BLACK
                } //set status bar with black and fullscreen
            }
        }
    }

    //to monitor user flow
    fun setLog(className: String, functionName: String?, hitApi: Boolean = false, responseApi: String): String {
        *//**
         * format save log
         * 2021-08-31/17:06:24.639/com.kioser.app/https://lb5.kioser.com/v3/account?phone=082236879005 (670ms)/"responseStatus":"SUCCESS","responseCode":"1000"
         *//*
        val dateTime = SimpleDateFormat("dd-MM-yyyy/hh:mm:ss", Locale.getDefault()).format(Calendar.getInstance().time)

        value.append("$dateTime/$className/$functionName/link/responseApi \n")

        *//*if (hitApi){
            value?.append("tanggal/jam/nama_package/$tag/$functionName/linkapi/responseApi \n")
        }else{
            value = null
        }*//*

        print(value.toString())
        return value.toString()
    }

    fun saveLog(logValue: String) {
        fragment?.let {
            //file location : data/com.kioser.app/files/log/mylog.txt
            val dir = File(fragment.activity?.getFilesDir(), childDir)

            if (!dir.exists()) {
                dir.mkdir()
            }

            try {
                val file = File(dir, fileName)
                if (file.exists()) {
                    val bytes = file.length()
                    val kilobytes = bytes / 1024
                    val megabytes = kilobytes / 1024
                    //if file length > 10 MB, clear file
                    if (megabytes > 10) {
                        file.writeText("", Charsets.UTF_8)
                    }
                }

                file.appendText("$logValue\n", Charsets.UTF_8)
            } catch (e: OutOfMemoryError) {
                e.printStackTrace()
                //memory tidak cukup
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadLog(): String {
        var string = ""
        fragment?.let {
            string = try {
                //val dir = File(fragment.activity?.getFilesDir(), childDir)
                //val file = File(dir, fileName)
                val file = File(fragment.activity?.getFilesDir(), "$childDir/$fileName")
                file.readText(Charsets.UTF_8)
            } catch (e: Exception) {
                "Belum ada log"
            }
        }

        return string
    }

    fun deleteLog(): Boolean {
        var state = false
        fragment?.let {
            val file = File(fragment.activity?.filesDir, "$childDir/$fileName")
            state = file.delete()
        }

        return state
    }

    fun checkNetwork(): Boolean {
        var state = false
        fragment?.let {
            val connectivyManager =
                fragment.activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val nw = connectivyManager.activeNetwork ?: return false
                val actNw = connectivyManager.getNetworkCapabilities(nw) ?: return false
                state = when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    else -> false
                }
            } else {
                state = connectivyManager.activeNetworkInfo?.isConnected ?: false
            }
        }
        return state
    }

    fun checkNetworkFromInterceptor(): Boolean? {
        return logUtils?.statusNetwork()
    }

    //atur letter space untuk tulisan referensi
    fun setLetterSpace(textview: TextView?, size: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textview?.letterSpacing = size
        } else {
            textview?.textScaleX = size * 10
        }
    }

    fun setEditTextPlaceholder(textview: TextView?, isitext: String, useAnimation: Boolean = false, previousLength: Int = 0) {
        fragment?.let {
            if (useAnimation) {
                val fadeInAnim =
                    AnimationUtils.loadAnimation(fragment.requireActivity(), R.anim.fade_in_fast)
                val fadeOutAnim =
                    AnimationUtils.loadAnimation(fragment.requireActivity(), R.anim.fade_out_fast)
                if (previousLength == 0 && isitext.isNotBlank()) {
                    textview?.startAnimation(fadeInAnim)
                    textview?.visibility = View.VISIBLE
                } else if (previousLength == 1 && isitext.isBlank()) {
                    textview?.startAnimation(fadeOutAnim)
                    textview?.visibility = View.GONE
                }
            } else {
                if (isitext.isBlank()) {
                    textview?.visibility = View.GONE
                } else
                    textview?.visibility = View.VISIBLE
            }
        }
    }

    fun  fetchFirebaseToken(): String {
        var state = ""
        fragment?.let {
            try {
                val a = FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(
                            ContentValues.TAG,
                            "Fetching FCM registration token failed",
                            task.exception
                        )
                        return@addOnCompleteListener
                    } else {
                        Log.w(TAG, "checkFirebaseToken: ${task.isSuccessful}")
                        fragment.activity?.applicationContext?.let {
                            Freshchat.getInstance(it)
                                .setPushRegistrationToken(task.result.toString())
                        }
                        sessionManager?.setFirebaseId(task.result.toString())
                    }
                }
                Log.e(TAG, "fetchFirebaseToken: ${a.result}")
                state = a.result ?: ""
            } catch (e: Exception) {
                //sessionManager?.setFirebaseId("")
                state = ""
            }
        }

        return state
    }*/

}