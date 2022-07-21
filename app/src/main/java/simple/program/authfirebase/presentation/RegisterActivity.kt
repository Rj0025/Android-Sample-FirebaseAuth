package simple.program.authfirebase.presentation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import simple.program.authfirebase.databinding.ActivityRegisterBinding

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setupObserver()
        setupView()

    }

    private fun setupObserver() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signInEvent.collect {
                    goToMainActivity()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect {
                    Toast.makeText(this@RegisterActivity, it.message , Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun goToMainActivity() {
        Toast.makeText(this@RegisterActivity, "Login Success", Toast.LENGTH_SHORT).show()
        val intent = Intent(this@RegisterActivity, Main2Activity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupView() = with(binding) {
        loginTextView.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
        }
        registerButton.setOnClickListener {
            viewModel.signUpWithEmail(
                emailEditText.text.toString(),
                passwordEditText.text.toString()
            )
        }
    }
}