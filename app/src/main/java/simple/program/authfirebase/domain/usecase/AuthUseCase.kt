package simple.program.authfirebase.domain.usecase

data class AuthUseCase(
    val isUserAuthenticated: IsUserAuthenticated,
    val signInAnonymously: SignInAnonymously,
    val signOut: SignOut,
    val getAuthState: GetAuthState,
    val oneTapSignInWithGoogle: OneTapSignInWithGoogle,
    val oneTopUpSignUpWithGoogle: OneTapSignUpWithGoogle,
    val firebaseSignInWithGoogle: FirebaseSignInWithGoogle,
    val createUserInFirestore: CreateUserInFirestore,
    val revokeAccess: RevokeAccess,
    val getDisplayName: GetDisplayName,
    val getPhotoUrl: GetPhotoUrl,
    val signInWithEmail: FirebaseSignInWithEmail,
    val signUpWithEmail: SignUpWithEmail
)