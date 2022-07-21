package simple.program.authfirebase.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import simple.program.authfirebase.databinding.ActivityLoginBinding
import javax.inject.Inject


@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val viewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var oneTopClient: SignInClient

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            try {
                val credentials = oneTopClient.getSignInCredentialFromIntent(result.data)
                signInWithGoogle(credentials.googleIdToken)
            } catch (it: ApiException) {
                print(it)
            }
        }
    }

    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val acc = task.getResult(ApiException::class.java)
                signInWithGoogle(acc.idToken)
            } catch (e: ApiException) {
                print(e)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setupView()
        setupObservables()
    }

    private fun setupObservables() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signInAnonymousEvent.collect { response ->
                    if (response) {
                        goToMainActivity()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.oneTapSignInOrSignUpEvent.collect {
                    launchOneTopGoogle(it)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signInEvent.collect {
                    goToMainActivity()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signInGoogleNormally.collect {
                    signInWithGoogleSignClient()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect {
                    Toast.makeText(this@LoginActivity, it.message , Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupView() = with(binding) {
        registerTextView.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
        loginAnonymously.setOnClickListener {
            viewModel.signInAnonymously()
        }

        imgGoogle.setOnClickListener {
            viewModel.oneTapSignIn()
        }

        loginButton.setOnClickListener {
            viewModel.signInWithEmail(
                emailEditText.text.toString(),
                passwordEditText.text.toString()
            )
        }
    }

    private fun goToMainActivity() {
        Toast.makeText(this@LoginActivity, "Login Success", Toast.LENGTH_SHORT).show()
        val intent = Intent(this@LoginActivity, Main2Activity::class.java)
        startActivity(intent)
        finish()
    }

    private fun signInWithGoogleSignClient() {
        val signInIntent: Intent = googleSignInClient.signInIntent
        googleLauncher.launch(signInIntent)
    }

    private fun launchOneTopGoogle(signInResult: BeginSignInResult) {
        val intent = IntentSenderRequest.Builder(signInResult.pendingIntent.intentSender).build()
        launcher.launch(intent)
    }

    private fun signInWithGoogle(idToken: String?) {
        val googleAuthCredential = GoogleAuthProvider.getCredential(idToken, null)
        viewModel.signInWithGoogle(googleAuthCredential)
    }

}