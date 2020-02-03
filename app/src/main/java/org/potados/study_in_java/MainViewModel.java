package org.potados.study_in_java;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class MainViewModel {

    private String msg = "Hello, World!";

    private MutableLiveData<String> _message = new MutableLiveData<String>(msg);
    public LiveData<String> message = _message;

    public void onButtonClick() {
        _message.postValue(_message.getValue() + msg);
    }
}
