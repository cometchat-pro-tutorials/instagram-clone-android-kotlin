package com.example.instagramclone.activity

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.cometchat.pro.core.AppSettings
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.uikit.ui_components.cometchat_ui.CometChatUI
import com.example.instagramclone.R
import com.example.instagramclone.constants.Constants
import com.example.instagramclone.fragment.FeedFragment
import com.example.instagramclone.fragment.NotificationFragment
import com.example.instagramclone.fragment.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), View.OnClickListener, BottomNavigationView.OnNavigationItemSelectedListener {
    private var plusIv: ImageView? = null
    private var chatIv: ImageView? = null
    private var logoutIv: ImageView? = null
    private var bottomNavigationView: BottomNavigationView? = null

    private var pDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        initEvents()
        initCometChat()
        initFragment(savedInstanceState)
    }

    private fun initViews() {
        plusIv = findViewById(R.id.plusIv)
        chatIv = findViewById(R.id.chatIv)
        logoutIv = findViewById(R.id.logoutIv)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        pDialog = ProgressDialog(this)
        pDialog!!.setMessage("Loading")
        pDialog!!.setCanceledOnTouchOutside(false)
    }

    private fun initEvents() {
        plusIv!!.setOnClickListener(this)
        chatIv!!.setOnClickListener(this)
        logoutIv!!.setOnClickListener(this)
        bottomNavigationView!!.setOnNavigationItemSelectedListener(this)
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

    private fun initFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            val fragment = FeedFragment()
            supportFragmentManager.beginTransaction().replace(R.id.container, fragment, fragment.javaClass.getSimpleName())
                .commit()
        }
    }

    private fun goToLoginActivity() {
        intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        if (CometChat.getLoggedInUser() == null) {
            goToLoginActivity();
        }
    }

    private fun goToCreatePost() {
        intent = Intent(this@MainActivity, CreateActivity::class.java)
        startActivity(intent)
    }

    private fun handleLogout() {
        pDialog!!.show()
        CometChat.logout(object : CometChat.CallbackListener<String>() {
            override fun onSuccess(p0: String?) {
                goToLoginActivity()
                pDialog!!.dismiss()
            }
            override fun onError(p0: CometChatException?) {
                pDialog!!.dismiss()
                Toast.makeText(this@MainActivity, "Cannot logout, please try again", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun logout() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Do you want to logout ?")
            .setCancelable(false)
            .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                    dialog, id -> handleLogout()
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()
            })
        val alert = dialogBuilder.create()
        alert.setTitle("Logout")
        alert.show()
    }

    private fun goToChat() {
        startActivity(Intent(this@MainActivity, CometChatUI::class.java))
    }

    override fun onClick(view: View?) {
        when(view!!.id) {
            R.id.plusIv -> goToCreatePost()
            R.id.logoutIv -> logout()
            R.id.chatIv -> goToChat()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_feed -> {
                val fragment = FeedFragment()
                supportFragmentManager.beginTransaction().replace(R.id.container, fragment, fragment.javaClass.getSimpleName())
                    .commit()
                return true
            }
            R.id.navigation_notification -> {
                val fragment = NotificationFragment()
                supportFragmentManager.beginTransaction().replace(R.id.container, fragment, fragment.javaClass.getSimpleName())
                    .commit()
                return true
            }
            R.id.navigation_profile -> {
                val fragment = ProfileFragment()
                supportFragmentManager.beginTransaction().replace(R.id.container, fragment, fragment.javaClass.getSimpleName())
                    .commit()
                return true
            }
        }
        return false
    }

}