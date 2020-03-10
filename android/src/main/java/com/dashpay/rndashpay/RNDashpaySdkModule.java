
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

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;

import android.util.Log;

public class RNDashpaySdkModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    private final ReactApplicationContext reactContext;
    private static final String PAYMENT_URI = "com.ar.pos";
    public static int tsn = 1;
    public static int lastSentTsn=0;
    private static final int PAYMENT_REQUEST = 1;
    public static String PACKAGE_NAME;

    public RNDashpaySdkModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        PACKAGE_NAME = this.reactContext.getPackageName();
        this.reactContext.addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return "RNDashpaySdk";
    }

    @ReactMethod
    public void pay(String reference, String amount) {

        Log.d("Loaded Intents", PACKAGE_NAME+"");

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
                    share.putExtra("REFERENCE_NUMBER",reference);
                    share.putExtra("TRANSACTION_ID",String.valueOf(tsn));
                    lastSentTsn = tsn;
                    tsn++;
                    share.setPackage(info.activityInfo.packageName);
                    found = true;
                    break;
                }
            }

            if (!found) {
                Log.d("Loaded Intents", "No intent not found");
                return;
            }

            Intent chooserIntent = Intent.createChooser(share, "Select");
            //add new new task flag to prevent the main activity from restarting
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.reactContext.startActivity(chooserIntent);
        } else {
            Log.d("Loaded Intents", "No intents to be loaded at this time");
        }
    }

    @Override
    public void onHostResume() {
        Log.d("Activity resumed","Activity has resumed");
    }

    @Override
    public void onHostPause() {
    }

    @Override
    public void onHostDestroy() {
    }
}
