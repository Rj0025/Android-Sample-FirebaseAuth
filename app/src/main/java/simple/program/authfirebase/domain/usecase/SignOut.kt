package simple.program.authfirebase.domain.usecase

import simple.program.authfirebase.domain.repository.AuthRepository

class SignOut(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() = repository.signOut()
}