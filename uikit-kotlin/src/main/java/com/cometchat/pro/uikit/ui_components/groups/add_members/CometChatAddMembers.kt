package com.cometchat.pro.uikit.ui_components.groups.add_members

import com.cometchat.pro.uikit.ui_components.shared.cometchatUsers.CometChatUsersAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.core.UsersRequest
import com.cometchat.pro.core.UsersRequest.UsersRequestBuilder
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.GroupMember
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.R
import com.google.android.material.appbar.MaterialToolbar
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_resources.utils.recycler_touch.ClickListener
import com.cometchat.pro.uikit.ui_resources.utils.recycler_touch.RecyclerTouchListener
import com.cometchat.pro.uikit.ui_resources.utils.sticker_header.StickyHeaderDecoration
import com.cometchat.pro.uikit.ui_components.users.user_details.CometChatUserDetailScreenActivity
import com.cometchat.pro.uikit.ui_resources.utils.ErrorMessagesUtils
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import java.util.*

class CometChatAddMembers : Fragment() {
    private var userListAdapter: CometChatUsersAdapter? = null
    private var usersRequest: UsersRequest? = null
    private var rvUserList: RecyclerView? = null
    private var etSearch: EditText? = null
    private var clearSearch: ImageView? = null
    private var guid: String? = null
    private var groupMembersUids: ArrayList<String>? = ArrayList()
    private var groupName: String? = null
    private var fontUtils: FontUtils? = null
    private var toolbar: MaterialToolbar? = null
    var groupMembers: List<GroupMember> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleArguments()
        fontUtils = FontUtils.getInstance(activity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_cometchat_add_member, container, false)
        init(view)
        return view
    }

    fun init(view: View) {
        // Inflate the layout
        setHasOptionsMenu(true)
        rvUserList = view.findViewById(R.id.rv_user_list)
        etSearch = view.findViewById(R.id.search_bar)
        toolbar = view.findViewById(R.id.add_member_toolbar)
        setToolbar(toolbar)
        checkDarkMode()
        fetchUsers()
        clearSearch = view.findViewById(R.id.clear_search)
        etSearch!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.isNotEmpty()) clearSearch!!.visibility = View.VISIBLE
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        etSearch!!.setOnEditorActionListener(OnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                searchUser(textView.text.toString())
                clearSearch!!.visibility = View.VISIBLE
                return@OnEditorActionListener true
            }
            false
        })
        clearSearch!!.setOnClickListener(View.OnClickListener {
            etSearch!!.setText("")
            clearSearch!!.visibility = View.GONE
            searchUser(etSearch!!.text.toString())
            val inputMethodManager = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            // Hide the soft keyboard
            inputMethodManager.hideSoftInputFromWindow(etSearch!!.windowToken, 0)
        })
        rvUserList!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    fetchUsers()
                }
            }
        })
        rvUserList!!.addOnItemTouchListener(RecyclerTouchListener(context, rvUserList!!, object : ClickListener() {
            override fun onClick(var1: View, var2: Int) {
                val user = var1.getTag(R.string.user) as User
                if (activity != null) {
                    val intent = Intent(activity, CometChatUserDetailScreenActivity::class.java)
                    intent.putExtra(UIKitConstants.IntentStrings.UID, user.uid)
                    intent.putExtra(UIKitConstants.IntentStrings.NAME, user.name)
                    intent.putExtra(UIKitConstants.IntentStrings.AVATAR, user.avatar)
                    intent.putExtra(UIKitConstants.IntentStrings.STATUS, user.status)
                    intent.putExtra(UIKitConstants.IntentStrings.IS_BLOCKED_BY_ME, user.isBlockedByMe)
                    intent.putExtra(UIKitConstants.IntentStrings.TYPE, CometChatConstants.RECEIVER_TYPE_GROUP)
                    intent.putExtra(UIKitConstants.IntentStrings.GUID, guid)
                    intent.putExtra(UIKitConstants.IntentStrings.IS_ADD_MEMBER, true)
                    intent.putExtra(UIKitConstants.IntentStrings.GROUP_NAME, groupName)
                    activity!!.finish()
                    startActivity(intent)
                }
            }

            override fun onLongClick(var1: View, var2: Int) {}
        }))
    }

    private fun checkDarkMode() {
        if (Utils.isDarkMode(context!!)) {
            toolbar!!.setTitleTextColor(resources.getColor(R.color.textColorWhite))
        } else {
            toolbar!!.setTitleTextColor(resources.getColor(R.color.primaryTextColor))
        }
    }

    private fun setToolbar(toolbar: MaterialToolbar?) {
        if (Utils.changeToolbarFont(toolbar!!) != null) {
            Utils.changeToolbarFont(toolbar)!!.typeface = fontUtils!!.getTypeFace(FontUtils.robotoMedium)
        }
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun handleArguments() {
        if (arguments != null) {
            guid = arguments!!.getString(UIKitConstants.IntentStrings.GUID)
            groupName = arguments!!.getString(UIKitConstants.IntentStrings.GROUP_NAME)
            groupMembersUids = arguments!!.getStringArrayList(UIKitConstants.IntentStrings.GROUP_MEMBER)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (activity != null) activity!!.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun fetchUsers() {
        if (usersRequest == null) {
            val LIMIT = 30
            usersRequest = UsersRequestBuilder().setLimit(LIMIT).build()
        }
        makeUserRequest(usersRequest)
    }

    private fun searchUser(s: String) {
        val usersRequest = UsersRequestBuilder().setSearchKeyword(s).setLimit(100).build()
        usersRequest.fetchNext(object : CallbackListener<List<User?>?>() {
            override fun onSuccess(users: List<User?>?) {
                if (userListAdapter != null) userListAdapter!!.searchUser(users!!)
            }

            override fun onError(e: CometChatException) {
                ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
                Log.d(TAG, "onError: " + e.message)
            }
        })
    }

    private fun makeUserRequest(usersRequest: UsersRequest?) {
        usersRequest!!.fetchNext(object : CallbackListener<List<User>>() {
            override fun onSuccess(users: List<User>) {
                if (users.isNotEmpty()) {
                    val userArrayList = ArrayList<User>()
                    for (user in users) {
                        if (!groupMembersUids!!.contains(user.uid)) {
                            userArrayList.add(user)
                        }
                    }
                    setAdapter(userArrayList)
                }
            }

            override fun onError(e: CometChatException) {
                ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
                Log.e(TAG, "onError: " + e.message)
            }
        })
    }

    private fun setAdapter(users: List<User>) {
        if (userListAdapter == null) {
            userListAdapter = CometChatUsersAdapter(context!!)
            val stickyHeaderDecoration = StickyHeaderDecoration(userListAdapter!!)
            rvUserList!!.addItemDecoration(stickyHeaderDecoration, 0)
            rvUserList!!.adapter = userListAdapter
        } else {
            userListAdapter!!.updateList(Utils.userSort(users)!!)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
        usersRequest = null
        userListAdapter = null
        fetchUsers()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: ")
    }

    companion object {
        private const val TAG = "CometChatAddMember"
    }
}