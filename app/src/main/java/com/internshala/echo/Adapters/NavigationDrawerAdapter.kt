package com.internshala.echo.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.internshala.echo.Activities.MainActivity
import com.internshala.echo.Fragments.AboutUsFragment
import com.internshala.echo.Fragments.FavouritesFragment
import com.internshala.echo.Fragments.MainScreenFragment
import com.internshala.echo.Fragments.SettingsFragment
import com.internshala.echo.R

class NavigationDrawerAdapter(_contentList : ArrayList<String>,
                              _getImages : IntArray, _context : Context)
    : RecyclerView.Adapter<NavigationDrawerAdapter.NavViewHolder>(){

    var contentList : ArrayList<String>? = null
    var getImages : IntArray? = null
    var mContext : Context? = null

    init {
        this.contentList = _contentList
        this.getImages = _getImages
        this.mContext = _context
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavViewHolder {
        var itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_custom_navigationdrawer,
                        parent, false)

        val returnThis = NavViewHolder(itemView)
        return returnThis
    }

    override fun getItemCount(): Int {
        return (contentList as ArrayList).size
    }

    override fun onBindViewHolder(holder: NavViewHolder, position: Int) {
        holder?.iconGET?.setBackgroundResource(getImages?.get(position) as Int)
        holder?.textGET?.setText(contentList?.get(position))

        holder.contentHolder?.setOnClickListener {

            if(position == 0){
                val mainScreenFragment = MainScreenFragment()
                (mContext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.detailsFragment, mainScreenFragment)
                        .commit()
            }
            else if(position == 1){
                val favouritesFragment = FavouritesFragment()
                (mContext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.detailsFragment, favouritesFragment)
                        .commit()
            }
            else if(position == 2){
                val settingsFragment = SettingsFragment()
                (mContext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.detailsFragment, settingsFragment)
                        .commit()
            }
            else{
                val aboutUsFragment = AboutUsFragment()
                (mContext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.detailsFragment, aboutUsFragment)
                        .commit()
            }
            MainActivity.Statified.drawerLayout?.closeDrawers()
        }
    }


    class NavViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        var iconGET : ImageView? = null
        var textGET : TextView? = null
        var contentHolder : RelativeLayout? = null

        init{
            iconGET = itemView?.findViewById(R.id.icon_navdrawer)
            textGET = itemView?.findViewById(R.id.text_navdrawer)
            contentHolder = itemView?.findViewById(R.id.navDrawer_item_content_holder)
        }
    }
}