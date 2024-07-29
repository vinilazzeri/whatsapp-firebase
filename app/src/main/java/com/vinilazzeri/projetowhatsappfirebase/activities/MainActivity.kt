package com.vinilazzeri.projetowhatsappfirebase.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.vinilazzeri.projetowhatsappfirebase.R
import com.vinilazzeri.projetowhatsappfirebase.adapters.ViewPagerAdapter
import com.vinilazzeri.projetowhatsappfirebase.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    //firebase
    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(binding.root)
        toolBarInitialize()
        initializeTabNavigation()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initializeTabNavigation() {

        val tabLayout = binding.mainTabLayout
        val viewPager = binding.mainViewPager

        //adapter
        val tabs = listOf("Chats", "Contacts")
        viewPager.adapter = ViewPagerAdapter(
            tabs, supportFragmentManager, lifecycle
        )

        tabLayout.isTabIndicatorFullWidth = true
        TabLayoutMediator(tabLayout, viewPager){
            tab, position ->
            tab.text = tabs[position]
        }.attach()

    }

    private fun toolBarInitialize() {
        val toolbar = binding.includeMainToolbar.mainTb
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "WhatsApp"
        }

        addMenuProvider(
            object : MenuProvider{
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.main_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when(menuItem.itemId){
                        R.id.profile_item -> {
                            startActivity(
                                Intent(applicationContext, ProfileActivity::class.java)
                            )
                        }
                        R.id.logout_item -> {
                            logoutUser()
                        }
                    }
                    return true
                }

            }
        )

    }

    private fun logoutUser() {
        AlertDialog.Builder(this)
            .setTitle("Log out")
            .setMessage("Would you like to leave Whatsapp?")
            .setNegativeButton("No"){dialog, position -> }
            .setPositiveButton("Yes"){dialog, position ->
                firebaseAuth.signOut()
                startActivity(
                    Intent(applicationContext, LoginActivity::class.java)
                )
            }
            .create()
            .show()
    }

}