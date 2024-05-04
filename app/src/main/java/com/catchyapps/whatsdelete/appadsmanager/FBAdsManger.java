package com.catchyapps.whatsdelete.appadsmanager;


import static com.catchyapps.whatsdelete.appadsmanager.AppAdmobManager.getPixelFromDp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.catchyapps.whatsdelete.BaseApplication;
import com.catchyapps.whatsdelete.R;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdExtendedListener;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdBase;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeAdViewAttributes;
import com.facebook.ads.NativeBannerAd;
import com.facebook.ads.NativeBannerAdView;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;


public class FBAdsManger {

    //todo for native banner facebook
    private NativeBannerAd mNativeBannerAd;
    private final NativeBannerAdView.Type mViewType = NativeBannerAdView.Type.HEIGHT_50;
    private static final int COLOR_LIGHT_GRAY = 0xff90949c;
    private static final int COLOR_CTA_BLUE_BG = 0xff4080ff;


    private InterstitialAd fbInterstitialAd;
    private static NativeAd nativeAd;
    private static AdOptionsView adOptionsView;
    //facebook banner ads
    private AdView bannerAdView;
    BaseApplication baseApplication;
    int adPriority;


    /**
     * Constructor
     */
    public FBAdsManger(BaseApplication baseApplication, int adPriority) {
        this.baseApplication = baseApplication;
        this.adPriority = adPriority;

    }

    public InterstitialAd getFbInterstitialAd() {
        return fbInterstitialAd;
    }

    /**
     * Load Facebook Interstitial
     */
    public void loadFbInterstitial() {
        if (fbInterstitialAd != null) {
            fbInterstitialAd.destroy();
            fbInterstitialAd = null;
        }

        InterstitialAdExtendedListener interstitialAdExtendedListener = new InterstitialAdExtendedListener() {
            @Override
            public void onInterstitialActivityDestroyed() {

            }

            @Override
            public void onInterstitialDisplayed(Ad ad) {

            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                loadFbInterstitial();
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                //  Utilit.logCat("loadFbInterstitial: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }

            @Override
            public void onRewardedAdCompleted() {

            }

            @Override
            public void onRewardedAdServerSucceeded() {

            }

            @Override
            public void onRewardedAdServerFailed() {

            }
        };

        fbInterstitialAd = new InterstitialAd(baseApplication, baseApplication.getResources()
                .getString(R.string.facebook_interstitial_id));
        // Load a new interstitial.
        InterstitialAd.InterstitialLoadAdConfig loadAdConfig = fbInterstitialAd
                .buildLoadAdConfig()
                // Set a listener to get notified on changes
                // or when the user interact with the ad.
                .withAdListener(interstitialAdExtendedListener)
                // .withCacheFlags(EnumSet.of(CacheFlag.VIDEO))
                //.withRewardData(new RewardData("YOUR_USER_ID", "YOUR_REWARD", 10))
                .build();
        fbInterstitialAd.loadAd(loadAdConfig);
    }

    /**
     * Request to Facebook Banner Ad
     */


    private int getBannerHeightInPixel() {
        Display display = ((WindowManager) baseApplication
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        final float scale = outMetrics.density;
        return (int) (50 * scale + 0.5f);
    }

    public void fbBannerAdView(ViewGroup adContainerView) {
        adContainerView.getLayoutParams().height = getBannerHeightInPixel();
        addPlaceHolderTextView(adContainerView);

        AdListener adListener = new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                //adMobManager.adMobAdaptiveBanner(adContainerView);
                //   Utility.logCat("fbBannerAdView: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Reposition the ad and add it to the view hierarchy.
/*                adContainerView.removeAllViews();
                adContainerView.addView(bannerAdView);*/
                if (bannerAdView.getParent() != null) {
                    ((ViewGroup) bannerAdView.getParent()).removeView(bannerAdView); // <- fix
                }
                adContainerView.addView(bannerAdView);
            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        };
        // app settings). Use different ID for each ad placement in your app.
        bannerAdView = new AdView(baseApplication, baseApplication.getResources().
                getString(R.string.facebook_banner_ads_id), AdSize.BANNER_HEIGHT_50);

        // Initiate a request to load an ad.
        bannerAdView.loadAd(bannerAdView.buildLoadAdConfig().withAdListener(adListener).build());
    }

    private void addPlaceHolderTextView(ViewGroup adContainerView) {
        TextView valueTV = new TextView(baseApplication);
        valueTV.setText("Loading Ad...");
        valueTV.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        valueTV.setGravity(Gravity.CENTER);
        adContainerView.addView(valueTV);
    }

    //  facebook Native Banner
    public void fbNativeBanner(ViewGroup mNativeAdContainer) {
        try {
            NativeAdListener nativeAdListener = new NativeAdListener() {
                @Override
                public void onMediaDownloaded(Ad ad) {
                }

                @Override
                public void onError(Ad ad, AdError adError) {
                    //   Utility.logCat("fbNativeBanner: " + adError.getErrorMessage());
                }

                @Override
                public void onAdLoaded(Ad ad) {

                    if (mNativeAdContainer == null) {
                        return;
                    }
                    mNativeAdContainer.removeAllViews();
                    // Create a NativeAdViewAttributes object and set the attributes
                    NativeAdViewAttributes attributes = new NativeAdViewAttributes(baseApplication)
                            .setBackgroundColor(Color.WHITE)
                            .setTitleTextColor(Color.GRAY)
                            .setDescriptionTextColor(COLOR_LIGHT_GRAY)
                            .setButtonBorderColor(COLOR_CTA_BLUE_BG)
                            .setButtonTextColor(Color.WHITE)
                            .setButtonColor(COLOR_CTA_BLUE_BG);

                    // Use NativeAdView.render to generate the ad View
                    View adView = NativeBannerAdView.render(baseApplication, mNativeBannerAd,
                            mViewType, attributes);

                    // Add adView to the container showing Ads
                    mNativeAdContainer.addView(adView, 0);
                    mNativeAdContainer.setBackgroundColor(Color.TRANSPARENT);
                }

                @Override
                public void onAdClicked(Ad ad) {
                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            };

            mNativeBannerAd = new NativeBannerAd(baseApplication, baseApplication.getResources().getString(R.string.facebook_native_banner_id));
            mNativeBannerAd.loadAd(mNativeBannerAd.buildLoadAdConfig().withAdListener(nativeAdListener).build());
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inflagte  Facebook Native Ad
     */

    private void inflateFacbookNativeAd(NativeAd nativeAd, View adView) {

        // Create native UI using the ad metadata.
        MediaView nativeAdIcon = adView.findViewById(R.id.native_ad_icon);
        TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
        TextView nativeAdBody = adView.findViewById(R.id.native_ad_body);
        TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
        TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
        Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);

        MediaView nativeAdMedia = adView.findViewById(R.id.native_ad_media);

        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        nativeAdCallToAction.setVisibility(
                nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdTitle.setText(nativeAd.getAdvertiserName());
        nativeAdBody.setText(nativeAd.getAdBodyText());
        sponsoredLabel.setText("Sponsored");

        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdIcon);
        clickableViews.add(nativeAdMedia);
        clickableViews.add(nativeAdCallToAction);
        nativeAd.registerViewForInteraction(
                adView,
                nativeAdMedia,
                nativeAdIcon,
                clickableViews);
        NativeAdBase.NativeComponentTag.tagView(nativeAdIcon, NativeAdBase.NativeComponentTag.AD_ICON);
        NativeAdBase.NativeComponentTag.tagView(nativeAdTitle, NativeAdBase.NativeComponentTag.AD_TITLE);
        NativeAdBase.NativeComponentTag.tagView(nativeAdBody, NativeAdBase.NativeComponentTag.AD_BODY);
        NativeAdBase.NativeComponentTag.tagView(nativeAdSocialContext, NativeAdBase.NativeComponentTag.AD_SOCIAL_CONTEXT);
        NativeAdBase.NativeComponentTag.tagView(nativeAdCallToAction, NativeAdBase.NativeComponentTag.AD_CALL_TO_ACTION);
    }


    @SuppressLint("ClickableViewAccessibility")
    public void showFbNativeAds(final ViewGroup adContainerLayout, final View alternateView) {

        if (alternateView != null) {
            adContainerLayout.getLayoutParams().height = getPixelFromDp(baseApplication, 240);
        }

        NativeAdListener nativeAdListener = new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                if (alternateView != null)
                    alternateView.setVisibility(View.VISIBLE);


            }

            @Override
            public void onAdLoaded(Ad ad) {
                try {
                    if (nativeAd != null && adContainerLayout != null) {
                        if (alternateView != null) {
                            alternateView.setVisibility(View.GONE);
                        }
                        adContainerLayout.setVisibility(View.VISIBLE);
                        LayoutInflater layoutInflater = (LayoutInflater) baseApplication.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        FrameLayout container = (FrameLayout) layoutInflater.inflate(R.layout.fb_native_view_container_layout, null);
                        LinearLayout adChoicesContainer = container.findViewById(R.id.ad_choices_container);
                        NativeAdLayout nativeAdLayout = container.findViewById(R.id.native_ad_container);
                        if (adChoicesContainer != null) {
                            adOptionsView = new AdOptionsView(baseApplication, nativeAd, nativeAdLayout);
                            adChoicesContainer.removeAllViews();
                            adChoicesContainer.addView(adOptionsView, 0);
                        }

                        inflateFacbookNativeAd(nativeAd, nativeAdLayout);

                        adContainerLayout.removeAllViews();
                        adContainerLayout.addView(container);

                        // Registering a touch listener to log which ad component receives the touch event.
                        // We always return false from onTouch so that we don't swallow the touch event (which
                        // would prevent click events from reaching the NativeAd control).
                        // The touch listener could be used to do animations.
                        nativeAd.setOnTouchListener((view, event) -> {
                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                int i = view.getId();
                                if (i == R.id.native_ad_call_to_action) {
                                    Timber.d("Call to action button clicked");
                                } else if (i == R.id.native_ad_media) {
                                    Timber.d("Main image clicked");
                                } else {
                                    Timber.d("Other ad component clicked");
                                }
                            }
                            return false;
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        };
        nativeAd = new NativeAd(baseApplication, baseApplication.getResources().getString(R.string.facebook_native_ads_id));
        nativeAd.loadAd(nativeAd.buildLoadAdConfig().withMediaCacheFlag(NativeAdBase.MediaCacheFlag.ALL).withAdListener(nativeAdListener).build());
    }

    public void customNativeBanner(ViewGroup mNativeAdContainer, View alternateView) {

        mNativeAdContainer.getLayoutParams().height = getPixelFromDp(baseApplication, 60);


        NativeAdListener nativeAdListener = new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                if (alternateView != null)
                    alternateView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdLoaded(Ad ad) {

                if (mNativeBannerAd == null || mNativeBannerAd != ad) {
                    return;
                }

                if (alternateView != null)
                    alternateView.setVisibility(View.GONE);
                // Inflate Native Banner Ad into Container
                inflateNativeBanner(mNativeAdContainer, mNativeBannerAd);
            }

            @Override
            public void onAdClicked(Ad ad) {
            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        };

        mNativeBannerAd = new NativeBannerAd(baseApplication, baseApplication.getResources().getString(R.string.facebook_native_banner_id));
        mNativeBannerAd.loadAd(mNativeBannerAd.buildLoadAdConfig().withAdListener(nativeAdListener).build());
    }

    private void inflateNativeBanner(ViewGroup mNativeLayout, NativeBannerAd nativeBannerAd) {
        // Unregister last ad
        nativeBannerAd.unregisterView();

        // Add the Ad view into the ad container.
        LayoutInflater layoutInflater = (LayoutInflater) baseApplication.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Inflate the Ad view.  The layout referenced is the one you created in the last step.
        NativeAdLayout nativeAdLayout = (NativeAdLayout) layoutInflater.inflate(R.layout.layout_fb_banner_native_, mNativeLayout, false);
        mNativeLayout.removeAllViews();
        mNativeLayout.addView(nativeAdLayout);
        // Add the AdChoices icon
        RelativeLayout adChoicesContainer = nativeAdLayout.findViewById(R.id.ad_choices_container);
        AdOptionsView adOptionsView = new AdOptionsView(baseApplication, nativeBannerAd, nativeAdLayout);
        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);

        // Create native UI using the ad metadata.
        TextView nativeAdTitle = nativeAdLayout.findViewById(R.id.native_ad_title);
        TextView nativeAdSocialContext = nativeAdLayout.findViewById(R.id.native_ad_social_context);
        TextView sponsoredLabel = nativeAdLayout.findViewById(R.id.native_ad_sponsored_label);
        MediaView nativeAdIconView = nativeAdLayout.findViewById(R.id.native_icon_view);
        Button nativeAdCallToAction = nativeAdLayout.findViewById(R.id.native_ad_call_to_action);

        // Set the Text.
        nativeAdCallToAction.setText(nativeBannerAd.getAdCallToAction());
        nativeAdCallToAction.setVisibility(
                nativeBannerAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdTitle.setText(nativeBannerAd.getAdvertiserName());
        nativeAdSocialContext.setText(nativeBannerAd.getAdSocialContext());
        sponsoredLabel.setText(nativeBannerAd.getSponsoredTranslation());

        // Register the Title and CTA button to listen for clicks.
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdCallToAction);
        nativeBannerAd.registerViewForInteraction(nativeAdLayout, nativeAdIconView, clickableViews);
    }
}

