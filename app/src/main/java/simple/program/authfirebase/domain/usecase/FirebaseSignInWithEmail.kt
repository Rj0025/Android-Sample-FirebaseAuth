package simple.program.authfirebase.domain.usecase

import android.util.Patterns
import com.google.firebase.auth.AuthCredential
import simple.program.authfirebase.domain.repository.AuthRepository

class FirebaseSignInWithEmail(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String) =
        repository.signInWithEmail(email, password)
}