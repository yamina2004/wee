package com.inapp.vpn.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.SkuDetails;
import com.google.common.collect.ImmutableList;
import com.inapp.vpn.R;
import com.inapp.vpn.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UnlockAllActivity extends AppCompatActivity {

    private Map<String, SkuDetails> skusWithSkuDetails = new HashMap<>();
    private final List<String> allSubs = new ArrayList<>(Arrays.asList(
            Config.all_month_id,
            Config.all_threemonths_id,
            Config.all_sixmonths_id,
            Config.all_yearly_id));

    private MutableLiveData<Integer> all_check = new MutableLiveData<>();
    @BindView(R.id.one_month)
    RadioButton oneMonth;
    @BindView(R.id.three_month)
    RadioButton threeMonth;
    @BindView(R.id.six_month)
    RadioButton sixMonth;
    @BindView(R.id.one_year)
    RadioButton oneYear;

    private BillingClient billingClient;

    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                if (purchases != null) {

                    if (purchases.get(0) != null) {
                        Log.v("CHECKBILLING", purchases.get(0).toString());
                        handlePurchase(purchases.get(0).getPurchaseToken());
                    }
                } else {
                    Toast.makeText(UnlockAllActivity.this, "Something went wrong. Please try again", Toast.LENGTH_LONG).show();
                }
            }
        }
    };


    public void MainActivity (View view){
        Intent intent = new Intent (this, MainActivity.class);
        startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_unlock_all_indratech);

        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
        billingSetup();

        ButterKnife.bind(this);
        all_check.setValue(-1);
        all_check.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                switch (integer) {
                    case 0:
                        threeMonth.setChecked(false);
                        sixMonth.setChecked(false);
                        oneYear.setChecked(false);
                        break;
                    case 1:
                        oneMonth.setChecked(false);
                        sixMonth.setChecked(false);
                        oneYear.setChecked(false);
                        break;
                    case 2:
                        threeMonth.setChecked(false);
                        oneMonth.setChecked(false);
                        oneYear.setChecked(false);
                        break;
                    case 3:
                        threeMonth.setChecked(false);
                        sixMonth.setChecked(false);
                        oneMonth.setChecked(false);
                        break;

                }
            }
        });

        oneMonth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) all_check.postValue(0);
            }
        });
        threeMonth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) all_check.postValue(1);
            }
        });
        sixMonth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) all_check.postValue(2);
            }
        });
        oneYear.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) all_check.postValue(3);
            }
        });
    }
    private void billingSetup() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    Log.v("CHECKBILLING", "ready");
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                Log.v("CHECKBILLING", "disconnected");
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                finish();
                Toast.makeText(UnlockAllActivity.this, "Service temporarily unavailable. Please check your Google Play account or try again after some time.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void queryProduct(String productId) {
        Log.v("CHECKBILLING", "clicked");
        QueryProductDetailsParams queryProductDetailsParams =
                QueryProductDetailsParams.newBuilder()
                        .setProductList(
                                ImmutableList.of(
                                        QueryProductDetailsParams.Product.newBuilder()
                                                .setProductId(productId)
                                                .setProductType(BillingClient.ProductType.SUBS)
                                                .build()))
                        .build();

        billingClient.queryProductDetailsAsync(
                queryProductDetailsParams,
                new ProductDetailsResponseListener() {
                    public void onProductDetailsResponse(BillingResult billingResult,
                                                         List<ProductDetails> productDetailsList) {

                        Log.v("CHECKBILLING", billingResult.toString());
                        Log.e("CHECKBILLING", productId + ": " + productDetailsList.toString());
                        if (productDetailsList.size() > 0) {

                            makePurchase(productDetailsList.get(0));

                        } else {
                            Log.e("CHECKBILLING", "onProductDetailsResponse: No products");

                            finish();
                            Toast.makeText(UnlockAllActivity.this, "Sorry, this subscription is currently unavailable", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }

    private void makePurchase(ProductDetails productDetails) {
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                        ImmutableList.of(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(productDetails)
                                        .setOfferToken("")
                                        .build()
                        )
                )
                .build();

        Log.v("CHECKBILLING", "makePurchase");
        // Launch the billing flow
        BillingResult billingResult = billingClient.launchBillingFlow(this, billingFlowParams);
    }

    private void handlePurchase(String purchaseToken) {

        AcknowledgePurchaseParams acknowledgePurchaseParams =
                AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchaseToken)
                        .build();

        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        Log.v("CHECKBILLING", "acknowledged");
                        Config.vip_subscription = true;
                        Config.all_subscription = true;
                    }
                };

                thread.start();
            }
        };

        billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
    }

    @OnClick(R.id.all_pur)
    void unlockAll() {
        if (all_check.getValue() != null) {

            switch (all_check.getValue()) {
                case 0:
                    queryProduct(Config.all_month_id);
                    break;
                case 1:
                    queryProduct(Config.all_threemonths_id);
                    break;
                case 2:
                    queryProduct(Config.all_sixmonths_id);
                    break;
                case 3:
                    queryProduct(Config.all_yearly_id);
                    break;
            }
        }
    }
}
