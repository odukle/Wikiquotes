package com.odukle.wikiquotes

import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "SearchAdapter"

class SearchAdapter(private var map: Map<String, String>) :
    RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    inner class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val resultTv: TextView = view.findViewById(R.id.search_result_tv)
        val resultCard: CardView = view.findViewById(R.id.search_result_card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.search_rv_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val title = map.keys.elementAt(position)
        holder.resultTv.text = title
        holder.resultCard.setOnClickListener {
            if (isOnline(MainActivity.instance)) {
                MainActivity.instance.progress_bar.visibility = View.VISIBLE
                MainActivity.instance.search_rv.visibility = View.GONE
                MainActivity.instance.layout_main.gravity = Gravity.CENTER
                val link = map.values.elementAt(position)
                Log.d(TAG, "onBindViewHolder: link --> $link")
                MainActivity.getFilmQuotes(link).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "onBindViewHolder: task success")
                        val seasonMap = task.result as MutableMap<String, Any>
                        val viewPager = MainActivity.instance.view_pager

                        seasonMap.remove("See also")
                        seasonMap.remove("References")

                        val sortedMap = seasonMap.toSortedMap()

                        viewPager.adapter = ViewPagerAdapter(sortedMap)
                        viewPager.offscreenPageLimit = sortedMap.size

                        MainActivity.instance.view_pager.visibility = View.VISIBLE
                        MainActivity.instance.tab_layout.visibility = View.VISIBLE
                        MainActivity.instance.search_layout.visibility = View.GONE
                        MainActivity.menuMain[0].isVisible = true
                        MainActivity.menuMain[0].icon = ContextCompat.getDrawable(
                            MainActivity.instance,
                            R.drawable.ic_baseline_search_24
                        )
                        MainActivity.instance.progress_bar.visibility = View.GONE
                        MainActivity.instance.toolbar.title = title
                        MainActivity.instance.search_rv.visibility = View.GONE
                        MainActivity.instance.app_icon.visibility = View.GONE
                        MainActivity.instance.layout_main.gravity = Gravity.NO_GRAVITY

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