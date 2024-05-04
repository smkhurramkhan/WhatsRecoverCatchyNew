package com.catchyapps.whatsdelete.appadsmanager;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.catchyapps.whatsdelete.BaseApplication;
import com.catchyapps.whatsdelete.R;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.Objects;

import timber.log.Timber;


public class AppAdmobManager {

    private InterstitialAd interstitialAd;
    private final BaseApplication baseApplication;
    int adPriority;
    private AdView bannerAdView;
    private final FBAdsManger adsFacebookManger;

    /**
     * Constructor
     */
    public AppAdmobManager(BaseApplication baseApplication, int adPriority) {
        this.baseApplication = baseApplication;
        this.adPriority = adPriority;
        MobileAds.initialize(baseApplication, initializationStatus -> {
        });
        adsFacebookManger = new FBAdsManger(baseApplication, adPriority);
    }

    public boolean checkConnection() {
        final ConnectivityManager connMgr = (ConnectivityManager) baseApplication.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr != null) {
            NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();
            if (activeNetworkInfo != null) { // connected to the internet
                // connected to the mobile provider's data plan
                if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    // connected to wifi
                    return true;
                } else return activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            }
        }
        return false;
    }


    public InterstitialAd getInterstitialAd() {
        return interstitialAd;
    }

    //  Load admob interstitial
    public void loadAdMobInterstitialAd() {
        if (!checkConnection())
            return;
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(baseApplication, baseApplication.getResources().getString(R.string.interstitial_id), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                AppAdmobManager.this.interstitialAd = interstitialAd;
                interstitialAd.setFullScreenContentCallback(fullScreenContentCallback);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                Timber.d(loadAdError.getMessage());
                interstitialAd = null;
            }
        });
    }

    FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
        @Override
        public void onAdDismissedFullScreenContent() {
            // Called when fullscreen content is dismissed.
            Timber.d("The ad was dismissed.");
            loadAdMobInterstitialAd();
        }

        @Override
        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
            // Called when fullscreen content failed to show.
            Timber.d("The ad failed to show.");
        }

        @Override
        public void onAdShowedFullScreenContent() {
            interstitialAd = null;
            Timber.d("The ad was shown.");
        }
    };

    // for admob custom native ads
    public void admobNativeBanner(ViewGroup mNativeAdContainer, View alternateView) {
        mNativeAdContainer.getLayoutParams().height = getPixelFromDp(baseApplication, 64);
        try {
            AdLoader adLoader = new AdLoader.Builder(baseApplication, baseApplication.getResources().getString(R.string.native_advanced))
                    .forNativeAd(NativeAd -> {

                        ColorDrawable cd = new ColorDrawable();
                        NativeTemplateStyle styles = new NativeTemplateStyle.Builder()
                                .withMainBackgroundColor(cd).build();
                        LayoutInflater layoutInflater = (LayoutInflater)
                                baseApplication.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        @SuppressLint("InflateParams")
                        LinearLayout cardView = (LinearLayout) layoutInflater
                                .inflate(R.layout.custom_native_ad_view_layout, null);


                        TemplateView templateView = cardView.findViewById(R.id.my_template);
                        templateView.setVisibility(View.VISIBLE);
                        templateView.setStyles(styles);
                        templateView.setNativeAd(NativeAd);

                        mNativeAdContainer.removeAllViews();
                        mNativeAdContainer.addView(cardView);
                        mNativeAdContainer.setVisibility(View.VISIBLE);

                        if (alternateView != null) {
                            alternateView.setVisibility(View.GONE);
                        }


                    }).withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            super.onAdFailedToLoad(loadAdError);
                            if (adPriority == 1) {
                                adsFacebookManger.customNativeBanner(mNativeAdContainer, alternateView);
                            } else {
                                alternateView.setVisibility(View.VISIBLE);
                            }

                        }
                    })
                    .build();
            adLoader.loadAd(new AdRequest.Builder().build());
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }


    private AdSize getAdSize() {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = ((WindowManager) baseApplication.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(baseApplication, adWidth);
    }


    //admob adaptive banner
    public void adMobAdaptiveBanner(ViewGroup adContainerView) {

        try {
            if (baseApplication != null) {
                bannerAdView = new AdView(baseApplication);
                bannerAdView.setAdUnitId(baseApplication.getResources().getString(R.string.banner_ad_unit_id));

                AdSize adSize = getAdSize();
                adContainerView.getLayoutParams().height = getPixelFromDp(baseApplication, 60);
                addPlaceHolderTextView(adContainerView);

                bannerAdView.setAdSize(adSize);
                bannerAdView.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        adContainerView.removeAllViews();
                        adContainerView.addView(bannerAdView);
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        Timber.d(loadAdError.getMessage());
                        adsFacebookManger.fbBannerAdView(adContainerView);
                    }

                    @Override
                    public void onAdClosed() {
                    }
                });

                AdRequest adRequest = new AdRequest.Builder().build();
                // Start loading the ad in the background.
                bannerAdView.loadAd(adRequest);
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    private void addPlaceHolderTextView(ViewGroup adContainerView) {
        TextView valueTV = new TextView(baseApplication);
        valueTV.setText(R.string.ad_loading);
        valueTV.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        valueTV.setGravity(Gravity.CENTER);
        adContainerView.addView(valueTV);
    }


    // admob native ads
    public void customNativeAd(ViewGroup nativeAdLayout, View itemPicker) {

        if (itemPicker == null) {
            nativeAdLayout.getLayoutParams().height = getPixelFromDp(baseApplication, 240);
            //addPlaceHolderTextView(nativeAdLayout);
        }

        @SuppressLint("InflateParams") AdLoader adLoader = new AdLoader.Builder(baseApplication, baseApplication.getResources().getString(R.string.native_advanced))
                .forNativeAd(unifiedNativeAd -> {

                    ColorDrawable cd = new ColorDrawable();
                    NativeTemplateStyle styles = new NativeTemplateStyle.Builder().withMainBackgroundColor(cd).build();
                    LayoutInflater layoutInflater = (LayoutInflater) baseApplication.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    FrameLayout cardView;
                    cardView = (FrameLayout) layoutInflater.inflate(R.layout.layout_native_ad_, null);
                    TemplateView templateView = cardView.findViewById(R.id.my_template);
                    templateView.setVisibility(View.VISIBLE);
                    templateView.setStyles(styles);
                    templateView.setNativeAd(unifiedNativeAd);


                    if (nativeAdLayout != null) {
                        nativeAdLayout.removeAllViews();
                        nativeAdLayout.addView(cardView);
                        nativeAdLayout.setVisibility(View.VISIBLE);
                        if (itemPicker != null)
                            itemPicker.setVisibility(View.GONE);
                    }


                }).withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        Timber.d(loadAdError.getMessage());
                        if (adPriority == 1) {
                            adsFacebookManger.showFbNativeAds(nativeAdLayout, itemPicker);
                        } else {
                            Objects.requireNonNull(itemPicker).setVisibility(View.VISIBLE);
                        }
                    }
                })
                .build();


        adLoader.loadAd(new AdRequest.Builder().build());
    }


    public static int getPixelFromDp(Application application, int dp) {
        Display display = ((WindowManager) application.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        final float scale = outMetrics.density;
        return (int) (dp * scale + 0.5f);
    }
}

