package com.example.tvlauncher

import android.content.ComponentName
import android.graphics.drawable.Drawable

data class AppItem(
    val label: String,
    val component: ComponentName,
    val image: Drawable,
    val isBanner: Boolean
)
