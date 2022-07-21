package simple.program.authfirebase.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import simple.program.authfirebase.databinding.ActivityMain2Binding


@AndroidEntryPoint
class Main2Activity : AppCompatActivity() {

    private val viewModel : AuthViewModel by viewModels()

    private lateinit var binding : ActivityMain2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setupObservables()
        setupView()
    }

    private fun setupObservables() {
        viewModel.getAuthState().observe(this){ isLogin ->
            Toast.makeText(this@Main2Activity, "Auth state change: $isLogin", Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isUserSignedOutEvent.collectLatest {
                    signOut()
                }
            }
        }
    }

    private fun setupView() = with(binding){
        signOutButton.setOnClickListener {
            viewModel.signOut()
        }
        nameTextView.text = viewModel.displayName
        Picasso.get().load(viewModel.photoUrl).into(profileImage)
    }


    private fun signOut() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}