package com.internshala.echo.Fragments


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.internshala.echo.Adapters.MainScreenAdapter
import com.internshala.echo.R
import com.internshala.echo.Songs
import com.internshala.echo.Songs.Statified.dateComparator
import com.internshala.echo.Songs.Statified.nameComparator
import com.l4digital.fastscroll.FastScrollRecyclerView
import java.util.*

class MainScreenFragment : Fragment() {

    var recyclerViewObject: FastScrollRecyclerView? = null
    var playPauseButton: ImageButton? = null
    var songTitle: TextView? = null
    var noSongs: RelativeLayout? = null
    var nowPlayingBottomBar: RelativeLayout? = null
    var myActivity: Activity? = null
    var _mainScreenAdapter: MainScreenAdapter? = null
    var getSongsList: ArrayList<Songs>? = null
    var currentPosition: Int = 0
    var visibleLayout: RelativeLayout? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        getSongsList = getSongsFromPhone()
        nowPlayingBottomBarSetup()

        var prefs = myActivity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)
        var action_sort_alphabetic = prefs?.getString("action_sort_alphabetic", "true")
        var action_sort_date = prefs?.getString("action_sort_date", "false")

        _mainScreenAdapter = MainScreenAdapter(getSongsList as ArrayList<Songs>,
                myActivity as Context)

        if (getSongsList != null) {

            val mLayoutManager = LinearLayoutManager(myActivity)
            recyclerViewObject?.layoutManager = mLayoutManager
            recyclerViewObject?.itemAnimator = DefaultItemAnimator()
            recyclerViewObject?.adapter = _mainScreenAdapter

            if (action_sort_alphabetic?.equals("true", true) as Boolean) {
                Collections.sort(getSongsList, nameComparator)
            }
            if (action_sort_date?.equals("true", true) as Boolean) {
                Collections.sort(getSongsList, dateComparator)
            }
            _mainScreenAdapter?.notifyDataSetChanged()

        } else {
            visibleLayout?.visibility = View.INVISIBLE
            noSongs?.visibility = View.VISIBLE
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.main, menu)
        super.onCreateOptionsMenu(menu, inflater)
        return
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        val editor = myActivity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)?.edit()

        if (item?.itemId == R.id.action_sort_date) {
            if (getSongsList != null)
                Collections.sort(getSongsList, dateComparator)

            editor?.putString("action_sort_date", "true")
            editor?.putString("action_sort_alphabetic", "false")
            editor?.apply()
            _mainScreenAdapter?.notifyDataSetChanged()
            return false

        } else if (item?.itemId == R.id.action_sort_alphabetic) {
            if (getSongsList != null)
                Collections.sort(getSongsList, nameComparator)

            editor?.putString("action_sort_date", "false")
            editor?.putString("action_sort_alphabetic", "true")
            editor?.apply()
            _mainScreenAdapter?.notifyDataSetChanged()
            return false
        }


        return super.onOptionsItemSelected(item)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        setHasOptionsMenu(true)
        activity?.title = "All Songs"
        val view = inflater!!.inflate(R.layout.fragment_main_screen, container, false)
        recyclerViewObject = view?.findViewById(R.id.contentMain)
        playPauseButton = view?.findViewById(R.id.playPauseButton)
        visibleLayout = view?.findViewById(R.id.visible_layout)
        songTitle = view?.findViewById(R.id.songTitle)
        noSongs = view?.findViewById(R.id.noSongs)
        nowPlayingBottomBar = view?.findViewById(R.id.hidden_bar_mainscreen)

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    fun getSongsFromPhone(): ArrayList<Songs> {
        var arrayList = ArrayList<Songs>()
        var contentResolver = myActivity?.contentResolver
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songUri, null, null, null, null)

        if (songCursor != null && songCursor.moveToFirst()) {
            val songID = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtists = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)

            while (songCursor.moveToNext()) {
                var currentId = songCursor.getLong(songID)
                var currentTitle = songCursor.getString(songTitle)
                var currentArtist = songCursor.getString(songArtists)
                var currentData = songCursor.getString(songData)
                var currentDateIndex = songCursor.getLong(dateIndex)

                arrayList.add(Songs(currentId, currentTitle, currentArtist, currentData, currentDateIndex))
            }
        }

        return arrayList
    }

    fun nowPlayingBottomBarSetup() {
        try {

            nowPlayingBottomBarClickHandler()
            songTitle?.text = SongPlayingFragment.statified.songHelper?.songTitle
            if (SongPlayingFragment.statified.mediaplayer?.isPlaying as Boolean) {
                nowPlayingBottomBar?.visibility = View.VISIBLE
                val scale = context!!.resources.displayMetrics.density
                val px = (482 * scale + 0.5f).toInt()
                visibleLayout?.layoutParams?.height = px
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            } else {
                nowPlayingBottomBar?.visibility = View.INVISIBLE
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            }

            SongPlayingFragment.statified.mediaplayer?.setOnCompletionListener {
                SongPlayingFragment.Staticated.onSongCompletion()
                songTitle?.text = SongPlayingFragment.statified.songHelper?.songTitle
                _mainScreenAdapter?.notifyDataSetChanged()
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun nowPlayingBottomBarClickHandler() {

        nowPlayingBottomBar?.setOnClickListener {
            FavouritesFragment.Statified.mediaPlayer = SongPlayingFragment.statified.mediaplayer
            val songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putString("songArtist", SongPlayingFragment.statified.songHelper?.songArtist)
            args.putString("songTitle", SongPlayingFragment.statified.songHelper?.songTitle)
            args.putString("path", SongPlayingFragment.statified.songHelper?.songPath)
            args.putInt("songId", SongPlayingFragment.statified.songHelper?.songID?.toInt() as Int)
            args.putInt("songPosition", SongPlayingFragment.statified.songHelper?.currentPosition as Int)
            args.putParcelableArrayList("songData", SongPlayingFragment.statified.fetchSongs)
            args.putString("favBottomBar", "clicked")
            songPlayingFragment.arguments = args
            fragmentManager?.beginTransaction()?.replace(R.id.detailsFragment, songPlayingFragment)?.addToBackStack("SongPlayingFragment")?.commit()
        }

        playPauseButton?.setOnClickListener {

            if (SongPlayingFragment.statified.mediaplayer?.isPlaying == true) {
                SongPlayingFragment.statified.mediaplayer?.pause()
                currentPosition = SongPlayingFragment.statified.mediaplayer?.currentPosition as Int
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            } else {
                SongPlayingFragment.statified.mediaplayer?.seekTo(currentPosition)
                SongPlayingFragment.statified.mediaplayer?.start()
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        }
    }


}
