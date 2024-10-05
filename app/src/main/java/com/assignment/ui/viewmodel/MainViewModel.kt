package com.assignment.ui.viewmodel

import androidx.lifecycle.*
import com.assignment.data.model.UploadResponse
import com.assignment.util.NetworkHelper
import com.assignment.util.Resource
import com.assignment.data.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepository: MainRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {

    private val _images = MutableLiveData<Resource<UploadResponse>>()
    val images: LiveData<Resource<UploadResponse>>
        get() = _images

    init {

    }

    fun uploadImage(filePath: String,apiKey:String) {

        viewModelScope.launch {
            val file = File(filePath)
            val requestFile = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull()))

            _images.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                mainRepository.uploadImage(requestFile,apiKey).let {
                    if (it.isSuccessful) {
                        _images.postValue(Resource.success(it.body()))
                    } else _images.postValue(Resource.error(it.errorBody().toString(), null))
                }
            } else _images.postValue(Resource.error("No internet connection", null))
        }
    }


}