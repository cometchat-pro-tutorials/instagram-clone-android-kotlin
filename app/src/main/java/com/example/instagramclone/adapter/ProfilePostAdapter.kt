package com.example.instagramclone.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.VideoView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagramclone.R
import com.example.instagramclone.activity.DetailActivity
import com.example.instagramclone.model.Post

class ProfilePostAdapter(
    private val context: Context,
    private val posts: List<Post>,
) : RecyclerView.Adapter<ProfilePostAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.profile_post_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        if (post.postCategory == 2) {
            holder.postContentVv.isVisible = true
            holder.postContentIv.isVisible = false
            holder.postContentVv.setVideoPath(post.content)
            holder.postContentVv.requestFocus()
            holder.postContentVv.start()
        } else if (post.postCategory == 1) {
            holder.postContentVv.isVisible = false
            holder.postContentIv.isVisible = true
            Glide.with(context)
                .load(post.content)
                .into(holder.postContentIv);
        }
        holder.postItemFl.setOnClickListener(View.OnClickListener {
            goToDetail(post)
        })
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val postContentIv: ImageView = itemView.findViewById(R.id.postContentIv)
        val postContentVv: VideoView = itemView.findViewById(R.id.postContentVv)
        val postItemFl: FrameLayout = itemView.findViewById(R.id.postItemFl)
    }

    private fun goToDetail(post: Post?) {
        if (post?.content == null) {
            return
        }
        val intent = Intent(this.context, DetailActivity::class.java)
        intent.putExtra("postContent", post.content)
        intent.putExtra("postCategory", post.postCategory)
        this.context.startActivity(intent)
    }
}