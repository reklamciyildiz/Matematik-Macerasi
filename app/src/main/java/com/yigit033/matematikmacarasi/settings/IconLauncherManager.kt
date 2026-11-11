package com.yigit033.matematikmacarasi.settings

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

/**
 * Android launcher icons are aliases, not independent app entry points.
 * Never disable MainActivity: explicit intents, shortcuts and deep links depend on it.
 */
object IconLauncherManager {
    private const val TAG="IconLauncherManager"
    private const val MAIN=".MainActivity"
    private const val BRAND=".BrandIconActivity"
    private const val DEFAULT=".DefaultIconActivity"
    private const val ROBOT=".RobotIconActivity"
    private const val SPACE=".SpaceIconActivity"

    private fun aliasFor(icon:String):String = when(icon) {
        IconPreferenceManager.ICON_BRAND -> BRAND
        IconPreferenceManager.ICON_DEFAULT -> DEFAULT
        IconPreferenceManager.ICON_ROBOT -> ROBOT
        IconPreferenceManager.ICON_SPACE -> SPACE
        else -> BRAND
    }

    fun apply(context:Context,icon:String):Boolean {
        val pm=context.packageManager
        val names=listOf(BRAND,DEFAULT,ROBOT,SPACE)
        val selected=aliasFor(icon)
        val target=ComponentName(context.packageName,context.packageName+MAIN)
        try {
            // Migrate persisted COMPONENT_DISABLED state from pre-S3.3 releases.
            if(pm.getComponentEnabledSetting(target)!=PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                pm.setComponentEnabledSetting(target,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP)
            }
            // Enable selected alias BEFORE disabling the previously active icon.
            names.forEach {alias->
                if(alias==selected) {
                    val component=ComponentName(context.packageName,context.packageName+alias)
                    if(pm.getComponentEnabledSetting(component)!=
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                        pm.setComponentEnabledSetting(component,
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP)
                    }
                }
            }
            names.forEach {alias->
                if(alias!=selected) {
                    val component=ComponentName(context.packageName,context.packageName+alias)
                    if(pm.getComponentEnabledSetting(component)!=
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                        pm.setComponentEnabledSetting(component,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP)
                    }
                }
            }
            IconPreferenceManager.getInstance(context).saveSelectedIcon(icon)
            return true
        } catch(e:Exception) {
            Log.e(TAG,"Launcher aliases could not be synchronized",e)
            // Restore the new brand icon as a recoverable launcher entry point.
            runCatching {
                pm.setComponentEnabledSetting(target,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP)
                pm.setComponentEnabledSetting(
                    ComponentName(context.packageName,context.packageName+BRAND),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP)
            }.onFailure {Log.e(TAG,"Default launcher recovery failed",it)}
            return false
        }
    }

    /** Migration/repair: honors the user's last saved icon. */
    fun reconcile(context:Context):Boolean =
        apply(context,IconPreferenceManager.getInstance(context).getSelectedIcon())
}
