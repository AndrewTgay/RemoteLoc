package com.udacity.project4.authentication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class LoginViewModel: ViewModel() {

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }

    val authenticationState = FirebaseUserLiveData().map {
        Log.i("Goooo", "in viewModel ${it?.displayName.toString()}")
        if(it!=null){
            AuthenticationState.AUTHENTICATED
        }
        else
            AuthenticationState.UNAUTHENTICATED
    }
}