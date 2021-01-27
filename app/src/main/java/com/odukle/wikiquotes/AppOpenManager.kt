package com.odukle.wikiquotes

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import java.util.*

private const val AD_UNIT_ID = "ca-app-pub-9193191601772541/8893840988"
const val PREF = "pref"
const val AD_COUNT = "adCount"

private const val TAG = "AppOpenManager"

class AppOpenManager(private val myApplication: MyApplication) :
    Application.ActivityLifecycleCallbacks, LifecycleObserver {

    private var appOpenAd: AppOpenAd? = null
    private lateinit var loadCallback: AppOpenAdLoadCallback
    private var currentActivity: Activity? = null
    private var isShowingAd = false
    private var loadTime: Long = 0

    init {
        myApplication.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    /** Request an ad  */
    fun fetchAd() {
        if (isAdAvailable()) {
            return
        }

        loadCallback = object : AppOpenAdLoadCallback() {

            override fun onAppOpenAdLoaded(ad: AppOpenAd) {
                appOpenAd = ad
                loadTime = Date().time
            }

            override fun onAppOpenAdFailedToLoad(loadAdError: LoadAdError) {
                // Handle the error.
            }
        }

        val request = getAdRequest()
        AppOpenAd.load(
            myApplication, AD_UNIT_ID, request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback
        )
    }

    /** Creates and returns ad request.  */
    private fun getAdRequest(): AdRequest? {
        return AdRequest.Builder().build()
    }

    /** Utility method that checks if ad exists and can be shown.  */
    fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    @OnLifecycleEvent(ON_START)
    fun onStart() {
        showAdIfAvailable()
        Log.d(TAG, "onStart")
    }

    fun showAdIfAvailable() {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        val sharedPreferences =
            SplashActivity.instance.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val adCount = sharedPreferences.getInt(AD_COUNT, 0)
        if (!isShowingAd && isAdAvailable() && adCount > 3) {
            Log.d(TAG, "Will show ad.")
            sharedPreferences.edit().putInt(AD_COUNT, 0).apply()
            val fullScreenContentCallback: FullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        // Set the reference to null so isAdAvailable() returns false.
                        appOpenAd = null
                        isShowingAd = false
                        fetchAd()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {}
                    override fun onAdShowedFullScreenContent() {
                        isShowingAd = true
                    }
                }
            appOpenAd?.show(currentActivity, fullScreenContentCallback)
        } else {
            Log.d(TAG, "Can not show ad.")
            fetchAd()
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
        val sharedPreferences =
            SplashActivity.instance.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        var adCount = sharedPreferences.getInt(AD_COUNT, 0)
        adCount++
        sharedPreferences.edit().putInt(AD_COUNT, adCount).apply()
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = null
    }
}