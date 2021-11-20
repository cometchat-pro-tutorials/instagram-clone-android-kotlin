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
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.cometchat.pro.core.CometChat
import com.example.instagramclone.R
import com.example.instagramclone.model.Post
import com.example.instagramclone.model.UserModel
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.util.*
import java.io.File

import java.io.FileInputStream
import java.net.URI


class CreateActivity : AppCompatActivity(), View.OnClickListener {
    private var uploadContainer: LinearLayoutCompat? = null
    private var postIv: ImageView? = null
    private var videoView: VideoView? = null
    private var createPostBtn: Button? = null

    private var pDialog: ProgressDialog? = null

    private lateinit var database: DatabaseReference
    private var uploadedUri: Uri? = null
    private var isVideo: Boolean? = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
        initViews()
        initEvents()
    }

    private fun initViews() {
        postIv = findViewById(R.id.postIv)
        uploadContainer = findViewById(R.id.uploadContainer)
        createPostBtn = findViewById(R.id.createPostBtn)
        videoView = findViewById(R.id.videoView)

        pDialog = ProgressDialog(this)
        pDialog!!.setMessage("Loading")
        pDialog!!.setCanceledOnTouchOutside(false)
    }

    private fun initEvents() {
        postIv!!.setOnClickListener(this)
        videoView!!.setOnClickListener(this)
        createPostBtn!!.setOnClickListener(this)
    }

    private fun chooseImage() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            )
            intent.type = "*/*"
            startActivityForResult(intent, SignUpActivity.PICK_IMAGE_REQUEST_CODE)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                SignUpActivity.READ_EXTERNAL_STORAGE_REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SignUpActivity.PICK_IMAGE_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                return
            }
            val uri = data?.data
            if (uri.toString().indexOf("video") != -1) {
                uploadContainer?.isVisible= false
                postIv?.isVisible = false
                videoView?.isVisible = true
                videoView?.setVideoURI(uri)
                videoView?.requestFocus()
                videoView?.start()
                uploadedUri = uri
                isVideo = true
            } else if (uri != null) {
                uploadContainer?.isVisible= false
                videoView?.isVisible = false
                postIv?.isVisible = true;
                val imageBitmap = uriToBitmap(uri)
                Glide.with(this)
                    .load(imageBitmap)
                    .centerCrop()
                    .into(postIv!!);
                uploadContainer?.isVisible = false
                uploadedUri = uri
                isVideo = false
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            SignUpActivity.READ_EXTERNAL_STORAGE_REQUEST_CODE -> {
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

    private fun goToMainActivity() {
        intent = Intent(this@CreateActivity, MainActivity::class.java)
        startActivity(intent)
    }

    private fun createFirebasePost(uuid: String?, postContent: String?) {
        val cometChatUser = CometChat.getLoggedInUser()
        if (cometChatUser != null) {
            val author = UserModel()
            author.uid = cometChatUser.uid
            author.avatar = cometChatUser.avatar
            author.name = cometChatUser.name

            val post = Post()
            post.id = uuid
            post.content = postContent
            post.likes = ArrayList()
            post.nLikes = 0
            post.author = author
            post.postCategory = if (isVideo == true) 2 else 1

            database = Firebase.database.reference;
            database.child("posts").child(uuid!!).setValue(post)

            database?.child("users")?.child(cometChatUser.uid)?.get()?.addOnSuccessListener {
                pDialog!!.dismiss()
                val user = it.getValue(UserModel::class.java)
                user!!.nPosts = if (user.nPosts != null) user.nPosts!!.plus(1) else 1
                database.child("users").child(cometChatUser.uid!!).setValue(user)
            }?.addOnFailureListener {
                pDialog!!.dismiss()
            }
            Toast.makeText(
                this@CreateActivity,
                "Your post was created successfully",
                Toast.LENGTH_LONG
            ).show()
            goToMainActivity()
        } else {
            Toast.makeText(
                this@CreateActivity,
                "Cannot load your cometchat account",
                Toast.LENGTH_LONG
            ).show()
            pDialog!!.dismiss()
        }
    }

    private fun getUploadedImage(): ByteArray {
        postIv?.isDrawingCacheEnabled = true
        postIv?.buildDrawingCache()
        val bitmap = (postIv?.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        return baos.toByteArray()
    }

    private fun uploadPostContent() {
        if (uploadedUri == null) {
            Toast.makeText(this@CreateActivity, "Please upload the post image or video", Toast.LENGTH_LONG).show()
            return
        }
        pDialog!!.show()
        val storage = Firebase.storage
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val refLink = if (isVideo == true) "posts/" + uuid + ".mp4" else "posts/" + uuid + ".jpeg"
        val postRef = storageRef.child(refLink)
        val uploadTask = if (isVideo == true) postRef.putFile(uploadedUri!!) else postRef.putBytes(getUploadedImage())
        uploadTask.addOnFailureListener {
            pDialog!!.dismiss()
            Toast.makeText(this, "Cannot upload your post", Toast.LENGTH_LONG).show();
        }.addOnSuccessListener { taskSnapshot ->
            postRef.getDownloadUrl().addOnSuccessListener(OnSuccessListener { uri ->
                if (uri != null) {
                    this.createFirebasePost(uuid.toString(), uri.toString())
                }
            })
        }
    }

    private fun createPost() {
        val cometChatUser = CometChat.getLoggedInUser();
        if (cometChatUser != null) {
            uploadPostContent()
        }
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.videoView -> chooseImage()
            R.id.postIv -> chooseImage()
            R.id.createPostBtn -> createPost()
        }
    }
}