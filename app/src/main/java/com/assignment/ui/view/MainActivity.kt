package com.assignment.ui.view

import android.Manifest
import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.assignment.databinding.ActivityMainBinding
import com.assignment.ui.viewmodel.MainViewModel
import com.assignment.util.Status
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private val mainViewModel : MainViewModel by viewModels()

    private lateinit var cameraExecutor: ExecutorService

    private val apiKey = "6d207e02198a847aa98d0a2a901485a5";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObserver()

        cameraExecutor = Executors.newSingleThreadExecutor()

        requestCameraAndStoragePermissions()

        binding.btnStartStream.setOnClickListener { startCamera() }
        binding.btnStopStream.setOnClickListener { cameraExecutor.shutdown() }
        binding.btnUpload.setOnClickListener { mainViewModel.uploadImage("content://media/external/images/media/1000001059",apiKey) }

    }

    private fun requestCameraAndStoragePermissions() {
        multiplePermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        )
    }

    private val multiplePermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val storageGranted = permissions[Manifest.permission.READ_MEDIA_IMAGES] ?: false

        if (cameraGranted && storageGranted) {
//            startCamera()
        } else {
            Toast.makeText(this, "Camera and storage permissions are required.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // Bind the camera provider to the lifecycle
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

            // Start capturing images
            captureImages(imageCapture)
        }, ContextCompat.getMainExecutor(this))
    }


    private fun captureImages(imageCapture: ImageCapture) {
        val scope = CoroutineScope(Dispatchers.Main)

        scope.launch {
            while (true) {
                // Create a ContentValues object for the new image
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "${System.currentTimeMillis()}.jpg") // Unique name
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES) // Save in Pictures directory
                }

                val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                val tempFile = File.createTempFile("image_${System.currentTimeMillis()}", ".jpg")

                imageUri?.let {
                    // Use OutputFileOptions to specify where to save the captured image
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()

                    // Take the picture
                    imageCapture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(this@MainActivity),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                contentResolver.openOutputStream(it)?.use { outputStream ->
                                    tempFile.inputStream().use { inputStream ->
                                        inputStream.copyTo(outputStream) // Copy the image data to the MediaStore
                                    }
                                }
                                // Delete the temporary file
                                tempFile.delete()

                                // Image captured successfully, process it here
                                Log.d("CameraX", "Image saved: $it")
//                                mainViewModel.uploadImage(it.path.toString(),apiKey)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Log.e("CameraX", "Image capture failed: ${exception.message}", exception)
                            }
                        }
                    )
                } ?: run {
                    Log.e("CameraX", "Failed to create image Uri")
                }

                delay(15000) // Adjust the delay as necessary
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


    private fun setupObserver() {
        mainViewModel.images.observe(this, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                   binding.progressBar.visibility = View.GONE

                    it.data?.let { images ->
                        images.image.url
                    }

                }
                Status.LOADING -> {
                   binding. progressBar.visibility = View.VISIBLE
                }

                Status.ERROR -> {
                   binding. progressBar.visibility = View.GONE
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
            }
        })
    }


}