package com.emirkanmaz.sharppixel.model

import android.graphics.Bitmap

data class ImageData(
    val originalBitmap: Bitmap,
    val processedBitmap: Bitmap? = null
)