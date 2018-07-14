package com.gh0u1l5.tenseconds.frontend.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.gh0u1l5.tenseconds.R
import com.gh0u1l5.tenseconds.backend.api.Auth
import com.gh0u1l5.tenseconds.backend.crypto.BiometricUtils
import com.gh0u1l5.tenseconds.frontend.adapters.IdentityAdapter
import com.gh0u1l5.tenseconds.frontend.fragments.AddAccountDialogFragment
import com.gh0u1l5.tenseconds.frontend.fragments.AddIdentityDialogFragment
import com.gh0u1l5.tenseconds.frontend.fragments.VerifyIdentityDialogFragment
import com.gh0u1l5.tenseconds.global.Constants.ACTION_ADD_ACCOUNT
import com.gh0u1l5.tenseconds.global.Constants.ACTION_ADD_IDENTITY
import com.gh0u1l5.tenseconds.global.Constants.ACTION_VERIFY_IDENTITY
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val mainHandler = Handler(Looper.getMainLooper())

    private val identityAdapter = IdentityAdapter(LinkedHashMap())
    private val identityLayoutManager = LinearLayoutManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        identity_list.apply {
            setHasFixedSize(true)
            adapter = identityAdapter
            layoutManager = identityLayoutManager
        }

        nav_view.setNavigationItemSelectedListener(this)

        fab.setOnClickListener { _ ->
            if (!BiometricUtils.hasValidBiometrics()) {
                // TODO: handle this situation gracefully
                return@setOnClickListener
            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent.apply {
                action = ACTION_ADD_IDENTITY
            })
        }

        main_container.setOnRefreshListener {
            identityAdapter.refreshData {
                main_container.isRefreshing = false
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val user = Auth.instance.currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            return
        }

        identityAdapter.refreshData {
            main_loading.visibility = View.GONE
            main_container.visibility = View.VISIBLE
        }

        val header = nav_view.getHeaderView(0)
        if (!user.displayName.isNullOrEmpty()) {
            header.findViewById<TextView>(R.id.user_nickname).text = user.displayName
        } else {
            val username = user.email?.substringBefore('@')?.capitalize()
            header.findViewById<TextView>(R.id.user_nickname).text = username
        }
        if (user.email != null) {
            header.findViewById<TextView>(R.id.user_email).text = user.email
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_exit -> mainHandler.postDelayed({
                Auth.instance.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
            }, 300L)
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return false
    }

    override fun onNewIntent(intent: Intent) {
        when (intent.action) {
            ACTION_ADD_IDENTITY -> {
                AddIdentityDialogFragment().apply {
                    addOnFinishedListener {
                        identityAdapter.refreshData()
                    }
                    show(supportFragmentManager, "AddIdentity")
                }
            }
            ACTION_ADD_ACCOUNT -> {
                AddAccountDialogFragment().apply {
                    val identityId = intent.getStringExtra("identityId") ?: return
                    setIdentity(identityId)
                    addOnFinishedListener {
                        identityAdapter.accountAdapters[identityId]?.refreshData()
                    }
                    show(supportFragmentManager, "AddAccount")
                }
            }
            ACTION_VERIFY_IDENTITY -> {
                VerifyIdentityDialogFragment().apply {
                    val identityId = intent.getStringExtra("identityId") ?: return
                    setIdentity(identityId)
                    addOnFinishedListener {
                        identityAdapter.notifyDataSetChanged() // TODO: does this work?
                    }
                    show(supportFragmentManager, "VerifyIdentity")
                }
            }
        }
    }
}
