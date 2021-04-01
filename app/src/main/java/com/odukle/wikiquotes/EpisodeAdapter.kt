package com.odukle.wikiquotes

import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

private const val TAG = "EpisodeAdapter"
private const val ITEM_TYPE_QUOTE = 0
private const val ITEM_TYPE_AD = 1

class EpisodeAdapter(
    private var quoteList: List<Any>,
    private val listener: OnQuoteClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    inner class QuotesViewHolderEA(view: View) : RecyclerView.ViewHolder(view) {
        val quoteTv: AppCompatTextView = view.findViewById(R.id.quote_tv_ep)
        val quoteCard: CardView = view.findViewById(R.id.quote_card)
        private val btnWhatsApp: ImageView = view.findViewById(R.id.btn_whatsapp)
        private val btnShare: ImageView = view.findViewById(R.id.btn_share)
        private val btnCopy: ImageView = view.findViewById(R.id.btn_copy)
        val btnLayout: LinearLayout = view.findViewById(R.id.btn_layout)

        init {

            btnWhatsApp.setOnClickListener {
                val quoteToCopy = quoteTv.text.toString()
                listener.onWhatsAppClick(quoteToCopy)
            }

            btnShare.setOnClickListener {
                val quoteToCopy = quoteTv.text.toString()
                listener.onShareClick(quoteToCopy)
            }

            btnCopy.setOnClickListener {
                val quoteToCopy = quoteTv.text.toString()
                listener.onCopyClick(quoteToCopy)
            }
        }

    }

    inner class AdViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val adView: AdView = view.findViewById(R.id.ad_view)
    }

    interface OnQuoteClickListener {
        fun onWhatsAppClick(quote: String)
        fun onShareClick(quote: String)
        fun onCopyClick(quote: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TYPE_AD -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.banner_ad_layout, parent, false)
                AdViewHolder(view)
            }

            else -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.episode_rv_layout, parent, false)
                QuotesViewHolderEA(view)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holderEA: RecyclerView.ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: from episode rv")


        when (getItemViewType(position)) {
            ITEM_TYPE_AD -> {
                (holderEA as AdViewHolder).adView.loadAd(MainActivity.adRequest)
            }

            else -> {

                val quote = quoteList[position] as String
                (holderEA as QuotesViewHolderEA).quoteTv.fixTextSelection()

                if (quote.startsWith("`")) {
                    holderEA.quoteCard.visibility = View.VISIBLE

                    holderEA.quoteTv.gravity = Gravity.CENTER
                    holderEA.quoteTv.typeface = ResourcesCompat.getFont(MainActivity.instance, R.font.louis_george_cafe_bold)
                    holderEA.quoteTv.textSize = 30F
                    holderEA.quoteTv.text = quote.replace("`", "")
                    holderEA.btnLayout.visibility = View.GONE

                    if (quote == "`" || quote == "`[edit]") {
                        holderEA.quoteCard.visibility = View.GONE
                    }

                } else {
                    holderEA.quoteCard.visibility = View.VISIBLE

                    holderEA.quoteTv.typeface = ResourcesCompat.getFont(MainActivity.instance, R.font.louis_george_cafe)
                    holderEA.quoteTv.gravity = Gravity.NO_GRAVITY

                    holderEA.quoteTv.textSize = 18F
                    holderEA.btnLayout.visibility = View.VISIBLE

                    val sb = StringBuilder()
                    quote.lines().forEach {
                        if (it.contains(":")) {
                            if (it.length > 10) {
                                val startText = it.substring(0, 10)
                                if (quote.indexOf(startText) != 1) {
                                    sb.append("\n\n$it")
                                } else {
                                    sb.append(it)
                                }
                            } else {
                                sb.append("\n\n$it")
                            }
                        }
                    }

                    val mQuote = sb.toString()
                    val quoteTv = holderEA.quoteTv
                    if (mQuote.isEmpty() || mQuote.isBlank()) {
                        quoteTv.text = quote
                        if (quote.isEmpty() || quote.isBlank()) {
                            holderEA.quoteCard.visibility = View.GONE
                        }
                    } else {
                        quoteTv.text = mQuote
                    }
                }

            }

        }

    }

    override fun getItemCount(): Int {
        return quoteList.size
    }

    override fun getItemId(position: Int): Long {
        return quoteList[position].hashCode().toLong()
    }

    override fun getItemViewType(position: Int): Int {
        if (quoteList[position] is String) {
            return ITEM_TYPE_QUOTE
        } else if (quoteList[position] is AdRequest) {
            return ITEM_TYPE_AD
        }

        return ITEM_TYPE_QUOTE
    }

    fun TextView.fixTextSelection() {
        setTextIsSelectable(false)
        post { setTextIsSelectable(true) }
    }
}