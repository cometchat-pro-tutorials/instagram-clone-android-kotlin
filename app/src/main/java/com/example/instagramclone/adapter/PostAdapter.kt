package com.example.instagramclone.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cometchat.pro.core.CometChat
import com.example.instagramclone.R
import com.example.instagramclone.activity.DetailActivity
import com.example.instagramclone.fragment.FeedFragment
import com.example.instagramclone.model.Notification
import com.example.instagramclone.model.Post
import com.example.instagramclone.model.UserModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

class PostAdapter(
    private var feedFragment: FeedFragment,
    private var mDatabase: DatabaseReference,
    private val context: Context,
    private val posts: List<Post>,
    private val cometChatUserId: String
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        Glide.with(context)
            .load(post.author?.avatar)
            .circleCrop()
            .into(holder.authorAvatarIv);
        holder.authorNameTxt.text = post.author?.name
        if (cometChatUserId.equals(post.author!!.uid)) {
            holder.followTxt.isVisible = false
            holder.dot.isVisible = false
        } else {
            holder.followTxt.text = if (post.hasFollowed === true) "Followed" else "Follow"
        }
        if (post.postCategory == 2) {
            holder.postContentVv.isVisible = true
            holder.postContentVv?.setVideoPath(post.content)
            holder.postContentVv?.requestFocus()
            holder.postContentVv?.start()
            holder.postContentIv.isVisible = false
        } else {
            holder.postContentIv.isVisible = true
            Glide.with(context)
                .load(post.content)
                .into(holder.postContentIv);
            holder.postContentVv.isVisible = false
        }
        val heartIcon = if (post.hasLiked == true) R.drawable.heart_active else R.drawable.heart
        Glide.with(context)
            .load(heartIcon)
            .into(holder.heartIv);
        holder.followTxt.setOnClickListener(View.OnClickListener {
            toggleFollow(post)
        })
        holder.heartIv.setOnClickListener(View.OnClickListener {
            toggleLike(post)
        })
        holder.postContentIv.setOnClickListener(View.OnClickListener {
            goToDetail(post)
        })
        holder.postContentVv.setOnClickListener(View.OnClickListener {
            goToDetail(post)
        })
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val authorAvatarIv: ImageView = itemView.findViewById(R.id.authorAvatarIv)
        val authorNameTxt: TextView = itemView.findViewById(R.id.authorNameTxt)
        val followTxt: TextView = itemView.findViewById(R.id.followTxt)
        val postContentIv: ImageView = itemView.findViewById(R.id.postContentIv)
        val heartIv: ImageView = itemView.findViewById(R.id.heartIv)
        val dot: View = itemView.findViewById(R.id.dot)
        val postContentVv: VideoView = itemView.findViewById(R.id.postContentVv)
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

    private fun createNotification(notification: Notification) {
        if (notification?.id == null) {
            return;
        }
        mDatabase = Firebase.database.reference;
        mDatabase.child("notifications").child(notification.id!!).setValue(notification)
    }

    private fun updateFollow(post: Post?, cometChatUserId: String?) {
        if (post == null || cometChatUserId == null) {
            return
        }
        val parent = this.context
        mDatabase?.child("users")?.child(post.author?.uid!!)?.get()?.addOnSuccessListener {
            val user = it.getValue(UserModel::class.java)
            val updatedFollow = ArrayList<String>()
            if (user?.followers == null || user?.followers!!.size == 0) {
                updatedFollow.add(cometChatUserId)
            } else if (post.hasFollowed == true) {
                for (follower in user.followers!!) {
                    if (!follower.equals(cometChatUserId)) {
                        updatedFollow.add(follower)
                    }
                }
            } else if (post.hasFollowed == false) {
                for (follower in user.followers!!) {
                    updatedFollow.add(follower)
                }
                updatedFollow.add(cometChatUserId)
                if (post.author!!.uid !== cometChatUserId) {
                    val cometChatUser = CometChat.getLoggedInUser()
                    val notificationId = UUID.randomUUID()
                    val notificationMessage = cometChatUser.name + " has followed you"
                    val notificationImage = cometChatUser.avatar
                    val receiverId = post.author!!.uid
                    val notification = Notification()
                    notification.notificationMessage = notificationMessage
                    notification.notificationImage = notificationImage
                    notification.id = notificationId.toString()
                    notification.receiverId = receiverId
                    createNotification(notification)
                }
            }
            user?.followers = updatedFollow
            user?.nFollowers = updatedFollow.size
            mDatabase = Firebase.database.reference;
            mDatabase.child("users").child(post.author!!.uid!!).setValue(user)
            feedFragment.getPosts()
        }?.addOnFailureListener {
        }
    }

    private fun toggleFollow(post: Post?) {
        if (post == null) {
            return;
        }
        updateFollow(post, cometChatUserId)
    }

    private fun updateLikes(post: Post?, cometChatUserId: String?) {
        if (post == null || cometChatUserId == null) {
            return
        }
        val updatedLikes = ArrayList<String>()
        if (post.likes == null || post.likes!!.size == 0) {
            updatedLikes.add(cometChatUserId)
        } else if (post.hasLiked == true) {
            for (like in post.likes!!) {
                if (!like.equals(cometChatUserId)) {
                    updatedLikes.add(like)
                }
            }
        } else if (post.hasLiked == false) {
            for (like in post.likes!!) {
                updatedLikes.add(like)
            }
            updatedLikes.add(cometChatUserId)
            if (post.author!!.uid !== cometChatUserId) {
                val cometChatUser = CometChat.getLoggedInUser()
                val notificationId = UUID.randomUUID()
                val notificationMessage = cometChatUser.name + " has liked your post"
                val notificationImage = cometChatUser.avatar
                val receiverId = post.author!!.uid
                val notification = Notification()
                notification.notificationMessage = notificationMessage
                notification.notificationImage = notificationImage
                notification.id = notificationId.toString()
                notification.receiverId = receiverId
                createNotification(notification)
            }
        }
        post.likes = updatedLikes
        post.nLikes = updatedLikes.size
        mDatabase = Firebase.database.reference;
        mDatabase.child("posts").child(post.id!!).setValue(post)
    }

    private fun toggleLike(post: Post?) {
        if (post == null) {
            return;
        }
        updateLikes(post, cometChatUserId)
    }
}