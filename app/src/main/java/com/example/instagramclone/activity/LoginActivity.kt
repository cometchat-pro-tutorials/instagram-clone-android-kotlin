package com.example.instagramclone.activity;

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cometchat.pro.core.AppSettings
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.User
import com.example.instagramclone.*
import com.example.instagramclone.R
import com.example.instagramclone.constants.Constants
import com.example.instagramclone.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private var emailEdt: EditText? = null
    private var passwordEdt: EditText? = null
    private var loginBtn: Button? = null
    private var registerTxt: TextView? = null
    private var pDialog: ProgressDialog? = null
    private var mAuth: FirebaseAuth? = null
    private var mDatabase: DatabaseReference? = null
    private var loggedInUser: UserModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initViews()
        initEvents()
        initFirebase()
        initFirebaseDatabase()
        initCometChat()
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        if (CometChat.getLoggedInUser() != null) {
            goToMainActivity();
        }
    }

    private fun goToMainActivity() {
        intent = Intent(this@LoginActivity, MainActivity::class.java);
        startActivity(intent);
    }

    private fun initViews() {
        emailEdt = findViewById(R.id.emailEdt)
        passwordEdt = findViewById(R.id.passwordEdt)
        loginBtn = findViewById(R.id.loginBtn)
        registerTxt = findViewById(R.id.registerTxt)
        pDialog = ProgressDialog(this)
        pDialog!!.setMessage("Loading")
        pDialog!!.setCanceledOnTouchOutside(false)
    }

    private fun initEvents() {
        loginBtn!!.setOnClickListener(this)
        registerTxt!!.setOnClickListener(this)
    }

    private fun initFirebase() {
        mAuth = FirebaseAuth.getInstance()
    }

    private fun initFirebaseDatabase() {
        mDatabase = FirebaseDatabase.getInstance(Constants.FIREBASE_REALTIME_DATABASE_URL).getReference()
    }

    private fun initCometChat() {
        val appSettings = AppSettings.AppSettingsBuilder().subscribePresenceForAllUsers().setRegion(
            Constants.COMETCHAT_REGION).build()

        CometChat.init(this, Constants.COMETCHAT_APP_ID, appSettings, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(successMessage: String) {
            }

            override fun onError(e: CometChatException) {
            }
        })
    }

    private fun loginCometChat() {
        if (loggedInUser != null && loggedInUser!!.uid != null) {
            val uid = loggedInUser!!.uid
            if (uid != null) {
                CometChat.login(uid, Constants.COMETCHAT_AUTH_KEY, object : CometChat.CallbackListener<User?>() {
                    override fun onSuccess(user: User?) {
                        pDialog!!.dismiss()
                        goToMainActivity()
                    }
                    override fun onError(e: CometChatException) {
                        pDialog!!.dismiss()
                        Toast.makeText(this@LoginActivity, "Failure to login to CometChat", Toast.LENGTH_LONG)
                    }
                })
            }
        } else {
            pDialog!!.dismiss()
        }
    }

    private fun getUserDetails(email: String?) {
        if (email == null) {
            pDialog!!.dismiss()
            return
        }
        mDatabase?.child(Constants.FIREBASE_USERS)?.orderByChild(Constants.FIREBASE_EMAIL_KEY)?.equalTo(email)?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (userSnapshot in dataSnapshot.getChildren()) {
                        loggedInUser = userSnapshot.getValue(UserModel::class.java)
                        if (loggedInUser != null) {
                            loginCometChat()
                        } else {
                            pDialog!!.dismiss()
                        }
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    pDialog!!.dismiss()
                    Toast.makeText(
                        this@LoginActivity,
                        "Cannot fetch user details information",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun callFirebaseAuthService(email: String, password: String) {
        mAuth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                getUserDetails(email)
            } else {
                pDialog!!.dismiss()
                Toast.makeText(
                    this@LoginActivity,
                    "Authentication Failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun validateUserCredentials(email: String?, password: String?): Boolean {
        if (email != null && email == EMPTY_STRING) {
            Toast.makeText(this@LoginActivity, "Please input your email", Toast.LENGTH_LONG).show()
            return false
        }
        if (password != null && password == EMPTY_STRING) {
            Toast.makeText(this@LoginActivity, "Please input your password", Toast.LENGTH_LONG)
                .show()
            return false
        }
        return true
    }

    private fun login() {
        val email = emailEdt!!.text.toString().trim { it <= ' ' }
        val password = passwordEdt!!.text.toString().trim { it <= ' ' }
        if (validateUserCredentials(email, password)) {
            pDialog!!.show()
            // call firebase authentication service.
            callFirebaseAuthService(email, password)
        }
    }

    private fun goToSignUpScreen() {
        val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
        startActivity(intent)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.loginBtn -> login()
            R.id.registerTxt -> goToSignUpScreen()
            else -> {
            }
        }
    }

    companion object {
        private const val EMPTY_STRING = ""
    }
}