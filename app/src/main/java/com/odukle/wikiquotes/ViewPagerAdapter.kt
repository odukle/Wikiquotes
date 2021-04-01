package com.odukle.wikiquotes

import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.sqrt


private const val TAG = "ViewPagerAdapter"

class ViewPagerAdapter(private var seasonMap: Map<String, Any>) :
    RecyclerView.Adapter<ViewPagerAdapter.PageViewHolder>() {

    inner class PageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val episodesRv: RecyclerView = view.findViewById(R.id.episodes_rv)
        val btnScrollUp: CardView = view.findViewById(R.id.btn_scroll_up)

        init {

            val layoutManager = LinearLayoutManager(episodesRv.context)
            episodesRv.layoutManager = layoutManager

            btnScrollUp.visibility = View.GONE
            episodesRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    when (newState) {
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            Handler(Looper.getMainLooper()).postDelayed({
                                btnScrollUp.visibility = View.GONE
                            }, 2500)
                        }

                        RecyclerView.SCROLL_STATE_DRAGGING, RecyclerView.SCROLL_STATE_SETTLING -> {
                            btnScrollUp.visibility = View.VISIBLE
                        }
                    }
                }
            })

            btnScrollUp.setOnClickListener {
                val smoothScroller: RecyclerView.SmoothScroller =
                    object : LinearSmoothScroller(btnScrollUp.context) {
                        override fun getVerticalSnapPreference(): Int {
                            return SNAP_TO_START
                        }
                    }

                smoothScroller.targetPosition = 0
                layoutManager.startSmoothScroll(smoothScroller)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        return PageViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.view_pager_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: from vp rv")

        val key = seasonMap.keys.elementAt(position)
        val eMap = seasonMap[key] as Map<String, Any>
        val sortedMap = eMap.toSortedMap(object : Comparator<String> {
            override fun compare(o1: String, o2: String): Int {
                val index1 = o1.indexOf("[")
                val index2 = o1.indexOf("]")
                val index3 = o2.indexOf("[")
                val index4 = o2.indexOf("]")

                if ((index1 != -1 && index2 != -1) && (index3 != -1 && index4 != -1)) {
                    val ep1 = o1.substring(index1, index2)
                    val ep2 = o2.substring(index3, index4)
                    return ep1.compareTo(ep2)
                }

                return o1.compareTo(o2)
            }
        })

        val mixedList = mutableListOf<Any>()
        val quoteList = mutableListOf<String>()
        for (episode in sortedMap.keys) {
            val epName = "`$episode"
            quoteList.add(epName)
            (sortedMap[episode] as Map<String, String>).forEach {
                quoteList.add(it.value)
            }
        }

        for (i in 0 until quoteList.size) {

            val metrics = DisplayMetrics()

            val yInches = metrics.heightPixels / metrics.ydpi
            val xInches = metrics.widthPixels / metrics.xdpi
            val diagonalInches = sqrt((xInches * xInches + yInches * yInches).toDouble())
            if (diagonalInches >= 6) {
                // 6.5inch device or bigger
                if (i % 8 == 0 && i != 0) {
                    mixedList.add(quoteList[i])
                    mixedList.add(MainActivity.adRequest)
                } else {
                    mixedList.add(quoteList[i])
                }
            } else {
                // smaller device
                if (i % 5 == 0 && i != 0) {
                    mixedList.add(quoteList[i])
                    mixedList.add(MainActivity.adRequest)
                } else {
                    mixedList.add(quoteList[i])
                }
            }

//            holder.btnScrollUp.visibility = View.GONE

        }


        val viewPager = MainActivity.instance.view_pager
        val tabLayout = MainActivity.instance.tab_layout

        holder.episodesRv.adapter = EpisodeAdapter(mixedList, MainActivity.instance)

        TabLayoutMediator(tabLayout, viewPager)
        { tab, pos ->
            val title = seasonMap.keys.elementAt(pos).replace("_", " ")
            tab.text = if (title.isEmpty()) "N/A" else title

        }.attach()


    }

    override fun getItemCount(): Int {
        return seasonMap.size
    }
}