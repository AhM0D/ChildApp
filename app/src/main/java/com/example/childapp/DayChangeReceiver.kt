package com.example.childapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class DayChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, p1: Intent) {
        if (p1.action == Intent.ACTION_DATE_CHANGED) {
            val pref = context.getSharedPreferences("ChildApp", 0)
            val editor = pref.edit()
            editor.putLong("onDuration", 0)
            editor.apply()
        }
    }
}