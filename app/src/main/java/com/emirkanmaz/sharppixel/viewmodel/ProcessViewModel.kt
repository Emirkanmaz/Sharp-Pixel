package com.emirkanmaz.sharppixel.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.emirkanmaz.sharppixel.model.ImageProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProcessViewModel(application: Application): AndroidViewModel(application) {
    private val _selectedBitmap = MutableLiveData<Bitmap?>()
    val selectedBitmap: LiveData<Bitmap?> get() = _selectedBitmap

    private val _processedBitmap = MutableLiveData<Bitmap?>()
    val processedBitmap: LiveData<Bitmap?> get() = _processedBitmap

    val _processingStatus = MutableLiveData<Boolean>()

    private val imageProcessor: ImageProcessor = ImageProcessor()

    fun loadBitmapFromUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= 29) {
                    val source = ImageDecoder.createSource(getApplication<Application>().contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(getApplication<Application>().contentResolver, uri)
                }
                _selectedBitmap.postValue(createDisplayBitmap(bitmap, 1920))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        imageProcessor.closeInterpreter()
    }

    suspend fun processImage() {
        withContext(Dispatchers.Default) {
            _selectedBitmap.value?.let { bitmap ->
                try {
                    withContext(Dispatchers.Main) {
                        _processingStatus.postValue(true)  // You could have an observer on this in the fragment
                    }

                    val originalWidth = bitmap.width
                    val originalHeight = bitmap.height
                    val processedBitmap = imageProcessor.predictImage(getApplication(), bitmap)
                    val scaledBitmap = Bitmap.createScaledBitmap(processedBitmap, originalWidth * 2, originalHeight * 2, true)
                    _processedBitmap.postValue(scaledBitmap)

                    withContext(Dispatchers.Main) {
                        _processingStatus.postValue(false)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _processedBitmap.postValue(null)
                }
            }
        }
    }

    private fun createDisplayBitmap(original: Bitmap, maxSize: Int): Bitmap {
        val width = original.width
        val height = original.height
        val ratio = width.toFloat() / height.toFloat()

        return if (width > height) {
            if (width > maxSize) {
                Bitmap.createScaledBitmap(original, maxSize, (maxSize / ratio).toInt(), true)
            } else {
                original
            }
        } else {
            if (height > maxSize) {
                Bitmap.createScaledBitmap(original, (maxSize * ratio).toInt(), maxSize, true)
            } else {
                original
            }
        }
    }

}