package com.odukle.wikiquotes

import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "SearchAdapter"

class SearchAdapter(private var map: Map<String, String>) :
    RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    inner class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val result_tv = view.findViewById<TextView>(R.id.search_result_tv)
        val result_card = view.findViewById<CardView>(R.id.search_result_card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.search_rv_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val title = map.keys.elementAt(position)
        holder.result_tv.text = title
        holder.result_card.setOnClickListener {
            if (isOnline(MainActivity.instance)) {
                MainActivity.instance.progress_bar.visibility = View.VISIBLE
                MainActivity.instance.search_rv.visibility = View.GONE
                MainActivity.instance.layout_main.gravity = Gravity.CENTER
                val link = map.values.elementAt(position)
                Log.d(TAG, "onBindViewHolder: link --> $link")
                MainActivity.getFilmQuotes(link).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d(TAG, "onBindViewHolder: task success")
                        val seasonMap = it.result as MutableMap<String, Any>
                        Log.d(TAG, "onBindViewHolder: seasonMap --> ${seasonMap?.size}")
                        val viewPager = MainActivity.instance.view_pager
                        val tabLayout = MainActivity.instance.tab_layout

                        seasonMap.remove("See also")
                        seasonMap.remove("References")
                        viewPager.adapter = ViewPagerAdapter(seasonMap)
                        MainActivity.instance.search_rv.visibility = View.GONE

                        MainActivity.instance.progress_bar.visibility = View.GONE
                        MainActivity.instance.layout_main.gravity = Gravity.NO_GRAVITY
                        MainActivity.instance.toolbar.title = title
                        MainActivity.instance.app_icon.visibility = View.GONE
                        viewPager.visibility = View.VISIBLE
                        tabLayout.visibility = View.VISIBLE
                    }
                }
            } else {
                Toast.makeText(MainActivity.instance, "No internet!", Toast.LENGTH_SHORT).show()
            }

        }
    }

    override fun getItemCount(): Int {
        return map.size
    }
}