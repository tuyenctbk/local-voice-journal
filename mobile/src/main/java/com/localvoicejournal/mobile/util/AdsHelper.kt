package com.localvoicejournal.mobile.util

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

object AdsHelper {

    private var mInterstitialAd: InterstitialAd? = null
    private var isListeningToLoad = false

    /**
     * Initializes the Mobile Ads SDK and preloads the first interstitial ad.
     */
    fun initialize(context: android.content.Context) {
        MobileAds.initialize(context) {
            loadInterstitial(context.applicationContext)
        }
    }

    /**
     * Preloads an Interstitial Ad.
     */
    fun loadInterstitial(context: android.content.Context) {
        if (mInterstitialAd != null || isListeningToLoad) return
        isListeningToLoad = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            "ca-app-pub-3940256099942544/1033173712", // Interstitial Test ID
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    isListeningToLoad = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    mInterstitialAd = null
                    isListeningToLoad = false
                }
            }
        )
    }

    /**
     * Shows a preloaded interstitial ad if ready.
     */
    fun showInterstitial(context: android.content.Context, isPremium: Boolean) {
        if (isPremium) return
        
        val activity = findActivity(context)
        if (activity == null) {
            android.util.Log.e("AdsHelper", "Context must be an Activity to show Interstitial Ad.")
            loadInterstitial(context.applicationContext)
            return
        }

        val ad = mInterstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mInterstitialAd = null
                    loadInterstitial(context.applicationContext)
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    mInterstitialAd = null
                    loadInterstitial(context.applicationContext)
                }
            }
            ad.show(activity)
        } else {
            android.util.Log.d("AdsHelper", "Interstitial Ad is not ready yet.")
            loadInterstitial(context.applicationContext)
        }
    }

    private fun findActivity(context: android.content.Context): Activity? {
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is Activity) {
                return ctx
            }
            ctx = ctx.baseContext
        }
        return null
    }

    /**
     * Anchored Adaptive Banner ad for mobile and tablet devices.
     */
    @Composable
    fun BannerAd(modifier: Modifier = Modifier, isPremium: Boolean = false) {
        if (isPremium) return
        
        if (LocalInspectionMode.current) {
            BannerAdPlaceholder(modifier)
            return
        }

        val context = LocalContext.current
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp

        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(
                        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                            ctx,
                            screenWidth
                        )
                    )
                    adUnitId = "ca-app-pub-3940256099942544/9214589741" // Anchored Adaptive Banner Test ID
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }

    @Composable
    private fun BannerAdPlaceholder(modifier: Modifier = Modifier) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color(0xFF141225))
                .border(1.dp, Color(0xFF2C2750), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Ad Banner [Test / Preview]", color = Color(0xFF4C4670), fontSize = 10.sp)
        }
    }

    /**
     * Native ad designed to look like a journal entry.
     * Ideal for the History list (DashboardScreen).
     */
    @Composable
    fun NativeAd(modifier: Modifier = Modifier, isPremium: Boolean = false) {
        if (isPremium) return

        if (LocalInspectionMode.current) {
            NativeAdPlaceholder(modifier)
            return
        }

        val context = LocalContext.current
        var nativeAdState by remember { mutableStateOf<NativeAd?>(null) }
        var loadFailed by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            val adLoader = AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110") // Native Advanced Test ID
                .forNativeAd { ad ->
                    nativeAdState = ad
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        loadFailed = true
                    }
                })
                .build()
            adLoader.loadAd(AdRequest.Builder().build())
        }

        DisposableEffect(nativeAdState) {
            onDispose {
                nativeAdState?.destroy()
            }
        }

        val currentAd = nativeAdState
        if (currentAd != null && !loadFailed) {
            AndroidView(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF16132C).copy(alpha = 0.4f))
                    .border(1.dp, Color(0xFF2C2750), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                factory = { ctx ->
                    val adView = NativeAdView(ctx)
                    val container = android.widget.LinearLayout(ctx).apply {
                        orientation = android.widget.LinearLayout.VERTICAL
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }

                    // Header layout
                    val header = android.widget.LinearLayout(ctx).apply {
                        orientation = android.widget.LinearLayout.HORIZONTAL
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        gravity = android.view.Gravity.CENTER_VERTICAL
                    }

                    val titleText = android.widget.TextView(ctx).apply {
                        setTextColor(android.graphics.Color.WHITE)
                        textSize = 14f
                        setTypeface(null, android.graphics.Typeface.BOLD)
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            0,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                    }
                    adView.headlineView = titleText
                    header.addView(titleText)

                    val badge = android.widget.TextView(ctx).apply {
                        text = "Ad"
                        setTextColor(android.graphics.Color.WHITE)
                        textSize = 9f
                        setTypeface(null, android.graphics.Typeface.BOLD)
                        setBackgroundColor(android.graphics.Color.parseColor("#7A60FF"))
                        setPadding(12, 4, 12, 4)
                    }
                    header.addView(badge)
                    container.addView(header)

                    // Spacer
                    val spacer = android.view.View(ctx).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            8.dpToPx(ctx)
                        )
                    }
                    container.addView(spacer)

                    // Body text
                    val bodyText = android.widget.TextView(ctx).apply {
                        setTextColor(android.graphics.Color.parseColor("#C0B3FF"))
                        textSize = 13f
                    }
                    adView.bodyView = bodyText
                    container.addView(bodyText)

                    adView.addView(container)
                    adView
                },
                update = { adView ->
                    val titleText = adView.headlineView as? android.widget.TextView
                    val bodyText = adView.bodyView as? android.widget.TextView

                    titleText?.text = currentAd.headline
                    bodyText?.text = currentAd.body

                    adView.setNativeAd(currentAd)
                }
            )
        } else {
            NativeAdPlaceholder(modifier)
        }
    }

    private fun Int.dpToPx(context: android.content.Context): Int {
        val density = context.resources.displayMetrics.density
        return (this * density).toInt()
    }

    @Composable
    private fun NativeAdPlaceholder(modifier: Modifier = Modifier) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF16132C).copy(alpha = 0.4f))
                .border(1.dp, Color(0xFF2C2750), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SPONSORED",
                        color = Color(0xFFC0B3FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF7A60FF), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Ad", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "AuraJournal Premium: Experience the deepest sensory reflections without any distractions.",
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
