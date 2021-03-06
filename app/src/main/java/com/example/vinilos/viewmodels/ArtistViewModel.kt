package com.example.vinilos.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.vinilos.models.Artist
import com.example.vinilos.repositories.ArtistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArtistViewModel(application: Application, artistId: Int): AndroidViewModel(application) {
    private val artistRepository = ArtistRepository(application)
    private val id = artistId
    private val _artist = MutableLiveData<Artist>()
    val artist: LiveData<Artist>
        get() = _artist
    private var _isNetworkErrorShown = MutableLiveData<Boolean>(false)
    val isNetworkErrorShown: LiveData<Boolean>
        get() = _isNetworkErrorShown
    private val _isLoading = MutableLiveData<Boolean>(true)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    init {
        refreshDataFromNetwork()
    }

    fun refreshDataFromNetwork() {
        _isLoading.value = true
        _isNetworkErrorShown.value = false
        viewModelScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.IO){
                try {
                    val data = artistRepository.refreshArtist(id)
                    _artist.postValue(data)
                    _isLoading.postValue(false)
                } catch(e: Exception) {
                    _isNetworkErrorShown.postValue(true)
                    _isLoading.postValue(false)
                }
            }
        }
    }

    class Factory(val app: Application, val artistId: Int) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ArtistViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ArtistViewModel(app, artistId) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}