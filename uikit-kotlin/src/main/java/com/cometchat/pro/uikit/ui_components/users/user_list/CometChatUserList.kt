package com.cometchat.pro.uikit.ui_components.users.user_list

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.core.UsersRequest
import com.cometchat.pro.core.UsersRequest.UsersRequestBuilder
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.shared.cometchatUsers.CometChatUsers
import com.cometchat.pro.uikit.ui_components.shared.cometchatUsers.CometChatUsersAdapter
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_resources.utils.ErrorMessagesUtils
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import com.cometchat.pro.uikit.ui_resources.utils.item_clickListener.OnItemClickListener
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction
import com.cometchat.pro.uikit.ui_settings.UIKitSettings
import com.cometchat.pro.uikit.ui_settings.enum.UserMode
import com.facebook.shimmer.ShimmerFrameLayout
import java.util.*

/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ /*

* Purpose - CometChatUserList class is a fragment used to display list of users and perform certain action on click of item.
            It also provide search bar to search user from the list.

* @author - CometChat

* @version - v1.0

* Created on - 20th December 2019

* Modified on  - 23rd March 2020

*/
class CometChatUserList constructor() : Fragment() {
    private var isTitleVisible: Boolean = true
    private val LIMIT: Int = 30
    private var c: Context? = null
    private val isSearching: Boolean = false
    private val userListAdapter: CometChatUsersAdapter? = null
    private var usersRequest // Use to fetch users
            : UsersRequest? = null
    private var rvUserList // Use to display list of users
            : CometChatUsers? = null
    private var etSearch // Use to perform search operation on list of users.
            : EditText? = null
    private var clearSearch //Use to clear the search operation performed on list.
            : ImageView? = null
    private var shimmerFrameLayout: ShimmerFrameLayout? = null
    private var title: TextView? = null
    private var rlSearchBox: RelativeLayout? = null
    private var noUserLayout: LinearLayout? = null
    private val userList: MutableList<User> = ArrayList()
    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                     savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_cometchat_userlist, container, false)
        title = view.findViewById(R.id.tv_title)
        title?.typeface = FontUtils.getInstance(activity).getTypeFace(FontUtils.robotoMedium)
        rvUserList = view.findViewById(R.id.rv_user_list)
        noUserLayout = view.findViewById(R.id.no_user_layout)
        etSearch = view.findViewById(R.id.search_bar)
        clearSearch = view.findViewById(R.id.clear_search)
        rlSearchBox = view.findViewById(R.id.rl_search_box)
        shimmerFrameLayout = view.findViewById(R.id.shimmer_layout)

        val fragment = fragmentManager?.findFragmentByTag("startChat")
        if (fragment != null && fragment.isVisible) {
            Log.e(TAG, "onCreateView: user " + fragment.toString())
            title?.visibility = View.GONE
        }

        FeatureRestriction.isUserSearchEnabled(object : FeatureRestriction.OnSuccessListener {
            override fun onSuccess(p0: Boolean) {
                if (!p0) {
                    etSearch?.visibility = View.GONE
                    clearSearch?.visibility = View.GONE
                }
            }

        })
        if (Utils.isDarkMode(context!!)) {
            title!!.setTextColor(resources.getColor(R.color.textColorWhite))
        } else {
            title!!.setTextColor(resources.getColor(R.color.primaryTextColor))
        }
        etSearch!!.addTextChangedListener(object : TextWatcher {
            public override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            public override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            public override fun afterTextChanged(editable: Editable) {
                if (editable.isEmpty()) {
                    // if etSearch is empty then fetch all users.
                    usersRequest = null
                    rvUserList!!.clear()
                    fetchUsers()
                } else {
                    // Search users based on text in etSearch field.
                    searchUser(editable.toString())
                }
            }
        })
        etSearch!!.setOnEditorActionListener(object : OnEditorActionListener {
            public override fun onEditorAction(textView: TextView, i: Int, keyEvent: KeyEvent?): Boolean {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    searchUser(textView.text.toString())
                    clearSearch!!.visibility = View.VISIBLE
                    return true
                }
                return false
            }
        })
        clearSearch!!.setOnClickListener {
            etSearch!!.setText("")
            clearSearch!!.visibility = View.GONE
            searchUser(etSearch?.text.toString())
            val inputMethodManager: InputMethodManager = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            // Hide the soft keyboard
            inputMethodManager.hideSoftInputFromWindow(etSearch!!.windowToken, 0)
        }


        // Uses to fetch next list of user if rvUserList (RecyclerView) is scrolled in upward direction.
        rvUserList!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            public override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    fetchUsers()
                }
            }
        })

        // Used to trigger event on click of user item in rvUserList (RecyclerView)
        rvUserList!!.setItemClickListener(object : OnItemClickListener<User?>() {
            public override fun OnItemClick(t: Any, position: Int) {
                if (events != null) events!!.OnItemClick(t as User, position)
            }
        })
        return view
    }


    private fun stopHideShimmer() {
        shimmerFrameLayout?.stopShimmer()
        shimmerFrameLayout?.visibility = View.GONE
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchUsers()
        if (isTitleVisible)
            title?.visibility = View.VISIBLE
        else title?.visibility = View.GONE
    }

    /**
     * This method is used to retrieve list of users present in your App_ID.
     * For more detail please visit our official documentation []//prodocs.cometchat.com/docs/android-users-retrieve-users.section-retrieve-list-of-users" ">&quot;https://prodocs.cometchat.com/docs/android-users-retrieve-users#section-retrieve-list-of-users&quot;
     *
     * @see UsersRequest
     */
    private fun fetchUsers() {
        if (usersRequest == null) {
            if (UIKitSettings.userInMode == UserMode.FRIENDS) usersRequest = UsersRequestBuilder().setLimit(30)
                    .friendsOnly(true).build()
            else if (UIKitSettings.userInMode == UserMode.ALL_USER) usersRequest = UsersRequestBuilder().setLimit(30).build()
        }
        usersRequest!!.fetchNext(object : CallbackListener<List<User>>() {
            public override fun onSuccess(users: List<User>) {
                Log.e(TAG, "onfetchSuccess: " + users.size)
                userList.addAll(users)
                stopHideShimmer()
                rvUserList!!.setUserList(users) // set the users to rvUserList i.e CometChatUserList Component.
                if (userList.size == 0) {
                    noUserLayout!!.visibility = View.VISIBLE
                    rvUserList!!.visibility = View.GONE
                } else {
                    rvUserList!!.visibility = View.VISIBLE
                    noUserLayout!!.visibility = View.GONE
                }
            }

            public override fun onError(e: CometChatException) {
                Log.e(TAG, "onError: " + e.message)
                stopHideShimmer()
                if (activity != null) ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
            }
        })
    }

    /**
     * This method is used to search users present in your App_ID.
     * For more detail please visit our official documentation []//prodocs.cometchat.com/docs/android-users-retrieve-users.section-retrieve-list-of-users" ">&quot;https://prodocs.cometchat.com/docs/android-users-retrieve-users#section-retrieve-list-of-users&quot;
     *
     * @param s is a string used to get users matches with this string.
     * @see UsersRequest
     */
    private fun searchUser(s: String) {
        val usersRequest: UsersRequest = UsersRequestBuilder().setSearchKeyword(s).setLimit(100).build()
        usersRequest.fetchNext(object : CallbackListener<List<User?>?>() {
            public override fun onSuccess(users: List<User?>?) {
                rvUserList!!.searchUserList(users) // set the users to rvUserList i.e CometChatUserList Component.
            }

            public override fun onError(e: CometChatException) {
                ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
            }
        })
    }

    public override fun onResume() {
        super.onResume()
    }

    public override fun onPause() {
        super.onPause()
    }

    public override fun onAttach(context: Context) {
        super.onAttach(context)
        this.c = context
    }

    fun setTitleVisible(isVisible: Boolean) {
        isTitleVisible = isVisible
    }

    companion object {
        private val TAG: String = "CometChatUserListScreen"
        private var events: OnItemClickListener<Any>? = null

        /**
         *
         * @param onItemClickListener An object of `OnItemClickListener<T>` abstract class helps to initialize with events
         * to perform onItemClick & onItemLongClick,
         * @see OnItemClickListener
         */
        @JvmStatic
        fun setItemClickListener(onItemClickListener: OnItemClickListener<Any>?) {
            events = onItemClickListener
        }
    }
}