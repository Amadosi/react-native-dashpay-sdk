
package com.dashpay.rndashpay;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.CountDownTimer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

import android.util.Log;

public class RNDashpaySdkModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    private final ReactApplicationContext reactContext;
    private static final String PAYMENT_URI = "com.ar.pos";
    public static int tsn = 1;
    public static int lastSentTsn = 0;
    private static final int PAYMENT_REQUEST = 11234;

    private String NO_APP_LIST = "APP_NOT_FOUND";
    private String APP_NOT_FOUND = "APP_NOT_FOUND";
    private String TRN_DECLINED = "TRN_DECLINED";
    private String TRN_FAILED = "TRN_FAILED";
    private String TRN_CANCELLED = "TRN_CANCELLED";

    public static String PACKAGE_NAME;

    private Promise mPaymentPromise;

    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {

        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
            if (requestCode == PAYMENT_REQUEST) {

                if (mPaymentPromise != null) {

                    if (resultCode == Activity.RESULT_OK) {

                        String tid = intent.getStringExtra("TRANSACTION_ID");

                        if (tid != null && Integer.parseInt(tid) == lastSentTsn) {

                            String result = intent.getStringExtra("RESULT");
                            String displayTest = intent.getStringExtra("DISPLAY_TEXT");

                            if (result.equals("APPROVED")) {
                                WritableMap map = Arguments.createMap();

                                map.putString("response_code", intent.getStringExtra("RESPONSE_CODE"));
                                map.putString("auth_code", intent.getStringExtra("AUTH_CODE"));
                                map.putString("display_message", intent.getStringExtra("DISPLAY_TEXT"));

                                mPaymentPromise.resolve(map);
                                return;

                            } else if (result.equals("DECLINED")) {
                                rejectPromise(TRN_DECLINED, "Transaction declined");
                            } else {
                                rejectPromise(TRN_FAILED, "Transaction failed");
                            }

                            mPaymentPromise = null;
                        }

                    }

                    mPaymentPromise = null;
                }
            }
        }
    };

    public RNDashpaySdkModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        PACKAGE_NAME = this.reactContext.getPackageName();
        this.reactContext.addLifecycleEventListener(this);
        this.reactContext.addActivityEventListener(mActivityEventListener);
    }

    @Override
    public String getName() {
        return "RNDashpaySdk";
    }

    @ReactMethod
    public void pay(String reference, String amount, final Promise promise) {

        Activity currentActivity = getCurrentActivity();
        mPaymentPromise = promise;

        boolean found = false;
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");

        // gets the list of intents that can be loaded.
        List<ResolveInfo> resInfo = reactContext.getPackageManager().queryIntentActivities(share, 0);
        if (!resInfo.isEmpty()) {
            for (ResolveInfo info : resInfo) {
                if (info.activityInfo.packageName.toLowerCase().contains(PAYMENT_URI) ||
                        info.activityInfo.name.toLowerCase().contains(PAYMENT_URI)) {
                    share.putExtra(Intent.EXTRA_ORIGINATING_URI, PACKAGE_NAME);
                    share.putExtra("TRANSACTION_TYPE", "PURCHASE");
                    //share.putExtra("TRANSACTION_TYPE","REVERSE LAST");
                    share.putExtra("AMOUNT", amount); // 15.00
                    share.putExtra("ADDITIONAL_AMOUNT", "0.00");
                    share.putExtra("OPERATOR_ID", "1");
                    share.putExtra("REFERENCE_NUMBER", reference);
                    share.putExtra("TRANSACTION_ID", String.valueOf(tsn));
                    lastSentTsn = tsn;
                    tsn++;
                    share.setPackage(info.activityInfo.packageName);
                    found = true;
                    break;
                }
            }

            if (!found) {
                rejectPromise(APP_NOT_FOUND, "Payment app not found");
                return;
            }

            Intent chooserIntent = Intent.createChooser(share, "Select");
            currentActivity.startActivityForResult(chooserIntent, PAYMENT_REQUEST);
        } else {
            rejectPromise(NO_APP_LIST, "Unable to retrieve application list");
        }
    }

    @Override
    public void onHostResume() {

    }

    @Override
    public void onHostPause() {
    }

    @Override
    public void onHostDestroy() {
        this.mPaymentPromise = null;
    }

    private void rejectPromise(String code, String message) {
        if (this.mPaymentPromise != null) {
            this.mPaymentPromise.reject(code, message);
            this.mPaymentPromise = null;
        }
    }
}
