package com.inapp.vpn.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.inapp.vpn.Activities.MainActivity;
import com.inapp.vpn.R;
import com.inapp.vpn.AdapterWrappers.ServerListAdapterFree;
import com.inapp.vpn.Config;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.inapp.vpn.Utils.Constants;
import com.inapp.vpn.model.Countries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FragmentFree extends Fragment implements ServerListAdapterFree.RegionListAdapterInterface {
    private RecyclerView recyclerView;
    private ServerListAdapterFree adapter;
    private ArrayList<Countries> countryArrayList;
    int server;
    InterstitialAd mInterstitialAd;
    public com.facebook.ads.InterstitialAd facebookInterstitialAd;

    boolean isAds;
    private RelativeLayout animationHolder;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_two, container, false);
        recyclerView = view.findViewById(R.id.region_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        countryArrayList = new ArrayList<>();
        animationHolder = view.findViewById(R.id.animation_layout);

        adapter = new ServerListAdapterFree(getActivity());
        recyclerView.setAdapter(adapter);

        MobileAds.initialize(getActivity(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.e("REWARDED INITIALIZ", initializationStatus.getAdapterStatusMap().toString());
            }
        });

        if (getResources().getBoolean(R.bool.ads_switch) && getResources().getBoolean(R.bool.facebook_list_ads) && (!Config.ads_subscription && !Config.all_subscription&& !Config.vip_subscription)) {

            isAds = true;
        } else if (getResources().getBoolean(R.bool.ads_switch) && getResources().getBoolean(R.bool.admob_list_ads) && (!Config.ads_subscription && !Config.all_subscription && !Config.vip_subscription)) {

            isAds = true;
        } else {

            isAds = false;

        }

        if (MainActivity.type.equals("ad") && isAds) {
            AdView adView = new AdView(getContext());
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(MainActivity.indratech_fast_27640849_ad_banner_id);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdClicked() {
                    // Code to be executed when the user clicks on an ad.
                }

                @Override
                public void onAdClosed() {
                    // Code to be executed when the user is about to return
                    // to the app after tapping on an ad.
                }

                @Override
                public void onAdFailedToLoad(LoadAdError adError) {
                    // Code to be executed when an ad request fails.
                    Log.v("ADMOBBANNER", adError.toString());
                }

                @Override
                public void onAdImpression() {
                    // Code to be executed when an impression is recorded
                    // for an ad.
                }

                @Override
                public void onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    Log.v("ADMOBBANNER", "banner loaded");
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                }
            });

            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);

            RelativeLayout.LayoutParams bannerParameters =
                    new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
            bannerParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
            bannerParameters.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

            FrameLayout lytMain = view.findViewById(R.id.lytAd);
            lytMain.addView(adView, bannerParameters);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadServers();
    }

    private void loadServers() {
        ArrayList<Countries> servers = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(Constants.FREE_SERVERS);
            for (int i=0; i < jsonArray.length();i++){
                JSONObject object = (JSONObject) jsonArray.get(i);
                servers.add(new Countries(object.getString("serverName"),
                        object.getString("flag_url"),
                        object.getString("ovpnConfiguration"),
                        object.getString("vpnUserName"),
                        object.getString("vpnPassword")
                ));

                if((i % 2 == 0)&&(i > 0)){
                    if (!Config.vip_subscription && !Config.all_subscription && !MainActivity.type.equals("ad")) {
                        servers.add(null);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        animationHolder.setVisibility(View.GONE);
        adapter.setData(servers);
    }

    @Override
    public void onCountrySelected(Countries item) {

        if (isAds) {
            MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                    Log.e("REWARDED INITIALIZ", initializationStatus.getAdapterStatusMap().toString());


                    if(MainActivity.type.equals("ad")) {

                        AdRequest adRequest = new AdRequest.Builder().build();
                        InterstitialAd.load(getContext(), MainActivity.admob_interstitial_id, adRequest,
                                new InterstitialAdLoadCallback() {
                                    @Override
                                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                        // The mInterstitialAd reference will be null until
                                        // an ad is loaded.
                                        mInterstitialAd = interstitialAd;
                                        Log.i("INTERSTITIAL", "onAdLoaded");

                                        if (mInterstitialAd != null) {

                                            mInterstitialAd.show(getActivity());

                                            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                                @Override
                                                public void onAdDismissedFullScreenContent() {
                                                    // Called when fullscreen content is dismissed.
                                                    Log.d("TAG", "The ad was dismissed.");
                                                    Log.d("TESTAD", " dismissed update");
                                                }

                                                public void onAdFailedToShowFullScreenContent(AdError adError) {
                                                    // Called when fullscreen content failed to show.
                                                    Log.d("TAG", "The ad failed to show.");
                                                }

                                                @Override
                                                public void onAdShowedFullScreenContent() {
                                                    // Called when fullscreen content is shown.
                                                    // Make sure to set your reference to null so you don't
                                                    // show it a second time.
                                                    mInterstitialAd = null;
                                                    Log.d("TAG", "The ad was shown.");
                                                }
                                            });

                                        } else {
                                            Log.d("TAG", "The interstitial ad wasn't ready yet.");
                                        }
                                    }

                                    @Override
                                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                        // Handle the error
                                        Log.i("INTERSTITIAL", loadAdError.getMessage());
                                        mInterstitialAd = null;
                                    }
                                });
                    }else {
                        AdSettings.setIntegrationErrorMode(AdSettings.IntegrationErrorMode.INTEGRATION_ERROR_CALLBACK_MODE);


                        AudienceNetworkAds.initialize(getContext());
                        com.facebook.ads.InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
                            @Override
                            public void onInterstitialDisplayed(Ad ad) {

                            }

                            @Override
                            public void onInterstitialDismissed(Ad ad) {

                            }

                            @Override
                            public void onError(Ad ad, AdError adError) {
                                Log.d("ADerror",adError.getErrorMessage());
                            }

                            @Override
                            public void onAdLoaded(Ad ad) {
                                facebookInterstitialAd.show();
                            }

                            @Override
                            public void onAdClicked(Ad ad) {

                            }

                            @Override
                            public void onLoggingImpression(Ad ad) {

                            }
                        };
                        facebookInterstitialAd = new com.facebook.ads.InterstitialAd(getContext(),MainActivity.indratech_fast_27640849_fb_interstitial_id);
                        facebookInterstitialAd.loadAd(facebookInterstitialAd.buildLoadAdConfig().withAdListener(interstitialAdListener).build());

                    }


                }
            });
        }
    }
}
