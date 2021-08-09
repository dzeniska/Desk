package com.dzenis_ska.desk.accountHelper

import android.util.Log
import android.widget.Toast
import com.dzenis_ska.desk.MainActivity
import com.dzenis_ska.desk.R
import com.dzenis_ska.desk.constants.FirebaseAuthConstants
import com.dzenis_ska.desk.dialoghelper.GoogleAccConst
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*

class AccountHelper(private val act: MainActivity) {

    private lateinit var signInClient: GoogleSignInClient

    fun signUpWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.length > 5) {
            act.mAuth.currentUser?.delete()?.addOnCompleteListener { task ->
                if(task.isSuccessful){
                    act.mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task1 ->
                        Toast.makeText(act, email, Toast.LENGTH_SHORT).show()
                        if (task1.isSuccessful) {
                            signUpWithEmailSuccessful(task1.result?.user!!)
                        } else {
                            signUpWithEmailException(task1.exception!!, email, password)
                        }
                    }
                }
            }
        } else {
            Toast.makeText(act, act.resources.getString(R.string.password_lenght), Toast.LENGTH_SHORT).show()
        }
    }

    private fun signUpWithEmailException(e: Exception, email: String, password: String) {
//                    Toast.makeText(activity, activity.resources.getString(R.string.sign_up_erroro), Toast.LENGTH_SHORT).show()
        Log.d("!!!er", e.toString())
        if (e is FirebaseAuthUserCollisionException) {
//            val exception = e as FirebaseAuthUserCollisionException
            Log.d("!!!erPas", e.errorCode)
            if (e.errorCode == FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE) {
                //Link Email
                linkEmailToG(email, password)
            }
        } else if (e is FirebaseAuthInvalidCredentialsException) {
//            val exception = e as FirebaseAuthInvalidCredentialsException
            if (e.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                Toast.makeText(act, FirebaseAuthConstants.ERROR_INVALID_EMAIL, Toast.LENGTH_SHORT).show()
            } else if (e.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) {
                Toast.makeText(act, FirebaseAuthConstants.ERROR_WEAK_PASSWORD, Toast.LENGTH_SHORT).show()
            }
        }
        if (e is FirebaseAuthWeakPasswordException) {
            if (e.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) {
                Toast.makeText(act, FirebaseAuthConstants.ERROR_WEAK_PASSWORD, Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun signUpWithEmailSuccessful(user: FirebaseUser) {
        sendEmailVerification(user)
        act.uiUpdate(user)
    }

    private fun linkEmailToG(email: String, password: String) {
        val credential = EmailAuthProvider.getCredential(email, password)
        if (act.mAuth.currentUser != null) {
            act.mAuth.currentUser?.linkWithCredential(credential)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(act, act.resources.getString(R.string.link_done), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(act, act.resources.getString(R.string.enter_to_g), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(act.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        return GoogleSignIn.getClient(act, gso)
    }

    fun signInWithGoogle() {
        signInClient = getSignInClient()
        val intent = signInClient.signInIntent
        act.startActivityForResult(intent, GoogleAccConst.GOOGLE_SIGN_IN_REQUEST_CODE)
    }

    fun signOutG() {
        getSignInClient().signOut()
    }

    fun signInFirebaseWithGoogle(token: String) {
        val credential = GoogleAuthProvider.getCredential(token, null)
        act.mAuth.currentUser?.delete()?.addOnCompleteListener { task ->
            if(task.isSuccessful){
                act.mAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(act, "Sign is done!", Toast.LENGTH_SHORT).show()
                        act.uiUpdate(task.result?.user)
                    } else {
                        Log.d("!!!Googleer", task.exception.toString())
                    }
                }
            }
        }

    }

    fun signInWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            act.mAuth.currentUser?.delete()?.addOnCompleteListener { task->
                if(task.isSuccessful){
                    act.mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task1 ->
                        //Toast.makeText(activity, email.toString(), Toast.LENGTH_SHORT).show()
                        if (task1.isSuccessful) {
                            act.uiUpdate(task1.result?.user)
                        } else {
                            signInWithEmailException(task1.exception)
                        }
                    }
                }
            }
        }
    }

    private fun signInWithEmailException(e: Exception?) {
//        Log.d("!!!er", e.toString())
        if (e is FirebaseAuthInvalidCredentialsException) {
//            Log.d("!!!er", e.toString())
            if (e.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                Toast.makeText(act, FirebaseAuthConstants.ERROR_INVALID_EMAIL, Toast.LENGTH_SHORT).show()
            } else if (e.errorCode == FirebaseAuthConstants.ERROR_WRONG_PASSWORD) {
                Toast.makeText(act, FirebaseAuthConstants.ERROR_WRONG_PASSWORD, Toast.LENGTH_SHORT).show()
            }
        } else if (e is FirebaseAuthInvalidUserException) {
//                        Log.d("!!!er", "er ${exception.errorCode}")
            if (e.errorCode == FirebaseAuthConstants.ERROR_USER_NOT_FOUND) {
                Toast.makeText(act, FirebaseAuthConstants.ERROR_USER_NOT_FOUND, Toast.LENGTH_SHORT).show()
            }
        }
        Toast.makeText(act, act.resources.getString(R.string.sign_in_erroro), Toast.LENGTH_SHORT).show()
    }

    private fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification().addOnCompleteListener() { task ->
            if (task.isSuccessful) {
                Toast.makeText(act, act.resources.getString(R.string.send_verification_done), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(act, act.resources.getString(R.string.send_verification_email_error), Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun signInAnonimously(listener: Listener){
        act.mAuth.signInAnonymously().addOnCompleteListener { task ->
            if(task.isSuccessful){
                listener.onComplete(listener)
                Toast.makeText(act, "Вы вошли как гость",Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(act, "Не удалось войти как гость",Toast.LENGTH_LONG).show()
            }
        }
    }
    interface Listener{
        fun onComplete(listener: Listener)
    }
}