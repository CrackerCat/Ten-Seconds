package com.gh0u1l5.tenseconds.frontend.activity

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
import android.widget.TextView
import com.gh0u1l5.tenseconds.R
import com.gh0u1l5.tenseconds.backend.api.Auth
import com.gh0u1l5.tenseconds.backend.crypto.BiometricUtils
import com.gh0u1l5.tenseconds.frontend.adapter.IdentityAdapter
import com.gh0u1l5.tenseconds.frontend.fragments.AddIdentityDialogFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val mainHandler = Handler(Looper.getMainLooper())

    private val identityListAdapter = IdentityAdapter(emptyList())
    private val identityListLayoutManager = LinearLayoutManager(this)

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
            adapter = identityListAdapter
            layoutManager = identityListLayoutManager
        }

        nav_view.setNavigationItemSelectedListener(this)

        fab.setOnClickListener { _ ->
            if (!BiometricUtils.hasValidBiometrics()) {
                // TODO: handle this situation gracefully
                return@setOnClickListener
            }
            AddIdentityDialogFragment().apply {
                addOnFinishedListener {
                    identityListAdapter.refreshData()
                }
                show(supportFragmentManager, "AddIdentity")
            }
        }

        main_container.setOnRefreshListener {
            identityListAdapter.refreshData()
        }
    }

    override fun onStart() {
        super.onStart()

        val user = Auth.instance.currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            return
        }

        identityListAdapter.refreshData()

        val header = nav_view.getHeaderView(0)
        if (user.email != null) {
            val username = user.email?.substringBefore('@')?.capitalize()
            header.findViewById<TextView>(R.id.user_nickname).text = username
            header.findViewById<TextView>(R.id.user_email).text = user.email
        } else {
            val username = if (user.displayName.isNullOrEmpty()) "Unknown" else user.displayName
            header.findViewById<TextView>(R.id.user_nickname).text = username
            header.findViewById<TextView>(R.id.user_email).text = "unknown@somewhere.com"
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
}
