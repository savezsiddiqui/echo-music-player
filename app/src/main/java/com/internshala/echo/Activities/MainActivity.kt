package com.internshala.echo.Activities

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import com.internshala.echo.Activities.MainActivity.Statified.drawerLayout
import com.internshala.echo.Activities.MainActivity.Statified.notificationManager
import com.internshala.echo.Adapters.NavigationDrawerAdapter
import com.internshala.echo.Fragments.MainScreenFragment
import com.internshala.echo.Fragments.SongPlayingFragment.statified.mediaplayer
import com.internshala.echo.Fragments.SongPlayingFragment.statified.songHelper
import com.internshala.echo.R
import android.content.BroadcastReceiver
import com.internshala.echo.Utils.CaptureBroadcast
import android.net.ConnectivityManager
import android.content.IntentFilter
import android.support.v4.app.NotificationCompat
import com.internshala.echo.Fragments.SongPlayingFragment.statified.MyActivity
import com.internshala.echo.Fragments.SongPlayingFragment.statified.currentPosition


class MainActivity : AppCompatActivity() {

    var navigationDrawerIconList: ArrayList<String> = arrayListOf()
    var imagesForNavigationDrawer = intArrayOf(R.drawable.navigation_allsongs,
            R.drawable.navigation_favorites, R.drawable.navigation_settings,
            R.drawable.navigation_aboutus)
    var trackNotificationBuilder: Notification? = null
    var notificationChannel: NotificationChannel? = null
    var notificationManager: NotificationManager? = null
    var channelId = "com.internshala.echo.Activities"
    var description = "we are VENOM. try to stop us and u die."


    @SuppressLint("StaticFieldLeak")
    object Statified {
        var drawerLayout: DrawerLayout? = null
        var notificationManager: NotificationManager? = null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)

        navigationDrawerIconList.add("All Songs")
        navigationDrawerIconList.add("Favourites")
        navigationDrawerIconList.add("Settings")
        navigationDrawerIconList.add("About Me")
        var toggle = ActionBarDrawerToggle(this@MainActivity, drawerLayout,
                toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close)
        drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()

        var mainScreenFragment = MainScreenFragment()
        this.supportFragmentManager
                .beginTransaction()
                .add(R.id.detailsFragment, mainScreenFragment, "MainScreenFragment")
                .commit()

        var navigationAdapter = NavigationDrawerAdapter(navigationDrawerIconList,
                imagesForNavigationDrawer, this)
        navigationAdapter.notifyDataSetChanged()

        var recyclerView = findViewById<RecyclerView>(R.id.navigationRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = navigationAdapter
        recyclerView.setHasFixedSize(true)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var intent = Intent(this@MainActivity, MainActivity::class.java)
        var pintent = PendingIntent.getActivity(this@MainActivity, System.currentTimeMillis().toInt(),
                intent, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationManager?.createNotificationChannel(notificationChannel)
            trackNotificationBuilder = Notification.Builder(this, channelId)
                    .setContentTitle("A track is playing in the background")
                    .setSmallIcon(R.drawable.echo_logo)
                    .setContentIntent(pintent)
                    .setOngoing(true)
                    .setAutoCancel(true)
                    .build()

        } else {
            trackNotificationBuilder = Notification.Builder(this)
                    .setContentTitle("A track is playing in the background")
                    .setSmallIcon(R.drawable.echo_logo)
                    .setContentIntent(pintent)
                    .setOngoing(true)
                    .setAutoCancel(true)
                    .build()
        }

    }

    override fun onStart() {
        super.onStart()
        try {
            notificationManager?.cancel(1998)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            if (mediaplayer?.isPlaying == true) {
                notificationManager?.notify(1998, trackNotificationBuilder)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            notificationManager?.cancel(1998)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
