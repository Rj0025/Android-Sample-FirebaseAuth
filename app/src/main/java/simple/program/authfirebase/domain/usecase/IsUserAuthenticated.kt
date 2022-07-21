package simple.program.authfirebase.domain.usecase

import simple.program.authfirebase.domain.repository.AuthRepository

class IsUserAuthenticated(
    private val repository: AuthRepository,
) {
    operator fun invoke() = repository.isUserAuthenticatedInFirebase()
}