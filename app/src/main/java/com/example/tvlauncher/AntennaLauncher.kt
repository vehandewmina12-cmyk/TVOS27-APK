package com.example.tvlauncher

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.tv.TvContract
import android.media.tv.TvInputInfo
import android.media.tv.TvInputManager

/** Opens the box's built-in TV / antenna (tuner) input. */
object AntennaLauncher {
    private const val PREFS = "launcher"
    private const val KEY_PKG = "antenna_pkg"

    // Common TV-app packages on Android 9 boxes. Device specific; the picker (long-press) covers the rest.
    private val KNOWN_PACKAGES = listOf(
        "com.droidlogic.tvsource",
        "com.mediatek.wwtv.tvcenter",
        "com.google.android.tv",
        "com.android.tv"
    )

    fun savePackage(ctx: Context, pkg: String) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_PKG, pkg).apply()
    }

    fun launch(ctx: Context): Boolean {
        // 1. app chosen by the user (long-press the Antenna button)
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_PKG, null)?.let { pkg ->
            ctx.packageManager.getLaunchIntentForPackage(pkg)?.let { if (start(ctx, it)) return true }
        }
        // 2. system tuner input -> Live Channels / TV app
        try {
            val tm = ctx.getSystemService(Context.TV_INPUT_SERVICE) as? TvInputManager
            val tuner = tm?.tvInputList?.firstOrNull { it.type == TvInputInfo.TYPE_TUNER }
            if (tuner != null) {
                val i = Intent(Intent.ACTION_VIEW, TvContract.Channels.CONTENT_URI)
                if (start(ctx, i)) return true
            }
        } catch (_: Exception) { }
        // 3. well-known vendor TV apps
        for (pkg in KNOWN_PACKAGES) {
            ctx.packageManager.getLaunchIntentForPackage(pkg)?.let { if (start(ctx, it)) return true }
        }
        return false
    }

    private fun start(ctx: Context, i: Intent): Boolean = try {
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(i)
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }
}
