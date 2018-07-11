package com.gh0u1l5.tenseconds.frontend.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import com.gh0u1l5.tenseconds.R
import com.gh0u1l5.tenseconds.backend.api.Auth
import com.gh0u1l5.tenseconds.backend.api.Store
import com.gh0u1l5.tenseconds.backend.crypto.MasterKey
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { _ ->
            val identityId = "iwEgyOOS74iX3mS3U90V"
            val accountId = "aU1PTgw4kf05DtHFo4W9"
            Store.IdentityCollection.fetch(identityId)
                    ?.addOnSuccessListener { _ ->
                        MasterKey.update(identityId, "passphrase".toCharArray())
                        Store.AccountCollection.fetch(identityId, accountId)
                                ?.addOnSuccessListener { account ->
                                    MasterKey.generate(this, identityId, accountId, account) {
                                        Log.w("RESULT", it.toString())
                                    }
                                }
                    }
        }

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onStart() {
        super.onStart()

        val user = Auth.instance.currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            return
        }

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
            R.id.nav_settings -> mainHandler.postDelayed({
                startActivity(Intent(this, SettingsActivity::class.java))
            }, 300L)
            R.id.nav_exit -> mainHandler.postDelayed({
                Auth.instance.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
            }, 300L)
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return false
    }
}