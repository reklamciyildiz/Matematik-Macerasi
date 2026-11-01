package com.yigit033.matematikmacarasi.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/** Repair legacy MainActivity disable state after installing an app update. */
class IconUpgradeReceiver : BroadcastReceiver() {
    override fun onReceive(context:Context,intent:Intent) {
        if(intent.action==Intent.ACTION_MY_PACKAGE_REPLACED) {
            val ok=IconLauncherManager.reconcile(context.applicationContext)
            if(!ok) Log.w("IconUpgradeReceiver","Launcher repair failed")
        }
    }
}
