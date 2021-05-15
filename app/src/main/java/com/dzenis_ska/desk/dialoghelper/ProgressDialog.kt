package com.dzenis_ska.desk.dialoghelper

import android.app.Activity
import android.app.AlertDialog
import com.dzenis_ska.desk.databinding.ProgressDialogLayoutBinding
import com.dzenis_ska.desk.databinding.SignDialogBinding

object ProgressDialog {

    fun createProgressDialog(act: Activity): AlertDialog{
        val builder = AlertDialog.Builder(act)
        val rootDialogElement = ProgressDialogLayoutBinding.inflate(act.layoutInflater)
        val view = rootDialogElement.root
        builder.setView(view)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()

        return dialog
    }
}