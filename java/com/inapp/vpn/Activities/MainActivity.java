package com.inapp.vpn.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.net.VpnService;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryPurchasesParams;
import com.android.billingclient.api.SkuDetails;
import com.bumptech.glide.Glide;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;
import com.inapp.vpn.Config;
import com.inapp.vpn.R;
import com.inapp.vpn.Utils.ActiveServer;
import com.inapp.vpn.model.Countries;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import top.oneconnectapi.app.OpenVpnApi;
import top.oneconnectapi.app.core.OpenVPNThread;

public class MainActivity extends ContentsActivity {

    public static String copyright_indratech_official_dont_change_the_value;
    public static Countries selectedCountry = null;
    private Locale locale;
    private boolean isFirst = true;

    public static String type = "";
    public static String indratech_fast_27640849_admob_id = "";
    public static String indratech_fast_27640849_ad_banner_id = "";
    public static String admob_interstitial_id = "";
    public static String indratech_fast_27640849_aad_native_id = "";
    public static String indratech_fast_27640849_fb_native_id = "";
    public static String indratech_fast_27640849_fb_interstitial_id = "";
    public static boolean indratech_fast_27640849_all_ads_on_off = false;

    private OpenVPNThread vpnThread = new OpenVPNThread();

    private BillingClient billingClient;
    private Map<String, SkuDetails> skusWithSkuDetails = new HashMap<>();
    private final List<String> allSubs = new ArrayList<>(Arrays.asList(
            Config.all_month_id,
            Config.all_threemonths_id,
            Config.all_sixmonths_id,
            Config.all_yearly_id));

    private final PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {

        }
    };

    private void billingSetup() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    Log.v("CHECKBILLING", "ready");
                    checkIfSubscribed();
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                Log.v("CHECKBILLING", "disconnected");
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.

            }
        });
    }

    private void checkIfSubscribed() {

        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                new PurchasesResponseListener() {
                    public void onQueryPurchasesResponse(BillingResult billingResult, List purchases) {
                        // check billingResult
                        // process returned purchase list, e.g. display the plans user owns

                        int isAcknowledged = 0;
                        Log.v("CHECKBILLING", "purchases: " + purchases.size());
                        if(purchases.size() > 0) {
                            for (int i = 0; i < purchases.size(); i++) {
                                Log.v("CHECKBILLING", "" + purchases.get(i).toString());
                                isAcknowledged++;
                            }
                        }

                        Config.vip_subscription = isAcknowledged > 0;
                        Config.all_subscription = isAcknowledged > 0;

                        if (!Config.vip_subscription) {
                            updateSubscription();
                        }
                    }
                }
        );
    }

    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("connectionState"));

        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });

        billingSetup();

        Intent intent = getIntent();

        if(getIntent().getExtras() != null) {
            selectedCountry = getIntent().getExtras().getParcelable("c");
            updateUI("LOAD");

            if (!Utility.isOnline(getApplicationContext())) {
                showMessage("No Internet Connection", "error");
            } else {
                showInterstitialAndConnect();
            }
        } else {
            if(selectedCountry != null) {
                updateUI("CONNECTED");

                Glide.with(this)
                        .load(selectedCountry.getFlagUrl())
                        .into(imgFlag);
                flagName.setText(selectedCountry.getCountry());
            }
        }

        if (intent.getStringExtra("type") != null) {
            type = intent.getStringExtra("type");
            indratech_fast_27640849_ad_banner_id = intent.getStringExtra("indratech_fast_27640849_ad_banner");
            admob_interstitial_id = intent.getStringExtra("admob_interstitial");
            indratech_fast_27640849_fb_native_id = intent.getStringExtra("indratech_fast_27640849_fb_native");
            indratech_fast_27640849_fb_interstitial_id = intent.getStringExtra("indratech_fast_27640849_fb_interstitial");
        }

        if(TextUtils.isEmpty(type)) {
            type = "";
            Log.v("AD_TYPE", " null");
        }

        if (type.equals("ad")) {
            RequestConfiguration.Builder requestBuilder = new RequestConfiguration.Builder();
            MobileAds.setRequestConfiguration(requestBuilder.build());
        } else {
            AdSettings.setIntegrationErrorMode(AdSettings.IntegrationErrorMode.INTEGRATION_ERROR_CALLBACK_MODE);

            //Initialize facebook ads
            AudienceNetworkAds.initialize(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(billingClient.isReady())
            checkIfSubscribed();
        else
            billingSetup();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        inAppUpdate();
    }

    @Override
    protected void disconnectFromVpn() {
        try {
            vpnThread.stop();
            updateUI("DISCONNECTED");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void checkRemainingTraffic() {

    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_main_indratech;
    }

    private void inAppUpdate() {
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(MainActivity.this);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo result) {
                if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        // For a flexible update, use AppUpdateType.FLEXIBLE
                        && result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(result, AppUpdateType.IMMEDIATE, MainActivity.this, 11);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                    // Request the update.
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 11) {
            Toast.makeText(this, "Start Downloand", Toast.LENGTH_SHORT).show();
            if (resultCode != RESULT_OK) {
                Log.d("Update", "Update failed" + resultCode);
            }
        }

        if (resultCode == RESULT_OK) {
            startVpn();
        } else {
            showMessage("Permission Denied", "error");
        }
    }

    public void prepareVpn() {

        Glide.with(this)
                .load(selectedCountry.getFlagUrl())
                .into(imgFlag);
        flagName.setText(selectedCountry.getCountry());

        if (Utility.isOnline(getApplicationContext())) {

            if(selectedCountry != null) {
                Intent intent = VpnService.prepare(this);
                Log.v("CHECKSTATE", "start");

                if (intent != null) {
                    startActivityForResult(intent, 1);
                } else
                    startVpn(); //have already permission
            } else {
                showMessage("Please select a server first", "");
            }

        } else {
            showMessage("No Internet Connection", "error");
        }
    }

    protected void startVpn() {
        try {
            ActiveServer.saveServer(MainActivity.selectedCountry, MainActivity.this);
            OpenVpnApi.startVpn(this, selectedCountry.getOvpn(), selectedCountry.getCountry(), selectedCountry.getOvpnUserName(), selectedCountry.getOvpnUserPassword());

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                updateUI(intent.getStringExtra("state"));
                Log.v("CHECKSTATE", intent.getStringExtra("state"));

                if (isFirst) {
                    if (ActiveServer.getSavedServer(MainActivity.this).getCountry() != null) {
                        selectedCountry = ActiveServer.getSavedServer(MainActivity.this);

                        Glide.with(MainActivity.this)
                                .load(selectedCountry.getFlagUrl())
                                .into(imgFlag);
                        flagName.setText(selectedCountry.getCountry());
                    }

                    isFirst = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                String duration = intent.getStringExtra("duration");
                String lastPacketReceive = intent.getStringExtra("lastPacketReceive");
                String byteIn = intent.getStringExtra("byteIn");
                String byteOut = intent.getStringExtra("byteOut");

                if (duration == null) duration = "00:00:00";
                if (lastPacketReceive == null) lastPacketReceive = "0";
                if (byteIn == null) byteIn = " ";
                if (byteOut == null) byteOut = " ";

                updateConnectionStatus(duration, lastPacketReceive, byteIn, byteOut);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    public void checkSelectedCountry() {
        if (selectedCountry == null) {
            updateUI("DISCONNECT");
            showMessage("Please select a server first", "");
        } else {
            showInterstitialAndConnect();
            updateUI("LOAD");
        }
    }
}
