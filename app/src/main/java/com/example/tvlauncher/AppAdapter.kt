package com.example.tvlauncher

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(
    private val onLaunch: (AppItem) -> Unit,
    private val onFocused: (AppItem) -> Unit
) : RecyclerView.Adapter<AppAdapter.VH>() {

    private var items: List<AppItem> = emptyList()

    fun submit(list: List<AppItem>) {
        items = list
        notifyDataSetChanged()
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, position: Int) {
        val item = items[position]
        h.icon.setImageDrawable(item.image)
        if (item.isBanner) {
            h.icon.scaleType = ImageView.ScaleType.CENTER_CROP
            h.icon.setPadding(0, 0, 0, 0)
        } else {
            h.icon.scaleType = ImageView.ScaleType.FIT_CENTER
            val p = (30 * h.itemView.resources.displayMetrics.density).toInt()
            h.icon.setPadding(p, p, p, p)
        }

        h.itemView.setOnClickListener { onLaunch(item) }          // left click / OK button
        h.itemView.setOnFocusChangeListener { v, hasFocus ->
            v.animate().scaleX(if (hasFocus) 1.14f else 1f)
                .scaleY(if (hasFocus) 1.14f else 1f)
                .translationZ(if (hasFocus) 24f else 0f)
                .setDuration(160).start()
            if (hasFocus) onFocused(item)
        }
        // mouse hover moves focus, like pointing at an icon on tvOS
        h.itemView.setOnHoverListener { v, e ->
            if (e.action == MotionEvent.ACTION_HOVER_ENTER && !v.hasFocus()) v.requestFocus()
            false
        }
    }
}
