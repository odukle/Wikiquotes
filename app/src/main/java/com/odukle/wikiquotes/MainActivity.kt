package com.odukle.wikiquotes

import android.app.Activity
import android.content.*
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.tasks.Task
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.android.synthetic.main.activity_main.*
//import kotlinx.android.synthetic.main.quotes_rv_layout.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.net.SocketTimeoutException
import kotlin.collections.set

const val CLIP_DATA = "clipData"
private const val TAG = "MainActivity"
const val INTER_UNIT_ID = "ca-app-pub-9193191601772541/4804300588"  //test

class MainActivity : AppCompatActivity(), EpisodeAdapter.OnQuoteClickListener {

    private var mInterstitialAd: InterstitialAd? = null

    companion object {
        lateinit var functions: FirebaseFunctions
        lateinit var instance: MainActivity
        lateinit var adRequest: AdRequest
        lateinit var menuMain: Menu

        fun getFilmQuotes(link: String): Task<Map<String, Any>> {
            Log.d(TAG, "getFilmQuotes: called")
            val data = hashMapOf(
                "link" to link
            )

            return functions.getHttpsCallable("getFilmQuotes")
                .call(data)
                .continueWith { task ->
                    task.result?.data as Map<String, Any>
                }
        }

        fun isAdRequestInitialized(): Boolean {
            return this::adRequest.isInitialized
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_ios_24)
        toolbar.visibility = View.GONE

        functions = FirebaseFunctions.getInstance("asia-south1")
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        instance = this

        adRequest = AdRequest.Builder().build()

        layout_main.gravity = Gravity.CENTER
        search_rv.visibility = View.GONE
        tab_layout.visibility = View.GONE
        view_pager.visibility = View.GONE

        var iCount = 0
        btn_search.setOnClickListener {
            if (search_query.text.isNullOrEmpty()) {
                Toast.makeText(this, "empty query", Toast.LENGTH_SHORT).show()
            } else {
                if (isOnline(this)) {
                    iCount++

                    val fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Ad showed fullscreen content.")
                            mInterstitialAd = null
                        }
                    }

                    if (mInterstitialAd == null) {
                        InterstitialAd.load(
                            this,
                            INTER_UNIT_ID,
                            adRequest,
                            object : InterstitialAdLoadCallback() {
                                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                                    mInterstitialAd = interstitialAd
                                    interstitialAd.fullScreenContentCallback =
                                        fullScreenContentCallback

                                    if (iCount > 3) {
                                        interstitialAd.show(this@MainActivity)
                                        iCount = 0
                                    }
                                }

                                override fun onAdFailedToLoad(error: LoadAdError) {
                                    Log.e(TAG, "onAdFailedToLoad: ${error.message}")
                                }
                            })
                    } else {
                        mInterstitialAd!!.fullScreenContentCallback = fullScreenContentCallback
                        if (iCount > 3) {
                            mInterstitialAd!!.show(this)
                            iCount = 0
                        }

                    }

                    layout_main.gravity = Gravity.CENTER
                    progress_bar.visibility = View.VISIBLE
                    search_error_tv.visibility = View.GONE
                    tab_layout.visibility = View.GONE
                    view_pager.visibility = View.GONE
                    search_instructions.visibility = View.GONE
                    imm.hideSoftInputFromWindow(btn_search.windowToken, 0)

                    CoroutineScope(IO).launch {
                        val map = getSearchResults(search_query.text.toString())
                        if (map != null) {
                            withContext(Main) {

                                if (map.isEmpty()) {
                                    search_error_tv.visibility = View.VISIBLE
                                    val query = search_query.text.toString()
                                    search_error_tv.text = "No results for \"$query\""
                                } else {
                                    search_error_tv.visibility = View.GONE
                                }
                                val adapter = SearchAdapter(map)
                                search_rv.layoutManager = LinearLayoutManager(this@MainActivity)
                                search_rv.adapter = adapter
                                search_rv.visibility = View.VISIBLE
                                toolbar.title = "Search results"
                                Log.d(TAG, "onCreate: adapter set")
                            }

                        }

                        withContext(Main) {
                            progress_bar.visibility = View.GONE
                            layout_main.gravity = Gravity.NO_GRAVITY
                            toolbar.visibility = View.VISIBLE
                        }

                    }
                } else {
                    Toast.makeText(this, "No internet!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        search_query.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                btn_search.performClick()
                true
            } else false

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menuMain = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.other_apps -> {
                startActivity(Intent(this, OtherApps::class.java))
            }

            R.id.menu_search -> {
                if (search_layout.isVisible) {
                    search_layout.visibility = View.GONE
                    item.icon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_search_24)
                } else {
                    search_layout.visibility = View.VISIBLE
                    item.icon = ContextCompat.getDrawable(this, R.drawable.ic_round_search_off_24)
                }

            }

            android.R.id.home -> {
                onBackPressed()
            }
        }

        return true
    }

    private suspend fun getSearchResults(query: String): MutableMap<String, String>? {

        try {

            val client = OkHttpClient()
            val request: Request = Request.Builder()
                .url("https://en.wikiquote.org/w/api.php?format=json&action=opensearch&search=$query&prop=text")
                .get()
                .build()

            val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
                throwable.printStackTrace()
            }

            return withContext(CoroutineScope(IO + exceptionHandler).coroutineContext) {
                try {
                    val response: Response = client.newCall(request).execute()
                    val jsonArray = JSONArray(response.body()!!.string())

                    val seasonArray = jsonArray.getJSONArray(1)
                    val linkArray = jsonArray.getJSONArray(3)

                    val qMap = mutableMapOf<String, String>()
                    for (i in 0 until seasonArray.length()) {
                        val name = seasonArray.getString(i)
                        val link = linkArray.getString(i)
                        Log.d(TAG, "getSearchResults: $name --> $link")
                        qMap[name] = link
                    }
                    Log.d(TAG, "getSearchResults: ${qMap.size}")
                    qMap
                } catch (e: Exception) {
                    Log.e(TAG, "getSearchResults: ${e.stackTraceToString()}")
                    if (e is SocketTimeoutException) {
                        search_error_tv.visibility = View.VISIBLE
                        search_error_tv.text = "Network connection timeout (Poor internet connection)"
                    }
                    null
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "getSearchResults: ${e.stackTraceToString()}")
            return null
        }
    }

    override fun onBackPressed() {

        if (tab_layout.isVisible) {
            tab_layout.visibility = View.GONE
            view_pager.visibility = View.GONE
            search_rv.visibility = View.VISIBLE
            search_layout.visibility = View.VISIBLE
            menuMain[0].isVisible = false
            app_icon.visibility = View.VISIBLE
            toolbar.title = "search results"
        } else if (search_rv.isVisible) {
            search_error_tv.visibility = View.GONE
            search_rv.visibility = View.GONE
            layout_main.gravity = Gravity.CENTER
            toolbar.title = "wikiquote"
            toolbar.visibility = View.GONE
            search_layout.visibility = View.VISIBLE
            search_instructions.visibility = View.VISIBLE
        } else {
            search_error_tv.visibility = View.GONE
            super.onBackPressed()
        }

    }

    override fun onWhatsAppClick(quote: String) {
        val whatsappIntent = Intent(Intent.ACTION_SEND)
        whatsappIntent.type = "text/plain"
        whatsappIntent.setPackage("com.whatsapp")
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, quote)
        try {
            startActivity(whatsappIntent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "Whatsapp have not been installed.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onShareClick(quote: String) {
        startActivity(Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, quote)
        })
    }

    override fun onCopyClick(quote: String) {
        val clipBoard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(CLIP_DATA, quote)
        clipBoard.setPrimaryClip(clip)
        Toast.makeText(
            this,
            "Quote copied to clipboard",
            Toast.LENGTH_SHORT
        ).show()
    }

}

@RequiresApi(Build.VERSION_CODES.M)
fun isOnline(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities =
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    if (capabilities != null) {
        when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            }

            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            }

            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
    }
    return false
}