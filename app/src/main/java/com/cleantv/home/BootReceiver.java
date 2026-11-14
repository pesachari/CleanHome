package com.cleantv.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Opens Clean Home when the Chromecast turns on (works once the Home button service is on). */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            try {
                HomeButtonService.openHome(context);
            } catch (RuntimeException ignored) {
                // Android may block this if the service is off; the Home button still works.
            }
        }
    }
}
