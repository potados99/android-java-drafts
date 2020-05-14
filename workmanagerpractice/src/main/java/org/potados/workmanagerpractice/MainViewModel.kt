package org.potados.workmanagerpractice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _progress = MutableLiveData<String>()
    val progress: LiveData<String> = _progress

    fun setProgress(progress: Int) {
        _progress.postValue("Currently at $progress%")
    }
}