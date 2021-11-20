package com.example.instagramclone.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.VideoView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.instagramclone.R

class DetailActivity : AppCompatActivity() {
    private var postContentIv: ImageView? = null
    private var postContentVv: VideoView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        initViews()
    }

    private fun initViews() {
        postContentIv = findViewById(R.id.postContentIv)
        postContentVv = findViewById(R.id.postContentVv)
        val postContent = intent?.getStringExtra("postContent").toString()
        val postCategory = intent?.getIntExtra("postCategory", 0)
        if (postCategory == 2) {
            postContentVv?.isVisible = true
            postContentIv?.isVisible = false
            postContentVv?.setVideoPath(postContent)
            postContentVv?.requestFocus()
            postContentVv?.start()
        } else if (postCategory == 1) {
            postContentIv?.isVisible = true
            postContentVv?.isVisible = false
            Glide.with(this)
                .load(postContent)
                .into(postContentIv!!);
        }
    }
}