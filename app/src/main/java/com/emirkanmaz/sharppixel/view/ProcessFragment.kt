package com.emirkanmaz.sharppixel.view

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.emirkanmaz.sharppixel.viewmodel.ProcessViewModel
import com.emirkanmaz.sharppixel.R
import com.emirkanmaz.sharppixel.databinding.FragmentProcessBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class ProcessFragment : Fragment() {

    private var _binding: FragmentProcessBinding? = null
    private val binding get() = _binding!!

    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private val viewModel: ProcessViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProcessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerLaunchers()

        binding.inputText.setOnClickListener {
            if (!binding.progressBar.isVisible){
                selectImage(it)
            }
        }
        binding.inputImageView.setOnClickListener {
            if (!binding.progressBar.isVisible){
                selectImage(it)
            }
        }

        viewModel.selectedBitmap.observe(viewLifecycleOwner) { bitmap ->
            bitmap?.let {
                binding.inputImageView.setImageBitmap(it)
                binding.processButton.isEnabled = true
            }
        }

        viewModel.processedBitmap.observe(viewLifecycleOwner) { bitmap ->
            bitmap?.let {
                binding.outputImageView.setImageBitmap(it)
                binding.saveButton.isEnabled = true
            }
        }

        binding.processButton.setOnClickListener {
            lifecycleScope.launch {
                binding.progressBar.visibility = View.VISIBLE
                binding.processButton.isEnabled = false
                binding.saveButton.isEnabled = false
                viewModel.processImage()
                binding.progressBar.visibility = View.GONE
            }
        }

        binding.saveButton.setOnClickListener {
            viewModel.processedBitmap.value?.let { bitmap ->
                saveBitmapToGallery(bitmap)
            } ?: run {
                Toast.makeText(requireContext(), "No processed image to save", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectImage(view: View) {
        val permission = if (Build.VERSION.SDK_INT >= 33) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (shouldShowRequestPermissionRationale(permission)) {
                Snackbar.make(view, getString(R.string.need_permission), Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.give_permission)) {
                        permissionLauncher.launch(permission)
                    }.show()
            } else {
                permissionLauncher.launch(permission)
            }
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intent)
        }
    }

    private fun registerLaunchers() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    lifecycleScope.launch {
                        viewModel.loadBitmapFromUri(uri)
                    }
                }
            }
        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intent)
            } else {
                Toast.makeText(requireContext(), getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun saveBitmapToGallery(bitmap: Bitmap) {
        val filename = "IMG_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        var uri: Uri? = null
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                uri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    fos = requireContext().contentResolver.openOutputStream(uri)
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                uri?.let { requireContext().contentResolver.update(it, contentValues, null, null) }
            }

            Toast.makeText(requireContext(), "Image saved to gallery", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to save image: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        } finally {
            fos?.close()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}