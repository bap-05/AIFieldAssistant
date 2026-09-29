package com.example.aifieldassistant.ui.viewmodel

import android.util.Base64
import java.io.File

fun encodeImageToBase64(imagePath: String): String? {
    return try {
        val bytes = File(imagePath).readBytes()
        Base64.encodeToString(bytes, Base64.NO_WRAP)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}