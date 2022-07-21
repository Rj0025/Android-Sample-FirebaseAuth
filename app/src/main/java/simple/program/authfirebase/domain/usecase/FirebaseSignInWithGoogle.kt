package simple.program.authfirebase.domain.usecase

import com.google.firebase.auth.AuthCredential
import simple.program.authfirebase.domain.repository.AuthRepository

class FirebaseSignInWithGoogle(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(googleAuthCredential: AuthCredential) =
        repository.firebaseSignInWithGoogle(googleAuthCredential)
}