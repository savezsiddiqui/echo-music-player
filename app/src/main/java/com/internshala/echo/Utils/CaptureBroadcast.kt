package com.internshala.echo.Utils

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.internshala.echo.Activities.MainActivity.Statified.notificationManager
import com.internshala.echo.Fragments.SongPlayingFragment.statified.mediaplayer
import com.internshala.echo.Fragments.SongPlayingFragment.statified.playButton
import com.internshala.echo.Fragments.SongPlayingFragment.statified.songHelper
import com.internshala.echo.R

class CaptureBroadcast : BroadcastReceiver() {


    override fun onReceive(p0: Context?, p1: Intent?) {

        var tm: TelephonyManager = p0?.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager

        if (p1?.action == Intent.ACTION_NEW_OUTGOING_CALL || tm.callState == TelephonyManager.CALL_STATE_RINGING) {
            try {

                notificationManager?.cancel(1998)

                if (mediaplayer?.isPlaying == true) {
                    mediaplayer?.pause()
                    songHelper?.isPlaying = false
                    playButton?.setBackgroundResource(R.drawable.pause_icon)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }
}