package simple.program.authfirebase.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.UnsupportedApiCallException
import com.google.firebase.auth.AuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import simple.program.authfirebase.domain.model.Response
import simple.program.authfirebase.domain.usecase.AuthUseCase
import simple.program.authfirebase.util.Constant.SIGN_IN_ERROR_MESSAGE
import simple.program.authfirebase.util.isValidEmail
import simple.program.authfirebase.util.isValidPassword
import java.lang.Exception
import javax.inject.Inject


// Depend on the user preference if shared flow or channel will be use for the events
// Shared Flow will is how flow so it might lose some data because data is being emitted even there is no observer

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val useCases: AuthUseCase,
) : ViewModel() {

    private val _error = Channel<Exception>()
    val error = _error.receiveAsFlow()

    val isUserAuthenticated get() = useCases.isUserAuthenticated()

    val displayName get() = useCases.getDisplayName()

    val photoUrl get() = useCases.getPhotoUrl()

    private val _signInAnonymousEvent = MutableSharedFlow<Boolean>()
    val signInAnonymousEvent = _signInAnonymousEvent.asSharedFlow()

    private val _signInEvent = Channel<Boolean>()
    val signInEvent = _signInEvent.receiveAsFlow()

    private val _signInGoogleNormally = Channel<Boolean>()
    val signInGoogleNormally = _signInGoogleNormally.receiveAsFlow()

    private val _isUserSignedOutEvent = MutableSharedFlow<Boolean>()
    val isUserSignedOutEvent = _isUserSignedOutEvent.asSharedFlow()

    private val _oneTapSignInOrSignUpEvent = MutableSharedFlow<BeginSignInResult>()
    val oneTapSignInOrSignUpEvent = _oneTapSignInOrSignUpEvent.asSharedFlow()

    private val _revokeAccessEvent = MutableSharedFlow<Boolean>()
    val revokeAccessEvent = _revokeAccessEvent.asSharedFlow()

    fun getAuthState() = liveData(Dispatchers.IO) {
        useCases.getAuthState().collect { response ->
            emit(response)
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            useCases.signInAnonymously().collect { response ->
                when (response) {
                    is Response.Error -> Unit
                    Response.Loading -> Unit
                    is Response.Success -> {
                        _signInAnonymousEvent.emit(response.data)
                    }
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            useCases.signOut().collect { response ->
                when (response) {
                    is Response.Error -> Unit
                    Response.Loading -> Unit
                    is Response.Success -> {
                        _isUserSignedOutEvent.emit(response.data)
                    }
                }
            }
        }
    }

    fun oneTapSignIn() = viewModelScope.launch {
        useCases.oneTapSignInWithGoogle().collect { response ->
            when (response) {
                Response.Loading -> Unit
                is Response.Error -> {
                    when (response.e) {
                        is UnsupportedApiCallException -> {
                            _signInGoogleNormally.send(true)
                        }
                        is ApiException -> {
                            // Have not check what error code for this
                            if (response.e.message == SIGN_IN_ERROR_MESSAGE) {
                                oneTapSignUp()
                            }

                        }
                    }
                }
                is Response.Success -> {
                    _oneTapSignInOrSignUpEvent.emit(response.data)
                }
            }
        }
    }

    private fun oneTapSignUp() = viewModelScope.launch {
        useCases.oneTopUpSignUpWithGoogle().collect { response ->
            when (response) {
                is Response.Error -> Unit
                Response.Loading -> Unit
                is Response.Success -> {
                    _oneTapSignInOrSignUpEvent.emit(response.data)
                }
            }
        }
    }

    fun signInWithGoogle(googleCredential: AuthCredential) = viewModelScope.launch {
        useCases.firebaseSignInWithGoogle(googleCredential).collect { response ->
            when (response) {
                is Response.Error -> {

                }
                Response.Loading -> Unit
                is Response.Success -> {
                    if (response.data) {
                        createUser()
                    } else {
                        _signInEvent.send(response.data)
                    }
                }
            }
        }
    }

    fun signInWithEmail(email: String, password: String) = viewModelScope.launch {
        if (!email.isValidEmail()) {
            _error.send(Exception("Invalid email address"))
            return@launch
        }

        useCases.signInWithEmail(email, password).collect { response ->
            when (response) {
                is Response.Error -> {
                    _error.send(response.e!!)
                }
                Response.Loading -> Unit
                is Response.Success -> {
                    _signInEvent.send(response.data)
                }
            }
        }
    }

    fun signUpWithEmail(email: String, password: String) = viewModelScope.launch {
        if (!email.isValidEmail()) {
            _error.send(Exception("Invalid email address"))
            return@launch
        }

        if (!password.isValidPassword()) {
            _error.send(Exception("Invalid password"))
            return@launch
        }

        useCases.signUpWithEmail(email, password).collect { response ->
            when (response) {
                is Response.Error -> {
                    _error.send(response.e!!)
                }
                Response.Loading -> Unit
                is Response.Success -> {
                    createUser()
                }
            }
        }
    }

    private fun createUser() = viewModelScope.launch {
        useCases.createUserInFirestore().collect { response ->
            when (response) {
                is Response.Error -> Unit
                Response.Loading -> Unit
                is Response.Success -> {
                    _signInEvent.send(response.data)
                }
            }
        }
    }

    fun revokeAccess() = viewModelScope.launch {
        useCases.revokeAccess().collect { response ->
            when (response) {
                is Response.Error -> Unit
                Response.Loading -> Unit
                is Response.Success -> {
                    _revokeAccessEvent.emit(response.data)
                }
            }
        }
    }
}
