package com.internshala.echo.Adapters

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.internshala.echo.Activities.MainActivity
import com.internshala.echo.Fragments.MainScreenFragment
import com.internshala.echo.Fragments.SongPlayingFragment
import com.internshala.echo.Fragments.SongPlayingFragment.statified.mediaplayer
import com.internshala.echo.Fragments.SongPlayingFragment.statified.trackTitle
import com.internshala.echo.R
import com.internshala.echo.Songs
import com.l4digital.fastscroll.FastScrollRecyclerView
import com.l4digital.fastscroll.FastScroller

class MainScreenAdapter(_songDetails: ArrayList<Songs>, _context: Context) : RecyclerView.Adapter<MainScreenAdapter.SongViewHolder>(), FastScroller.SectionIndexer {

    override fun getSectionText(position: Int): String {
        val songObject = songDetails?.get(position)
        val key = songObject?.songTitle.toString()
        return key[0].toString()
    }

    var songDetails: ArrayList<Songs>? = null
    var mContext: Context? = null

    init {
        this.songDetails = _songDetails
        this.mContext = _context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        var itemView = LayoutInflater.from(parent?.context)
                .inflate(R.layout.row_custom_mainscreen_adapter, parent, false)

        return SongViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        if (songDetails == null)
            return 0
        else
            return (songDetails as ArrayList<Songs>).size
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val songObject = songDetails?.get(position)
        holder.trackTitle?.text = songObject?.songTitle
        holder.trackArtist?.text = songObject?.artist
        holder.contentHolder?.setOnClickListener {

            if (mediaplayer?.isPlaying == true) {
                mediaplayer?.reset()
            }

            val songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putString("songArtist", songObject?.artist)
            args.putString("songTitle", songObject?.songTitle)
            args.putString("path", songObject?.songData)
            args.putInt("songId", songObject?.SongID?.toInt() as Int)
            args.putInt("songPosition", position)
            args.putParcelableArrayList("songData", songDetails)
            songPlayingFragment.arguments = args

            (mContext as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.detailsFragment, songPlayingFragment)
                    .addToBackStack("fromMainScreen")
                    .commit()
        }
    }

    class SongViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        var trackTitle: TextView? = null
        var trackArtist: TextView? = null
        var contentHolder: RelativeLayout? = null

        init {
            trackTitle = itemView?.findViewById(R.id.trackTitle)
            trackArtist = itemView?.findViewById(R.id.trackArtist)
            contentHolder = itemView?.findViewById(R.id.contentRow)
        }
    }
}
