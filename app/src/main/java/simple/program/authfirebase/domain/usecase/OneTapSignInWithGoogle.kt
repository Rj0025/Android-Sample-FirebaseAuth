package simple.program.authfirebase.domain.usecase

import simple.program.authfirebase.domain.repository.AuthRepository

class OneTapSignInWithGoogle(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke() = repository.oneTapSignInWithGoogle()
}