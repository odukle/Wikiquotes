package com.odukle.wikiquotes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "ViewPagerAdapter"
class ViewPagerAdapter(private var seasonMap: Map<String, Any>) : RecyclerView.Adapter<ViewPagerAdapter.PageViewHolder>() {

    inner class PageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val episodesRv = view.findViewById<RecyclerView>(R.id.episodes_rv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        return PageViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.view_pager_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val key = seasonMap.keys.elementAt(position)
        holder.episodesRv.adapter = EpisodeAdapter(seasonMap[key] as Map<String, Any>)
        holder.episodesRv.layoutManager = LinearLayoutManager(holder.itemView.context)

        val viewPager = MainActivity.instance.view_pager
        val tabLayout = MainActivity.instance.tab_layout

        TabLayoutMediator(tabLayout, viewPager) { tab, pos ->
            val title = seasonMap.keys.elementAt(pos)
            tab.text = if (title.isEmpty()) "N/A" else title
        }.attach()
    }

    override fun getItemCount(): Int {
        return seasonMap.size
    }
}