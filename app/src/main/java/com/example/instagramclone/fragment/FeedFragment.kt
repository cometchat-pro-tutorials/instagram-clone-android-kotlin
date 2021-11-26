package com.example.instagramclone.fragment

import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.core.CometChat
import com.example.instagramclone.R
import com.example.instagramclone.adapter.PostAdapter
import com.example.instagramclone.constants.Constants
import com.example.instagramclone.model.Post
import com.example.instagramclone.model.UserModel
import com.google.firebase.database.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FeedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FeedFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var postRv: RecyclerView? = null

    private var pDialog: ProgressDialog? = null
    private var mDatabase: DatabaseReference? = null
    private var posts: ArrayList<Post>? = null
    private var adapter: PostAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initFirebaseDatabase()
        getPosts()
    }

    private fun initViews() {
        pDialog = ProgressDialog(this.context)
        pDialog!!.setMessage("Loading")
        pDialog!!.setCanceledOnTouchOutside(false)

        postRv = requireView().findViewById(R.id.postRv)
    }

    private fun initFirebaseDatabase() {
        mDatabase =
            FirebaseDatabase.getInstance(Constants.FIREBASE_REALTIME_DATABASE_URL).getReference()
    }

    private fun initRecyclerView(posts: ArrayList<Post>?) {
        if (posts == null || posts.size == 0) {
            return;
        }
        postRv!!.layoutManager = LinearLayoutManager(this.context)
        val cometChatUser = CometChat.getLoggedInUser()
        val cometChatUserId = cometChatUser.uid
        adapter = this.context?.let { PostAdapter(this, mDatabase!!, it, posts, cometChatUserId) }
        postRv!!.adapter = adapter
        pDialog!!.dismiss()
    }

    private fun hasLiked(post: Post?, id: String?) {
        if (post?.likes == null || post?.likes?.size === 0 || id == null) {
            post?.hasLiked = false
            return;
        }
        for (like in post.likes!!) {
            if (like.equals(id)) {
                post.hasLiked = true;
                return;
            }
        }
        post.hasLiked = false
    }

    private fun hasFollowed(index: Int?, post: Post?, id: String?) {
        if (post?.author == null || post.author?.uid == null || id == null) {
            return;
        }
        val userId = post.author?.uid
        mDatabase?.child("users")?.child(userId!!)?.get()?.addOnSuccessListener {
            val user = it.getValue(UserModel::class.java)
            if (user?.followers == null || user.followers?.size == 0) {
                post.hasFollowed = false
            } else {
                for (follower in user.followers!!) {
                    if (follower.equals(id)) {
                        post.hasFollowed = true
                    }
                }
            }
            posts!!.set(index!!, post)
            if (adapter != null) {
                adapter!!.notifyDataSetChanged()
            }
        }?.addOnFailureListener {
        }
    }

    private fun updateFollow() {
        val cometChatUser = CometChat.getLoggedInUser()
        val cometChatUserId = cometChatUser.uid
        for ((index, post) in posts!!.withIndex()) {
            hasFollowed(index, post, cometChatUserId)
        }
    }

    fun getPosts() {
        val cometChatUser = CometChat.getLoggedInUser()
        if (cometChatUser != null) {
            pDialog!!.show()
            mDatabase?.child(Constants.FIREBASE_POSTS)?.orderByChild(Constants.FIREBASE_ID_KEY)
                ?.addValueEventListener(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        posts = ArrayList()
                        if (dataSnapshot.children.count() > 0) {
                            for (postSnapshot in dataSnapshot.children) {
                                val post = postSnapshot.getValue(Post::class.java)
                                if (post != null) {
                                    hasLiked(post, cometChatUser.uid)
                                    posts!!.add(post)
                                }
                            }
                            initRecyclerView(posts)
                            updateFollow()
                        } else {
                            pDialog!!.dismiss()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        pDialog!!.dismiss()
                        Toast.makeText(
                            context,
                            "Cannot fetch list of posts",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FeedFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FeedFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}