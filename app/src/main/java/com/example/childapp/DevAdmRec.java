package com.example.childapp;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class DevAdmRec extends DeviceAdminReceiver {
    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        DevicePolicyManager deviceManger = (DevicePolicyManager)context.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        deviceManger.lockNow();
        return "Your warning";
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_DEVICE_ADMIN_DISABLE_REQUESTED)) {
            DevicePolicyManager deviceManger = (DevicePolicyManager)context.getSystemService(
                    Context.DEVICE_POLICY_SERVICE);
            deviceManger.lockNow();
        }
        // an Intent broadcast.
    }
}