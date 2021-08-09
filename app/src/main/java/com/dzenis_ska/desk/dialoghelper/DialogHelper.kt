 package com.dzenis_ska.desk.dialoghelper

import android.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.dzenis_ska.desk.MainActivity
import com.dzenis_ska.desk.R
import com.dzenis_ska.desk.accountHelper.AccountHelper
import com.dzenis_ska.desk.databinding.SignDialogBinding

class DialogHelper(private val activity: MainActivity) {

     val accHelper = AccountHelper(activity)

    fun createSignDialog(index: Int){
        val builder = AlertDialog.Builder(activity)
        val rootDialogElement = SignDialogBinding.inflate(activity.layoutInflater)

        val view = rootDialogElement.root
        builder.setView(view)
        setDialogState(index, rootDialogElement)

        val dialog = builder.create()

        rootDialogElement.edSignPassword.apply {
            afterTextChanged {
                if(it.length < 6){
                    rootDialogElement.edSignPassword.error = "activity.getString()"
                }
            }
        }

        rootDialogElement.btSignUpIn.setOnClickListener(){
            setOnClickSignUpIn(dialog, rootDialogElement, index)
        }

        rootDialogElement.btForgetP.setOnClickListener(){
            setOnClickResetPassword(dialog, rootDialogElement)
        }

        rootDialogElement.btnGoogleSignIn.setOnClickListener(){
            accHelper.signInWithGoogle()
            dialog.dismiss()
        }
        dialog.show()
    }


    private fun setOnClickResetPassword(dialog: AlertDialog?, rootDialogElement: SignDialogBinding) {
        if(rootDialogElement.edSignEmail.text.isNotEmpty()){
            activity.mAuth.sendPasswordResetEmail(rootDialogElement.edSignEmail.text.toString()).addOnCompleteListener{task ->
                if(task.isSuccessful){
                    Toast.makeText(activity, R.string.email_reset_password_was_send, Toast.LENGTH_SHORT).show()
                }
            }
            dialog?.dismiss()
        }else{
            rootDialogElement.tvDiallogMessage.text = activity.resources.getString(R.string.send_your_email_for_)
            rootDialogElement.tvDiallogMessage.visibility = View.VISIBLE
            rootDialogElement.edSignEmail.hint = activity.resources.getString(R.string.send_your_email_for_).substring(0, 20) + "..."
            rootDialogElement.edSignEmail.setHintTextColor(activity.resources.getColor(R.color.ahtung))
            Toast.makeText(activity, R.string.email_reset_password_was_send, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setOnClickSignUpIn(dialog: AlertDialog?, rootDialogElement: SignDialogBinding, index: Int) {

        dialog?.dismiss()
        if(index == DialogConst.SIGN_UP_STATE){

            accHelper.signUpWithEmail(rootDialogElement.edSignEmail.text.toString(), rootDialogElement.edSignPassword.text.toString())
        }else{
            accHelper.signInWithEmail(rootDialogElement.edSignEmail.text.toString(), rootDialogElement.edSignPassword.text.toString())
        }
    }

    private fun setDialogState(index: Int, rootDialogElement: SignDialogBinding) {
        if(index == DialogConst.SIGN_UP_STATE){
            rootDialogElement.tvSignTitle.text = activity.resources.getString(R.string.ac_sign_up)
            rootDialogElement.btSignUpIn.text = activity.resources.getString(R.string.sign_up_action)
        }else{
            rootDialogElement.tvSignTitle.text = activity.resources.getString(R.string.ac_sign_in)
            rootDialogElement.btSignUpIn.text = activity.resources.getString(R.string.sign_in_action)
            rootDialogElement.btForgetP.visibility = View.VISIBLE
        }
    }
    fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }
}