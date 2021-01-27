package com.odukle.wikiquotes

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "EpisodeAdapter"
class EpisodeAdapter(private var episodeMap: Map<String, Any>) : RecyclerView.Adapter<EpisodeAdapter.QuotesViewHolder>() {

    inner class QuotesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val episodeTv = view.findViewById<TextView>(R.id.episode_tv)
        val quoteRv = view.findViewById<RecyclerView>(R.id.quote_rv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuotesViewHolder {
        return QuotesViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.episode_rv_layout, parent, false)
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: QuotesViewHolder, position: Int) {
        val key = episodeMap.keys.elementAt(position)
        if (key.isEmpty() || key.isBlank()) {
            holder.episodeTv.visibility = View.GONE
        }
        holder.episodeTv.text = key
        val qMap = episodeMap[key] as MutableMap<String, Any>
        qMap.values.removeIf {
            (it as String).isBlank() || it.isEmpty()
        }

        val qList = qMap.values.toMutableList()
        val mixedList = mutableListOf<Any>()

        for (i in 0 until qList.size) {
            if ((i % 4 == 0 && i != 0) || (qList[i] as String).length > 600) {
                mixedList.add(qList[i])
                mixedList.add(MainActivity.adRequest)
            } else {
                mixedList.add(qList[i])
            }
        }

        holder.quoteRv.adapter = QuotesAdapter(mixedList)
        holder.quoteRv.layoutManager = LinearLayoutManager(holder.itemView.context)
    }

    override fun getItemCount(): Int {
        return episodeMap.size
    }
}