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

class AccountHelper(private val activity: MainActivity) {

    private lateinit var signInClient: GoogleSignInClient

    fun signUpWithEmail(email: String, password: String) {

        if (email.isNotEmpty() && password.length > 5) {
            activity.mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                Toast.makeText(activity, email, Toast.LENGTH_SHORT).show()
                if (task.isSuccessful) {
                    sendEmailVerification(task.result?.user!!)
                    activity.uiUpdate(task.result?.user)
                } else {
//                    Toast.makeText(activity, activity.resources.getString(R.string.sign_up_erroro), Toast.LENGTH_SHORT).show()
                    Log.d("!!!er", task.exception.toString())
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        val exception = task.exception as FirebaseAuthUserCollisionException
                        Log.d("!!!erPas", exception.errorCode)
                        if (exception.errorCode == FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE) {
//                            Toast.makeText(activity, FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE, Toast.LENGTH_SHORT).show()
                            //Link Email
                            linkEmailToG(email, password)
                        }
                    } else if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        val exception = task.exception as FirebaseAuthInvalidCredentialsException
                        if (exception.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                            Toast.makeText(activity, FirebaseAuthConstants.ERROR_INVALID_EMAIL, Toast.LENGTH_SHORT).show()
                        } else if (exception.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) {
                            Toast.makeText(activity, FirebaseAuthConstants.ERROR_WEAK_PASSWORD, Toast.LENGTH_SHORT).show()
                        }
                    }
                    if (task.exception is FirebaseAuthWeakPasswordException) {
                        val exception = task.exception as FirebaseAuthWeakPasswordException
                        if (exception.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) {
                            Toast.makeText(activity, FirebaseAuthConstants.ERROR_WEAK_PASSWORD, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } else {
            Toast.makeText(activity, activity.resources.getString(R.string.password_lenght), Toast.LENGTH_SHORT).show()
        }
    }

    private fun linkEmailToG(email: String, password: String) {
        val credential = EmailAuthProvider.getCredential(email, password)
        if (activity.mAuth.currentUser != null) {
            activity.mAuth.currentUser?.linkWithCredential(credential)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(activity, activity.resources.getString(R.string.link_done), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(activity, activity.resources.getString(R.string.enter_to_g), Toast.LENGTH_SHORT).show()
                }
            }
        } else {

        }
    }

    private fun getSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        return GoogleSignIn.getClient(activity, gso)
    }

    fun signInWithGoogle() {
        signInClient = getSignInClient()
        val intent = signInClient.signInIntent
        activity.startActivityForResult(intent, GoogleAccConst.GOOGLE_SIGN_IN_REQUEST_CODE)
    }

    fun signOutG() {
        getSignInClient().signOut()
    }

    fun signInFirebaseWithGoogle(token: String) {
        val credential = GoogleAuthProvider.getCredential(token, null)
        activity.mAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(activity, "Sign is done!", Toast.LENGTH_SHORT).show()
                activity.uiUpdate(task.result?.user)
            } else {
                Log.d("!!!Googleer", task.exception.toString())
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            activity.mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                //Toast.makeText(activity, email.toString(), Toast.LENGTH_SHORT).show()
                if (task.isSuccessful) {
                    activity.uiUpdate(task.result?.user)
                } else {
                    Log.d("!!!er", task.exception.toString())
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Log.d("!!!er", task.exception.toString())
                        val exception = task.exception as FirebaseAuthInvalidCredentialsException
                        if (exception.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                            Toast.makeText(activity, FirebaseAuthConstants.ERROR_INVALID_EMAIL, Toast.LENGTH_SHORT).show()
                        } else if (exception.errorCode == FirebaseAuthConstants.ERROR_WRONG_PASSWORD) {
                            Toast.makeText(activity, FirebaseAuthConstants.ERROR_WRONG_PASSWORD, Toast.LENGTH_SHORT).show()
                        }
                    } else if (task.exception is FirebaseAuthInvalidUserException) {
                        val exception = task.exception as FirebaseAuthInvalidUserException
//                        Log.d("!!!er", "er ${exception.errorCode}")
                        if (exception.errorCode == FirebaseAuthConstants.ERROR_USER_NOT_FOUND) {
                            Toast.makeText(activity, FirebaseAuthConstants.ERROR_USER_NOT_FOUND, Toast.LENGTH_SHORT).show()
                        }
                    }
                    Toast.makeText(activity, activity.resources.getString(R.string.sign_in_erroro), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification().addOnCompleteListener() { task ->
            if (task.isSuccessful) {
                Toast.makeText(activity, activity.resources.getString(R.string.send_verification_done), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, activity.resources.getString(R.string.send_verification_email_error), Toast.LENGTH_SHORT).show()
            }
        }
    }
}