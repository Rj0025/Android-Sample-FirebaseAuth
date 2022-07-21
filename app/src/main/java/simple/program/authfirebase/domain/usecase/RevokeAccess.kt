package simple.program.authfirebase.domain.usecase

import simple.program.authfirebase.domain.repository.AuthRepository

class RevokeAccess(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke() = repository.revokeAccess()
}