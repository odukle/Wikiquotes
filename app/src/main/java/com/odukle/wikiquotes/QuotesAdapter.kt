package com.odukle.wikiquotes

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView


const val CLIP_DATA = "clipData"
private const val ITEM_TYPE_QUOTE = 0
private const val ITEM_TYPE_AD = 1

private const val TAG = "QuotesAdapter"

class QuotesAdapter(private var qList: List<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class QuotesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val quoteTv: TextView = view.findViewById(R.id.quote_tv)
        val btnWhatsApp: ImageView = view.findViewById(R.id.btn_whatsapp)
        val btnShare: ImageView = view.findViewById(R.id.btn_share)
        val btnCopy: ImageView = view.findViewById(R.id.btn_copy)
    }

    inner class AdViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val adView: AdView = view.findViewById(R.id.ad_view)
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
                        .inflate(R.layout.quotes_rv_layout, parent, false)
                QuotesViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (getItemViewType(position)) {

            ITEM_TYPE_QUOTE -> {
                val quote = qList[position] as String

                if (quote.isEmpty() || quote.isBlank()) {
                    (holder as QuotesViewHolder).quoteTv.visibility = View.GONE
                }

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

                if (mQuote.isEmpty() || mQuote.isBlank()) {
                    (holder as QuotesViewHolder).quoteTv.text = quote
                } else {
                    (holder as QuotesViewHolder).quoteTv.text = mQuote
                }

                val quoteToCopy = holder.quoteTv.text.toString()

                holder.btnWhatsApp.setOnClickListener {
                    val whatsappIntent = Intent(Intent.ACTION_SEND)
                    whatsappIntent.type = "text/plain"
                    whatsappIntent.setPackage("com.whatsapp")
                    whatsappIntent.putExtra(Intent.EXTRA_TEXT, quoteToCopy)
                    try {
                        MainActivity.instance.startActivity(whatsappIntent)
                    } catch (ex: ActivityNotFoundException) {
                        Toast.makeText(
                            MainActivity.instance,
                            "Whatsapp have not been installed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                holder.btnShare.setOnClickListener {
                    MainActivity.instance.startActivity(Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, quoteToCopy)
                    })
                }

                holder.btnCopy.setOnClickListener {
                    val clipBoard =
                        MainActivity.instance.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(CLIP_DATA, quoteToCopy)
                    clipBoard.setPrimaryClip(clip)
                    Toast.makeText(
                        MainActivity.instance,
                        "Quote copied to clipboard",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }

            ITEM_TYPE_AD -> {
                (holder as AdViewHolder).adView.loadAd(MainActivity.adRequest)
            }

        }


    }

    override fun getItemViewType(position: Int): Int {
        if (qList[position] is String) {
            return ITEM_TYPE_QUOTE
        } else if (qList[position] is AdRequest) {
            return ITEM_TYPE_AD
        }

        return ITEM_TYPE_QUOTE
    }

    override fun getItemCount(): Int {
        return qList.size
    }
}