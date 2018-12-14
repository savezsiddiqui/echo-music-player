package com.internshala.echo.Fragments


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.internshala.echo.Databases.EchoDatabase
import com.internshala.echo.Fragments.FavouritesFragment.Statified.mediaPlayer
import com.internshala.echo.Fragments.SettingsFragment.Statified.MY_SHAKE_PREFS
import com.internshala.echo.Fragments.SongPlayingFragment.Staticated.onSongCompletion
import com.internshala.echo.Fragments.SongPlayingFragment.Staticated.play
import com.internshala.echo.Fragments.SongPlayingFragment.Staticated.playNext
import com.internshala.echo.Fragments.SongPlayingFragment.Staticated.processIntel
import com.internshala.echo.Fragments.SongPlayingFragment.Staticated.updateTextViews
import com.internshala.echo.Fragments.SongPlayingFragment.statified.MyActivity
import com.internshala.echo.Fragments.SongPlayingFragment.statified.audioVisualiztion
import com.internshala.echo.Fragments.SongPlayingFragment.statified.currentPosition
import com.internshala.echo.Fragments.SongPlayingFragment.statified.endTime
import com.internshala.echo.Fragments.SongPlayingFragment.statified.favouriteContent
import com.internshala.echo.Fragments.SongPlayingFragment.statified.favouritesButton
import com.internshala.echo.Fragments.SongPlayingFragment.statified.fetchSongs
import com.internshala.echo.Fragments.SongPlayingFragment.statified.glVisualizer
import com.internshala.echo.Fragments.SongPlayingFragment.statified.loopButton
import com.internshala.echo.Fragments.SongPlayingFragment.statified.mSensorListener
import com.internshala.echo.Fragments.SongPlayingFragment.statified.mSensorManager
import com.internshala.echo.Fragments.SongPlayingFragment.statified.mediaplayer
import com.internshala.echo.Fragments.SongPlayingFragment.statified.playButton
import com.internshala.echo.Fragments.SongPlayingFragment.statified.seekbar
import com.internshala.echo.Fragments.SongPlayingFragment.statified.shuffleButton
import com.internshala.echo.Fragments.SongPlayingFragment.statified.skipNextButton
import com.internshala.echo.Fragments.SongPlayingFragment.statified.skipPreviousButton
import com.internshala.echo.Fragments.SongPlayingFragment.statified.songHelper
import com.internshala.echo.Fragments.SongPlayingFragment.statified.startTime
import com.internshala.echo.Fragments.SongPlayingFragment.statified.trackArtist
import com.internshala.echo.Fragments.SongPlayingFragment.statified.trackTitle
import com.internshala.echo.Fragments.SongPlayingFragment.statified.updateTime
import com.internshala.echo.R
import com.internshala.echo.SongHelperClass
import com.internshala.echo.Songs
import java.util.*
import java.util.concurrent.TimeUnit


class SongPlayingFragment : Fragment() {

    @SuppressLint("StaticFieldLeak")
    object statified {
        var MyActivity: Activity? = null
        var mediaplayer: MediaPlayer? = null
        var trackTitle: TextView? = null
        var trackArtist: TextView? = null
        var seekbar: SeekBar? = null
        var startTime: TextView? = null
        var endTime: TextView? = null
        var playButton: ImageButton? = null
        var skipNextButton: ImageButton? = null
        var skipPreviousButton: ImageButton? = null
        var shuffleButton: ImageButton? = null
        var loopButton: ImageButton? = null
        var songHelper: SongHelperClass? = null
        var currentPosition: Int = 0
        var fetchSongs: ArrayList<Songs>? = null
        var audioVisualiztion: AudioVisualization? = null
        var glVisualizer: GLAudioVisualizationView? = null
        var favouritesButton: ImageButton? = null
        var favouriteContent: EchoDatabase? = null
        var mSensorManager: SensorManager? = null
        var mSensorListener: SensorEventListener? = null

        var updateTime = object : Runnable {
            override fun run() {
                val getCurrent = mediaplayer?.currentPosition

                val minutes = TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(getCurrent.toLong()) % 60

                if ((minutes * 60 + seconds) <= mediaplayer?.duration as Int) {
                    startTime?.text = String.format("%d:%d", minutes, seconds)
                    seekbar?.progress = getCurrent.toInt()
                }

                Handler().postDelayed(this, 1000)
            }

        }
    }

    object Staticated {
        var MY_SHUFFLE_PREFS = "Shuffle"
        var MY_LOOP_PREFS = "Loop"

        fun onSongCompletion() {

            songHelper?.isPlaying = true
            when {
                songHelper?.isLoop == true -> play()
                songHelper?.isShuffle == true -> playNext("Shuffle")
                else -> playNext("playNextNormal")
            }
        }

        fun play() {

            var nextSong = fetchSongs?.get(currentPosition)
            songHelper?.songPath = nextSong?.songData
            songHelper?.songTitle = nextSong?.songTitle
            songHelper?.songArtist = nextSong?.artist
            songHelper?.songID = nextSong?.SongID as Long

            updateTextViews(songHelper?.songTitle as String, songHelper?.songArtist as String)

            if (favouriteContent?.checkIfIdExists(songHelper?.songID?.toInt() as Int) as Boolean) {
                favouritesButton?.setImageDrawable(ContextCompat.getDrawable(MyActivity!!, R.drawable.favourite_on))
            } else {
                favouritesButton?.setImageDrawable(ContextCompat.getDrawable(MyActivity!!, R.drawable.favourite_off))
            }

            mediaplayer?.reset()
            try {
                mediaplayer?.setDataSource(MyActivity, Uri.parse(songHelper?.songPath))
                mediaplayer?.prepare()
                mediaplayer?.start()
                processIntel(mediaplayer as MediaPlayer)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun playNext(check: String) {
            when {
                check.equals("playNextNormal", true) -> currentPosition += 1
                check.equals("Shuffle", true) -> {
                    var randObject = Random()
                    var randomPosition = randObject.nextInt(fetchSongs?.size?.plus(1) as Int)
                    currentPosition = randomPosition
                }
            }
            if (currentPosition == fetchSongs?.size)
                currentPosition = 0

            playButton?.setBackgroundResource(R.drawable.pause_icon)
            songHelper?.isLoop = false
            play()
        }

        fun updateTextViews(songTitle: String, songArtist: String) {
            trackTitle?.text = songTitle
            trackArtist?.text = songArtist
        }

        fun processIntel(mediaplayer: MediaPlayer) {

            val finalTime = mediaplayer.duration
            val sTime = mediaplayer.currentPosition
            startTime?.text = String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(sTime.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(sTime.toLong()) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(sTime.toLong())))

            endTime?.text = String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong())))

            seekbar?.max = finalTime
            seekbar?.progress = sTime
            Handler().postDelayed(updateTime, 1000)
        }
    }

    var mAcceleration: Float = 0f
    var mAccelerationCurrent: Float = 0f
    var mAccelerationLast: Float = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        activity?.title = "All Songs"
        var view = inflater.inflate(R.layout.fragment_song_playing, container, false)
        trackTitle = view?.findViewById(R.id.songTitle)
        trackArtist = view?.findViewById(R.id.songArtist)
        seekbar = view?.findViewById(R.id.seekBar)
        startTime = view?.findViewById(R.id.startTime)
        endTime = view?.findViewById(R.id.endTime)
        playButton = view?.findViewById(R.id.playPauseButton)
        skipNextButton = view?.findViewById(R.id.playNextButton)
        skipPreviousButton = view?.findViewById(R.id.playPreviousButton)
        shuffleButton = view?.findViewById(R.id.shuffleButton)
        loopButton = view?.findViewById(R.id.loopButton)
        glVisualizer = view?.findViewById(R.id.visualizer_view)
        favouritesButton = view?.findViewById(R.id.favouriteButton)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioVisualiztion = glVisualizer as AudioVisualization
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        MyActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        MyActivity = activity
    }

    override fun onResume() {
        super.onResume()
        audioVisualiztion?.onResume()
        mSensorManager?.registerListener(mSensorListener,
                mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        audioVisualiztion?.onPause()
        mSensorManager?.unregisterListener(mSensorListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        audioVisualiztion?.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSensorManager = MyActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAcceleration = 0.0f
        mAccelerationCurrent = SensorManager.GRAVITY_EARTH
        mAccelerationLast = SensorManager.GRAVITY_EARTH
        bindShakeListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)

        var item: MenuItem? = menu?.findItem(R.id.action_redirect)
        item?.isVisible = true

        var item2: MenuItem? = menu?.findItem(R.id.action_sort)
        item2?.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_redirect -> {
                MyActivity?.onBackPressed()
                return false
            }
        }
        return false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        favouriteContent = EchoDatabase(MyActivity)
        songHelper = SongHelperClass()
        songHelper?.isLoop = false
        songHelper?.isShuffle = false

        var path: String? = null
        var songTitle: String? = null
        var songArtist: String? = null
        var songID: Long = 0

        try {
            path = arguments?.getString("path")
            songTitle = arguments?.getString("songTitle")
            songArtist = arguments?.getString("songArtist")
            songID = arguments?.getInt("songId")?.toLong() as Long
            currentPosition = arguments?.getInt("songPosition") as Int
            fetchSongs = arguments?.getParcelableArrayList("songData")

            songHelper?.songPath = path
            songHelper?.songArtist = songArtist
            songHelper?.songTitle = songTitle
            songHelper?.songID = songID
            songHelper?.currentPosition = currentPosition

            updateTextViews(songHelper?.songTitle as String, songHelper?.songArtist as String)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        var fromBottomBar = arguments?.get("favBottomBar") as? String

        if (fromBottomBar != null) {
            mediaplayer = mediaPlayer
        } else {
            mediaplayer = MediaPlayer()
            mediaplayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)

            try {
                mediaplayer?.setDataSource(MyActivity, Uri.parse(path))
                mediaplayer?.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mediaplayer?.start()
        }

        songHelper?.isPlaying = mediaplayer?.isPlaying == true
        processIntel(mediaplayer as MediaPlayer)

        if (songHelper?.isPlaying == true)
            playButton?.setBackgroundResource(R.drawable.pause_icon)
        else
            playButton?.setBackgroundResource(R.drawable.play_icon)

        mediaplayer?.setOnCompletionListener {
            onSongCompletion()
        }

        clickHandler()

        var visualizationHandler = DbmHandler.Factory.newVisualizerHandler(MyActivity as Context, 0)
        audioVisualiztion?.linkTo(visualizationHandler)

        var prefsForShuffle = MyActivity?.getSharedPreferences(Staticated.MY_SHUFFLE_PREFS, Context.MODE_PRIVATE)
        var isShuffleEnabled = prefsForShuffle?.getBoolean("feature", false)

        if (isShuffleEnabled as Boolean) {
            songHelper?.isShuffle = true
            songHelper?.isLoop = false
            shuffleButton?.setBackgroundResource(R.drawable.shuffle_icon)
            loopButton?.setBackgroundResource(R.drawable.loop_white_icon)
        } else {
            songHelper?.isShuffle = false
            shuffleButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }

        var prefsForLoop = MyActivity?.getSharedPreferences(Staticated.MY_LOOP_PREFS, Context.MODE_PRIVATE)
        var isLoopEnabled = prefsForLoop?.getBoolean("feature", false)

        if (isLoopEnabled as Boolean) {
            songHelper?.isLoop = true
            songHelper?.isShuffle = false
            loopButton?.setBackgroundResource(R.drawable.loop_icon)
            shuffleButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        } else {
            songHelper?.isLoop = false
            loopButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }

        if (favouriteContent?.checkIfIdExists(songHelper?.songID?.toInt() as Int) as Boolean) {
            favouritesButton?.setImageDrawable(ContextCompat.getDrawable(MyActivity!!, R.drawable.favourite_on))
        } else {
            favouritesButton?.setImageDrawable(ContextCompat.getDrawable(MyActivity!!, R.drawable.favourite_off))
        }
    }


    fun clickHandler() {

        favouritesButton?.setOnClickListener {
            if (favouriteContent?.checkIfIdExists(songHelper?.songID?.toInt() as Int) as Boolean) {
                favouritesButton?.setImageDrawable(ContextCompat.getDrawable(MyActivity!!, R.drawable.favourite_off))
                favouriteContent?.deleteFavourite(songHelper?.songID?.toInt() as Int)
                Toast.makeText(MyActivity, "Removed From Favourites ", Toast.LENGTH_SHORT).show()
            } else {
                favouritesButton?.setImageDrawable(ContextCompat.getDrawable(MyActivity!!, R.drawable.favourite_on))
                favouriteContent?.storeASFavourite(songHelper?.songID?.toInt() as Int, songHelper?.songArtist as String, songHelper?.songTitle as String, songHelper?.songPath as String)
                Toast.makeText(MyActivity, "Added to Favourites ", Toast.LENGTH_SHORT).show()
            }
        }

        playButton?.setOnClickListener {
            if (mediaplayer?.isPlaying as Boolean) {
                mediaplayer?.pause()
                playButton?.setBackgroundResource(R.drawable.play_icon)
                songHelper?.isPlaying = false
            } else {
                mediaplayer?.start()
                playButton?.setBackgroundResource(R.drawable.pause_icon)
                songHelper?.isPlaying = true
            }
        }

        skipNextButton?.setOnClickListener {
            songHelper?.isPlaying = true
            if (songHelper?.isShuffle == true)
                playNext("Shuffle")
            else
                playNext("playNextNormal")

        }

        skipPreviousButton?.setOnClickListener {
            songHelper?.isPlaying = true
            if (songHelper?.isLoop == true)
                loopButton?.setBackgroundResource(R.drawable.loop_white_icon)
            playPrevious()
        }

        shuffleButton?.setOnClickListener {

            var editorShuffle = MyActivity?.getSharedPreferences(Staticated.MY_SHUFFLE_PREFS, Context.MODE_PRIVATE)?.edit()
            var editorLoop = MyActivity?.getSharedPreferences(Staticated.MY_LOOP_PREFS, Context.MODE_PRIVATE)?.edit()

            if (songHelper?.isShuffle == true) {
                songHelper?.isShuffle = false
                shuffleButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
            } else {
                songHelper?.isShuffle = true
                shuffleButton?.setBackgroundResource(R.drawable.shuffle_icon)
                songHelper?.isLoop = false
                loopButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorShuffle?.putBoolean("feature", true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            }
        }

        loopButton?.setOnClickListener {

            var editorShuffle = MyActivity?.getSharedPreferences(Staticated.MY_SHUFFLE_PREFS, Context.MODE_PRIVATE)?.edit()
            var editorLoop = MyActivity?.getSharedPreferences(Staticated.MY_LOOP_PREFS, Context.MODE_PRIVATE)?.edit()

            if (songHelper?.isLoop == true) {
                songHelper?.isLoop = false
                loopButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            } else {
                songHelper?.isLoop = true
                songHelper?.isShuffle = false
                loopButton?.setBackgroundResource(R.drawable.loop_icon)
                shuffleButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", true)
                editorLoop?.apply()
            }
        }

        seekbar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                mediaplayer?.pause()
                var pos = seekbar?.progress as Int
                mediaplayer?.seekTo(pos)
                mediaplayer?.start()
                processIntel(mediaplayer as MediaPlayer)
            }

        })


    }


    fun playPrevious() {
        currentPosition -= 1
        if (currentPosition == -1)
            currentPosition = 0

        playButton?.setBackgroundResource(R.drawable.pause_icon)
        songHelper?.isLoop = false

        play()
    }

    fun bindShakeListener() {
        mSensorListener = object : SensorEventListener {
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

            }

            override fun onSensorChanged(p0: SensorEvent) {
                val x = p0.values[0]
                val y = p0.values[1]
                val z = p0.values[2]

                mAccelerationLast = mAccelerationCurrent
                mAccelerationCurrent = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                var delta = mAccelerationCurrent - mAccelerationLast
                mAcceleration = mAcceleration * 0.9f + delta

                if (mAcceleration > 12) {
                    var prefs = MyActivity?.getSharedPreferences(MY_SHAKE_PREFS, Context.MODE_PRIVATE)
                    var isAllowed = prefs?.getBoolean("feature", false)
                    if (isAllowed == true) {
                        playNext("playNextNormal")
                    }

                }
            }

        }
    }


}
