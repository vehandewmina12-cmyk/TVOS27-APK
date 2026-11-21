package com.example.tvlauncher

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var list: RecyclerView
    private lateinit var label: TextView
    private lateinit var adapter: AppAdapter
    private var apps: List<AppItem> = emptyList()

    private val pkgReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) = loadApps()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        label = findViewById(R.id.appLabel)
        list = findViewById(R.id.appList)
        list.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        adapter = AppAdapter(onLaunch = { launchApp(it) }, onFocused = { label.text = it.label })
        list.adapter = adapter

        val antenna = findViewById<TextView>(R.id.btnAntenna)
        antenna.setOnClickListener {
            if (!AntennaLauncher.launch(this)) {
                Toast.makeText(this, "Antenna app not found - choose it", Toast.LENGTH_LONG).show()
                pickAntennaApp()
            }
        }
        antenna.setOnLongClickListener { pickAntennaApp(); true }
        antenna.setOnHoverListener { v, e ->
            if (e.action == MotionEvent.ACTION_HOVER_ENTER) v.requestFocus(); false
        }

        val settings = findViewById<TextView>(R.id.btnSettings)
        settings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        settings.setOnHoverListener { v, e ->
            if (e.action == MotionEvent.ACTION_HOVER_ENTER) v.requestFocus(); false
        }

        val f = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        registerReceiver(pkgReceiver, f)
        loadApps()
    }

    override fun onDestroy() {
        unregisterReceiver(pkgReceiver)
        super.onDestroy()
    }

    private fun loadApps() {
        val pm = packageManager
        val result = LinkedHashMap<String, AppItem>()
        for (cat in listOf(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER)) {
            val q = Intent(Intent.ACTION_MAIN).addCategory(cat)
            for (ri in pm.queryIntentActivities(q, 0)) {
                val ai = ri.activityInfo
                if (ai.packageName == packageName || result.containsKey(ai.packageName)) continue
                val banner = ai.loadBanner(pm)
                result[ai.packageName] = AppItem(
                    label = ri.loadLabel(pm).toString(),
                    component = ComponentName(ai.packageName, ai.name),
                    image = banner ?: ri.loadIcon(pm),
                    isBanner = banner != null
                )
            }
        }
        apps = result.values.sortedBy { it.label.lowercase() }
        adapter.submit(apps)
        list.post { list.findViewHolderForAdapterPosition(0)?.itemView?.requestFocus() }
    }

    private fun launchApp(item: AppItem) {
        try {
            startActivity(Intent(Intent.ACTION_MAIN).setComponent(item.component)
                .addCategory(Intent.CATEGORY_LAUNCHER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot open ${item.label}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickAntennaApp() {
        val names = apps.map { it.label }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Choose your TV / Antenna app")
            .setItems(names) { _, which ->
                AntennaLauncher.savePackage(this, apps[which].component.packageName)
                Toast.makeText(this, "Saved: ${apps[which].label}", Toast.LENGTH_SHORT).show()
            }.show()
    }

    /** Launcher "back": return to the first icon. Safe to call twice. */
    private fun goHome() {
        list.scrollToPosition(0)
        list.post { list.findViewHolderForAdapterPosition(0)?.itemView?.requestFocus() }
    }

    // RIGHT CLICK = BACK (left click is handled by the normal click listeners)
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.isFromSource(InputDevice.SOURCE_MOUSE) &&
            ev.actionMasked == MotionEvent.ACTION_DOWN &&
            (ev.buttonState and MotionEvent.BUTTON_SECONDARY) != 0
        ) {
            goHome()
            return true
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) { goHome(); return true }
        return super.onKeyDown(keyCode, event)
    }

    @Deprecated("Launcher never finishes")
    override fun onBackPressed() { goHome() }
}
