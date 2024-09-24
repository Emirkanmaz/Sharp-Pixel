package com.emirkanmaz.sharppixel.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class ImageProcessor() {
    private val MODEL_PATH = "Real-ESRGAN-x4plus_with_metadata.tflite"
    private var interpreter: Interpreter? = null

    fun predictImage(context: Context, imageBitmap: Bitmap?): Bitmap {

        if (interpreter == null){
            interpreter = loadModelFile(context)
        }

        val inputImage = preprocessImage(imageBitmap!!)

        // Giriş boyutu: [1, 128, 128, 3]
        val inputBuffer = ByteBuffer.allocateDirect(1 * 128 * 128 * 3 * 4) // 4 byte per float
        inputBuffer.order(ByteOrder.nativeOrder())
        inputBuffer.rewind()

        // Bitmap'i ByteBuffer'a kopyala ve normalize et
        val intValues = IntArray(128 * 128)
        inputImage.getPixels(intValues, 0, 128, 0, 0, 128, 128)
        for (pixelValue in intValues) {
            inputBuffer.putFloat(((pixelValue shr 16 and 0xFF) / 255.0f))
            inputBuffer.putFloat(((pixelValue shr 8 and 0xFF) / 255.0f))
            inputBuffer.putFloat(((pixelValue and 0xFF) / 255.0f))
        }

        // Çıkış boyutu: [3, 512, 512]
        val outputBuffer = ByteBuffer.allocateDirect(3 * 512 * 512 * 4) // 4 byte per float
        outputBuffer.order(ByteOrder.nativeOrder())

        interpreter!!.run(inputBuffer, outputBuffer)

        // Çıkış verilerini Bitmap'e dönüştür
        outputBuffer.rewind()
        val outputBitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
        val outputArray = FloatArray(3 * 512 * 512)
        outputBuffer.asFloatBuffer().get(outputArray)

        for (y in 0 until 512) {
            for (x in 0 until 512) {
                val r = (outputArray[(y * 512 + x)] * 255).toInt().coerceIn(0, 255)
                val g = (outputArray[(512 * 512) + (y * 512 + x)] * 255).toInt().coerceIn(0, 255)
                val b = (outputArray[(2 * 512 * 512) + (y * 512 + x)] * 255).toInt().coerceIn(0, 255)
                outputBitmap.setPixel(x, y, android.graphics.Color.rgb(r, g, b))
            }
        }

        // Görüntüyü aynalayın ve 90 derece sağa döndürün
        val matrix = Matrix()
        matrix.postScale(-1f, 1f, outputBitmap.width / 2f, outputBitmap.height / 2f)
        matrix.postRotate(-90f)

        val finalBitmap = Bitmap.createBitmap(outputBitmap, 0, 0, outputBitmap.width, outputBitmap.height, matrix, true)

        return finalBitmap
//        return outputBitmap
    }

    private fun preprocessImage(image: Bitmap): Bitmap {
        // Config.HARDWARE formatındaki bitmap'i ARGB_8888'e dönüştür
        val convertedBitmap = image.copy(Bitmap.Config.ARGB_8888, true)
        return Bitmap.createScaledBitmap(convertedBitmap, 128, 128, true)
    }

    private fun loadModelFile(context: Context): Interpreter {
        val fileDescriptor = context.assets.openFd(MODEL_PATH)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        val mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        return Interpreter(mappedByteBuffer)
    }

    fun closeInterpreter() {
        interpreter?.close()
        interpreter = null
    }
}