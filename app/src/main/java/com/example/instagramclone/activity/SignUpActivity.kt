package com.example.instagramclone.activity

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.util.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.cometchat.pro.exceptions.CometChatException

import com.cometchat.pro.models.User

import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.CallbackListener
import com.example.instagramclone.R
import com.example.instagramclone.model.UserModel
import com.example.instagramclone.constants.Constants
import com.google.firebase.auth.ktx.auth


class SignUpActivity : AppCompatActivity(), View.OnClickListener {
    private var userAvatarIv: ImageView? = null
    private var userAvatarTxt: TextView? = null
    private var fullnameEdt: EditText? = null
    private var emailEdt: EditText? = null
    private var passwordEdt: EditText? = null
    private var confirmPasswordEdt: EditText? = null
    private var registerBtn: Button? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private var pDialog: ProgressDialog? = null

    private var uploadedUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        initViews()
        initEvents()
    }

    private fun initViews() {
        userAvatarIv = findViewById(R.id.userAvatarIv);
        userAvatarTxt = findViewById(R.id.userAvatarTxt);
        fullnameEdt = findViewById(R.id.fullnameEdt);
        emailEdt = findViewById(R.id.emailEdt);
        passwordEdt = findViewById(R.id.passwordEdt);
        confirmPasswordEdt = findViewById(R.id.confirmPasswordEdt);
        registerBtn = findViewById(R.id.registerBtn);

        pDialog = ProgressDialog(this)
        pDialog!!.setMessage("Loading")
        pDialog!!.setCanceledOnTouchOutside(false)
    }

    private fun initEvents() {
        registerBtn?.setOnClickListener(this);
        userAvatarIv?.setOnClickListener(this);
    }

    private fun chooseImage() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            )
            intent.type = "image/*"
            intent.putExtra("crop", "true")
            intent.putExtra("scale", true)
            intent.putExtra("aspectX", 16)
            intent.putExtra("aspectY", 9)
            startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE_REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                return
            }
            val uri = data?.data
            uploadedUri = uri.toString()
            if (uri != null) {
                val imageBitmap = uriToBitmap(uri)
                Glide.with(this)
                    .load(imageBitmap)
                    .circleCrop()
                    .into(userAvatarIv!!);
                userAvatarTxt?.isVisible = false
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // pick image after request permission success
                    chooseImage()
                }
            }
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap {
        return MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
    }

    private fun validate(fullName: String?, email: String?, password: String?, confirmPassword: String?): Boolean {
        if (uploadedUri == null || uploadedUri.equals(EMPTY_STRING)) {
            Toast.makeText(this@SignUpActivity, "Please upload your avatar", Toast.LENGTH_LONG).show();
            return false;
        }
        if (fullName == null || fullName.equals(EMPTY_STRING)) {
            Toast.makeText(this@SignUpActivity, "Please input your full name", Toast.LENGTH_LONG).show();
            return false;
        }
        if (email == null || email.equals(EMPTY_STRING)) {
            Toast.makeText(this@SignUpActivity, "Please input your email", Toast.LENGTH_LONG).show();
            return false;
        }
        if (password == null || password.equals(EMPTY_STRING)) {
            Toast.makeText(this@SignUpActivity, "Please input your password", Toast.LENGTH_LONG).show();
            return false;
        }
        if (confirmPassword == null || confirmPassword.equals(EMPTY_STRING)) {
            Toast.makeText(this@SignUpActivity, "Please input your confirm password", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this@SignUpActivity, "Your password and confirm password must be matched", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private fun goToLoginActivity() {
        intent = Intent(this@SignUpActivity, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun createCometChatAccount(userId: String?, fullname: String?, avatar: String?) {
        val apiKey = Constants.COMETCHAT_API_KEY // Replace with your API Key.
        val user = User()
        user.uid = userId // Replace with your uid for the user to be created.
        user.name = fullname // Replace with the name of the user
        user.avatar = avatar
        CometChat.createUser(user, apiKey, object : CallbackListener<User>() {
            override fun onSuccess(user: User) {
                pDialog!!.dismiss()
                Toast.makeText(this@SignUpActivity, fullname + " was created successfully", Toast.LENGTH_LONG).show()
                goToLoginActivity()
            }
            override fun onError(e: CometChatException) {
                pDialog!!.dismiss()
            }
        })
    }

    private fun insertFirebaseDatabase(userId: String?, fullname: String?, email: String?, avatar: String?) {
        val userModel = UserModel()
        userModel.uid = userId
        userModel.name = fullname
        userModel.email = email
        userModel.avatar = avatar
        database = Firebase.database.reference;
        database.child("users").child(userId!!).setValue(userModel)
    }

    private fun createFirebaseAccount(fullname: String?, email: String?, password: String?, avatar: String?) {
        if (email != null && password != null) {
            auth = Firebase.auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = UUID.randomUUID()
                        insertFirebaseDatabase(userId.toString(), fullname, email, avatar)
                        createCometChatAccount(userId.toString(), fullname, avatar)
                    } else {
                        pDialog!!.dismiss()
                        Toast.makeText(this@SignUpActivity, "Cannot create your account, please try again", Toast.LENGTH_LONG).show();
                    }
                }
        } else {
            pDialog!!.dismiss()
            Toast.makeText(this@SignUpActivity, "Please provide your email and password", Toast.LENGTH_LONG).show();
        }
    }

    private fun uploadUserAvatar(fullname: String?, email: String?, password: String?) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val uuid = UUID(1, 1)
        val avatarRef = storageRef.child("users/" + uuid + ".jpeg")
        userAvatarIv?.isDrawingCacheEnabled = true
        userAvatarIv?.buildDrawingCache()
        val bitmap = (userAvatarIv?.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = avatarRef.putBytes(data)
        uploadTask.addOnFailureListener {
            pDialog!!.dismiss()
            Toast.makeText(this, "Cannot upload your avatar", Toast.LENGTH_LONG).show();
        }.addOnSuccessListener { taskSnapshot ->
            avatarRef.getDownloadUrl().addOnSuccessListener(OnSuccessListener { uri ->
                if (uri != null) {
                    this.createFirebaseAccount(fullname, email, password, uri.toString())
                }
            })
        }
    }

    private fun register() {
        val fullName = fullnameEdt!!.text.toString().trim()
        val email = emailEdt!!.text.toString().trim()
        val password = passwordEdt!!.text.toString().trim()
        val confirmPassword = confirmPasswordEdt!!.text.toString().trim()
        if (validate(fullName, email, password, confirmPassword)) {
            pDialog!!.show()
            uploadUserAvatar(fullName, email, password)
        }
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.userAvatarIv -> chooseImage()
            R.id.registerBtn -> register()
            else -> {}
        }
    }

    companion object {
        const val PICK_IMAGE_REQUEST_CODE = 1000
        const val READ_EXTERNAL_STORAGE_REQUEST_CODE = 1001
        const val EMPTY_STRING = ""
    }
}