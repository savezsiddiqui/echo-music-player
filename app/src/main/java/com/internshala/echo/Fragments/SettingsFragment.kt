package com.internshala.echo.Fragments


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.Switch
import com.internshala.echo.Fragments.SettingsFragment.Statified.MY_SHAKE_PREFS
import com.internshala.echo.R



class SettingsFragment : Fragment() {

    var switchButton : Switch? = null
    var myActivity : Activity? = null

    object Statified {
        var MY_SHAKE_PREFS = "ShakeFeature"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        activity?.title = "Settings"
        var view =  inflater.inflate(R.layout.fragment_settings, container, false)
        switchButton = view?.findViewById(R.id.switchButton)

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item : MenuItem? = menu?.findItem(R.id.action_sort)
        item?.isVisible = false
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val prefs = myActivity?.getSharedPreferences(MY_SHAKE_PREFS,Context.MODE_PRIVATE)
        val isallowed = prefs?.getBoolean("feature", false)
        switchButton?.isChecked = isallowed == true

        switchButton?.setOnCheckedChangeListener {compoundButton, b ->
            if(b) {

                val editor = myActivity?.getSharedPreferences(MY_SHAKE_PREFS, Context.MODE_PRIVATE)?.edit()
                editor?.putBoolean("feature", true)
                editor?.apply()

            } else {

                val editor = myActivity?.getSharedPreferences(MY_SHAKE_PREFS, Context.MODE_PRIVATE)?.edit()
                editor?.putBoolean("feature", false)
                editor?.apply()
            }
        }
    }



}
