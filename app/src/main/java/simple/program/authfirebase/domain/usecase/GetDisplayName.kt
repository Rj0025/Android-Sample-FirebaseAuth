package simple.program.authfirebase.domain.usecase

import simple.program.authfirebase.domain.repository.AuthRepository

class GetDisplayName(
    private val repository: AuthRepository,
) {
    operator fun invoke() = repository.getDisplayName()
}