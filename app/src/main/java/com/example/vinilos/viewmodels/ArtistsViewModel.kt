package com.example.vinilos.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.vinilos.models.Artist
import com.example.vinilos.repositories.ArtistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArtistsViewModel(application: Application): AndroidViewModel(application) {
    private val artistRepository = ArtistRepository(application)
    private val _artist = MutableLiveData<List<Artist>>()
    private var initialArtist: List<Artist> = emptyList()
    val artist: LiveData<List<Artist>>
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
                    val data = artistRepository.refreshData()
                    _artist.postValue(data)
                    initialArtist = data
                    _isLoading.postValue(false)
                } catch(e: Exception) {
                    _isNetworkErrorShown.postValue(true)
                    _isLoading.postValue(false)
                }
            }
        }
    }

    fun filterByArtistName(name: String) {
        var filteredList = mutableListOf<Artist>()
        for(artist in this.initialArtist) {
            if(artist.name.lowercase().startsWith(name.lowercase())) {
                filteredList.add(artist)
            }
        }
        _artist.postValue(filteredList)
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ArtistsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ArtistsViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}