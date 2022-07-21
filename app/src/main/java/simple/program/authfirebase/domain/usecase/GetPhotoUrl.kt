package simple.program.authfirebase.domain.usecase

import simple.program.authfirebase.domain.repository.AuthRepository

class GetPhotoUrl(
    private val repository: AuthRepository,
) {
    operator fun invoke() = repository.getPhotoUrl()
}