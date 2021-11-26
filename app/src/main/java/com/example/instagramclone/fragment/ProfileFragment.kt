package com.example.instagramclone.fragment

import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cometchat.pro.core.CometChat
import com.example.instagramclone.R
import com.example.instagramclone.adapter.PostAdapter
import com.example.instagramclone.adapter.ProfilePostAdapter
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
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var authorAvatarIv: ImageView? = null
    private var postIv: ImageView? = null
    private var videoIv: ImageView? = null

    private var postBottomLine: View? = null
    private var videoBottomLine: View? = null

    private var nPostsTxt: TextView? = null
    private var nFollowersTxt: TextView? = null

    private var postRv: RecyclerView? = null

    private var pDialog: ProgressDialog? = null
    private var mDatabase: DatabaseReference? = null

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
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initEvents()
        initFirebaseDatabase()
        getProfile()
        getPosts(1)
    }

    private fun initViews() {
        val view = this.requireView()
        authorAvatarIv = view.findViewById(R.id.authorAvatarIv)
        nPostsTxt = view.findViewById(R.id.nPostsTxt)
        nFollowersTxt = view.findViewById(R.id.nFollowersTxt)
        postIv = view.findViewById(R.id.postIv)
        videoIv = view.findViewById(R.id.videoIv)
        postBottomLine = view.findViewById(R.id.postBottomLine)
        videoBottomLine = view.findViewById(R.id.videoBottomLine)
        postRv = view.findViewById(R.id.profilePostRv)

        videoBottomLine?.isVisible = false

        pDialog = ProgressDialog(this.context)
        pDialog!!.setMessage("Loading")
        pDialog!!.setCanceledOnTouchOutside(false)
    }

    private fun initEvents() {
        postIv?.setOnClickListener(View.OnClickListener {
            postBottomLine?.isVisible = true
            videoBottomLine?.isVisible = false
            getPosts(1)
        })
        videoIv?.setOnClickListener(View.OnClickListener {
            videoBottomLine?.isVisible = true
            postBottomLine?.isVisible = false
            getPosts(2)
        })
    }

    private fun initFirebaseDatabase() {
        mDatabase = FirebaseDatabase.getInstance(Constants.FIREBASE_REALTIME_DATABASE_URL).getReference()
    }

    private fun getProfile() {
        val cometChatUser = CometChat.getLoggedInUser()
        if (cometChatUser != null) {
            val cometChatUserId = cometChatUser.uid
            if (cometChatUserId != null) {
                pDialog!!.show()
                mDatabase?.child("users")?.child(cometChatUserId)?.get()?.addOnSuccessListener {
                    pDialog!!.dismiss()
                    val user = it.getValue(UserModel::class.java)
                    Glide.with(this)
                        .load(user!!.avatar)
                        .circleCrop()
                        .into(authorAvatarIv!!)
                    nPostsTxt?.text = if (user.nPosts !== null) user.nPosts.toString() else "0"
                    nFollowersTxt?.text = if (user.nFollowers !== null) user.nFollowers.toString() else "0"
                }?.addOnFailureListener {
                    pDialog!!.dismiss()
                }
            }
        }
    }

    private fun initRecyclerView(posts: ArrayList<Post>?) {
        if (posts == null) {
            return;
        }
        postRv!!.layoutManager = GridLayoutManager(this.context, 3)
        val adapter = this.context?.let { ProfilePostAdapter(it, posts) }
        postRv!!.adapter = adapter
        pDialog!!.dismiss()
    }

    fun getPosts(postCategory: Int) {
        val cometChatUser = CometChat.getLoggedInUser()
        if (cometChatUser != null) {
            pDialog!!.show()
            mDatabase?.child(Constants.FIREBASE_POSTS)?.orderByChild(Constants.FIREBASE_ID_KEY)
                ?.addValueEventListener(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val posts = ArrayList<Post>()
                        if (dataSnapshot.children.count() > 0) {
                            for (postSnapshot in dataSnapshot.children) {
                                val post = postSnapshot.getValue(Post::class.java)
                                if (post != null && post.author!!.uid.equals(cometChatUser.uid) && post.postCategory == postCategory) {
                                    posts.add(post)
                                }
                            }
                        } else {
                            pDialog!!.dismiss()
                        }
                        initRecyclerView(posts)
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
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}