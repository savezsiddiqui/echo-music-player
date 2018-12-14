package com.internshala.echo.Fragments


import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.internshala.echo.Activities.MainActivity
import com.internshala.echo.Adapters.FavouritesAdapter
import com.internshala.echo.Databases.EchoDatabase
import com.internshala.echo.Fragments.FavouritesFragment.Statified.mediaPlayer
import com.internshala.echo.Fragments.SongPlayingFragment.Staticated.onSongCompletion
import com.internshala.echo.Fragments.SongPlayingFragment.statified.currentPosition
import com.internshala.echo.Fragments.SongPlayingFragment.statified.favouriteContent
import com.internshala.echo.Fragments.SongPlayingFragment.statified.fetchSongs
import com.internshala.echo.Fragments.SongPlayingFragment.statified.mediaplayer
import com.internshala.echo.Fragments.SongPlayingFragment.statified.playButton
import com.internshala.echo.Fragments.SongPlayingFragment.statified.songHelper
import com.internshala.echo.R
import com.internshala.echo.Songs
import kotlinx.android.synthetic.main.app_bar_main.*
import java.util.*


class FavouritesFragment : Fragment() {

    var myActivity: Activity? = null
    var noFavourites: TextView? = null
    var playButtonBottomBar: ImageButton? = null
    var bottomBar: RelativeLayout? = null
    var favRecycler: RecyclerView? = null
    var songTitle: TextView? = null
    var currentPosition: Int = 0
    var echoData: EchoDatabase? = null
    var refreshList: ArrayList<Songs>? = null
    var databaseList: ArrayList<Songs>? = null
    var favouritesAdapter: FavouritesAdapter? = null
    var visibleLayout: RelativeLayout? = null

    object Statified {
        var mediaPlayer: MediaPlayer? = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        activity?.title = "Favourites"
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_favourites, container, false)

        noFavourites = view?.findViewById(R.id.noFavourites)
        playButtonBottomBar = view?.findViewById(R.id.playBottomBar)
        bottomBar = view?.findViewById(R.id.hiddenBarFavScreen)
        favRecycler = view?.findViewById(R.id.favouritesRecycler)
        songTitle = view?.findViewById(R.id.songTitleFavScreen)
        visibleLayout = view?.findViewById(R.id.visible_layout_favScreen)

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

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.main, menu)
        super.onCreateOptionsMenu(menu, inflater)
        return
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        val editor = myActivity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)?.edit()

        if (item?.itemId == R.id.action_sort_date) {
            if (refreshList != null)
                Collections.sort(refreshList, Songs.Statified.dateComparator)

            editor?.putString("action_sort_date", "true")
            editor?.putString("action_sort_alphabetic", "false")
            editor?.apply()
            favouritesAdapter?.notifyDataSetChanged()
            return false

        } else if (item?.itemId == R.id.action_sort_alphabetic) {
            if (refreshList != null)
                Collections.sort(refreshList, Songs.Statified.nameComparator)

            editor?.putString("action_sort_date", "false")
            editor?.putString("action_sort_alphabetic", "true")
            editor?.apply()
            favouritesAdapter?.notifyDataSetChanged()
            return false
        }


        return super.onOptionsItemSelected(item)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        var prefs = myActivity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)
        var action_sort_alphabetic = prefs?.getString("action_sort_alphabetic", "true")
        var action_sort_date = prefs?.getString("action_sort_date", "false")

        echoData = EchoDatabase(myActivity)
        display_fav_after_checking()
        bottomBarSetup()

        if (refreshList != null) {

            val mLayoutManager = LinearLayoutManager(myActivity)
            favRecycler?.layoutManager = mLayoutManager
            favRecycler?.itemAnimator = DefaultItemAnimator()
            favRecycler?.adapter = favouritesAdapter

            if (action_sort_alphabetic?.equals("true", true) as Boolean) {
                Collections.sort(refreshList, Songs.Statified.nameComparator)
            }
            if (action_sort_date?.equals("true", true) as Boolean) {
                Collections.sort(refreshList, Songs.Statified.dateComparator)
            }
            favouritesAdapter?.notifyDataSetChanged()

        } else {
            visibleLayout?.visibility = View.INVISIBLE
            noFavourites?.visibility = View.VISIBLE
        }
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

    fun bottomBarSetup() {
        try {

            bottomBarClickHandler()
            songTitle?.text = songHelper?.songTitle

            if (mediaplayer?.isPlaying as Boolean) {
                bottomBar?.visibility = View.VISIBLE
                playButtonBottomBar?.setBackgroundResource(R.drawable.pause_icon)
                val scale = context!!.resources.displayMetrics.density
                val px = (482 * scale + 0.5f).toInt()
                visibleLayout?.layoutParams?.height = px
            } else {
                bottomBar?.visibility = View.INVISIBLE
                playButtonBottomBar?.setBackgroundResource(R.drawable.play_icon)
            }

            mediaplayer?.setOnCompletionListener {
                onSongCompletion()
                songTitle?.text = songHelper?.songTitle
                favouritesAdapter?.notifyDataSetChanged()
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun bottomBarClickHandler() {

        bottomBar?.setOnClickListener {
            mediaPlayer = mediaplayer
            val songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putString("songArtist", songHelper?.songArtist)
            args.putString("songTitle", songHelper?.songTitle)
            args.putString("path", songHelper?.songPath)
            args.putInt("songId", songHelper?.songID?.toInt() as Int)
            args.putInt("songPosition", songHelper?.currentPosition as Int)
            args.putParcelableArrayList("songData", fetchSongs)
            args.putString("favBottomBar", "clicked")
            songPlayingFragment.arguments = args
            fragmentManager?.beginTransaction()?.replace(R.id.detailsFragment, songPlayingFragment)?.addToBackStack("SongPlayingFragment")?.commit()
        }

        playButtonBottomBar?.setOnClickListener {

            if (mediaplayer?.isPlaying == true) {
                mediaplayer?.pause()
                currentPosition = mediaplayer?.currentPosition as Int
                playButtonBottomBar?.setBackgroundResource(R.drawable.play_icon)
            } else {
                mediaplayer?.seekTo(currentPosition)
                mediaplayer?.start()
                playButtonBottomBar?.setBackgroundResource(R.drawable.pause_icon)
            }
        }
    }


    fun display_fav_after_checking() {

        if (echoData?.checksize() as Int > 0) {
            refreshList = ArrayList<Songs>()
            databaseList = echoData?.queryDBlist()
            val fetchListFromDevice = getSongsFromPhone()
            if (fetchListFromDevice != null) {
                for (i in 0..fetchListFromDevice.size - 1) {
                    for (j in 0..databaseList?.size as Int - 1) {

                        if (databaseList?.get(j)?.SongID === (fetchListFromDevice?.get(i)?.SongID)) {
                            refreshList?.add((databaseList as ArrayList<Songs>)[j])
                        }
                    }
                }

            }
            if (refreshList == null) {
                favRecycler?.visibility = View.INVISIBLE
                noFavourites?.visibility = View.VISIBLE
            } else {
                favouritesAdapter = FavouritesAdapter(refreshList as ArrayList<Songs>, myActivity as Context)
                val mLayoutManager = LinearLayoutManager(myActivity)
                favRecycler?.layoutManager = mLayoutManager
                favRecycler?.itemAnimator = DefaultItemAnimator()
                favRecycler?.adapter = favouritesAdapter
                favRecycler?.setHasFixedSize(true)
            }
        } else {
            favRecycler?.visibility = View.INVISIBLE
            noFavourites?.visibility = View.VISIBLE
        }

    }
}
