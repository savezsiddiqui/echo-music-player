package com.internshala.echo.Fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import com.internshala.echo.R



class AboutUsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        activity?.title = "About Me"
        return inflater.inflate(R.layout.fragment_about_us, container, false)
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




}
