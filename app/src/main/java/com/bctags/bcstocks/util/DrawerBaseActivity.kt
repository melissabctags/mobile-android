package com.bctags.bcstocks.util

import android.content.Context
import android.content.Intent
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bctags.bcstocks.R
import com.bctags.bcstocks.ui.inventory.InventoryCountActivity
import com.bctags.bcstocks.ui.login.LoginActivity
import com.bctags.bcstocks.ui.receives.NewReceiveActivity
import com.bctags.bcstocks.ui.simplereader.TagReaderActivity
import com.google.android.material.navigation.NavigationView

open class DrawerBaseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawer_base)

        drawerLayout = layoutInflater.inflate(R.layout.activity_drawer_base,null) as DrawerLayout;
        var container: FrameLayout  =  drawerLayout.findViewById(R.id.activityContainer)

    }

    override fun setContentView(view: View){

        drawerLayout = layoutInflater.inflate(R.layout.activity_drawer_base,null) as DrawerLayout;
        var container:FrameLayout = drawerLayout.findViewById(R.id.activityContainer);
        container.addView(view);
        super.setContentView(drawerLayout);

        var toolbar:Toolbar = drawerLayout.findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        var navigationView:NavigationView = drawerLayout.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        var toggle: ActionBarDrawerToggle = ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close)
        toggle.syncState()

        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)

        var ivNotification: ImageView = drawerLayout.findViewById(R.id.ivNotification);
        ivNotification.setOnClickListener{
            val intent = Intent(this, NewReceiveActivity::class.java)
            startActivity(intent)
        }
//
//        var ivSideMenu: ImageView = drawerLayout.findViewById(R.id.ivSideMenu);
//        ivSideMenu.setOnClickListener{
//            drawerLayout.openDrawer(navigationView)
//        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawerLayout.closeDrawer(GravityCompat.START)
        when (item.itemId) {
            R.id.nav_item_logOut -> clearSessionPreference()
            R.id.nav_item_simple_reader -> simpleTagReader()
            R.id.nav_item_inventory_count -> inventoryCount()
        }
        return false;
    }
    private fun clearSessionPreference(){
        val sharedPreferences = getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("TOKEN", "").apply()
        sharedPreferences.edit().putBoolean("SESSION", false).apply()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun inventoryCount(){
        val intent = Intent(this, InventoryCountActivity::class.java)
        startActivity(intent)
    }
    private fun simpleTagReader(){
        val intent = Intent(this, TagReaderActivity::class.java)
        startActivity(intent)
    }



    private fun allocateActivityTitle(titleString: String){
        supportActionBar?.title = titleString
    }




}