package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object{
        const val SIGN_IN_RESULT_CODE = 1001
        const val TAG = "AuthenticationActivity"
    }
    lateinit var binding: ActivityAuthenticationBinding
    private val viewModel by viewModels<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        binding  = DataBindingUtil.setContentView(this , R.layout.activity_authentication)
        binding.btnLogin.setOnClickListener{
            login()
        }
        observeAuthenticationState()

        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

    }
    private fun startReminderActivity() {
        val intent = Intent(this , RemindersActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun observeAuthenticationState() {
        viewModel.authenticationState.observe(this, Observer {authenticationState->
            if(authenticationState==LoginViewModel.AuthenticationState.AUTHENTICATED){
                startReminderActivity()
            }
        })
    }

    private fun login() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
            // This is where you can provide more ways for users to register and sign in.
        )
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            SIGN_IN_RESULT_CODE
        )

    }

}
