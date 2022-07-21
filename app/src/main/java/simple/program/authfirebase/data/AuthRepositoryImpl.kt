package simple.program.authfirebase.data

import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue.serverTimestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import simple.program.authfirebase.domain.model.Response
import simple.program.authfirebase.domain.model.Response.Error
import simple.program.authfirebase.domain.model.Response.Loading
import simple.program.authfirebase.domain.model.Response.Success
import simple.program.authfirebase.domain.repository.AuthRepository
import simple.program.authfirebase.util.Constant.CREATED_AT
import simple.program.authfirebase.util.Constant.DISPLAY_NAME
import simple.program.authfirebase.util.Constant.EMAIL
import simple.program.authfirebase.util.Constant.ERROR_MESSAGE
import simple.program.authfirebase.util.Constant.PHOTO_URL
import simple.program.authfirebase.util.Constant.SIGN_IN_REQUEST
import simple.program.authfirebase.util.Constant.SIGN_UP_REQUEST
import simple.program.authfirebase.util.Constant.USERS_REF
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton


@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private var oneTapClient: SignInClient,
    @Named(SIGN_IN_REQUEST)
    private var signInRequest: BeginSignInRequest,
    @Named(SIGN_UP_REQUEST)
    private var signUpRequest: BeginSignInRequest,
    private var signInClient: GoogleSignInClient,
    private val db: FirebaseFirestore,
) : AuthRepository {
    override fun isUserAuthenticatedInFirebase(): Boolean = auth.currentUser != null

    override suspend fun firebaseSignInAnonymously() = flow {
        try {
            emit(Loading)
            auth.signInAnonymously().await()
            emit(Success(true))
        } catch (e: Exception) {
            emit(Error(e))
        }
    }

    override suspend fun signOut(): Flow<Response<Boolean>> = flow {
        try {
            emit(Loading)
            auth.currentUser?.apply {
                if (isAnonymous) {
                    delete().await()
                    emit(Success(true))
                } else {
                    emit(Loading)
                    auth.signOut()
                    oneTapClient.signOut().await()
                    emit(Success(true))
                }
            }
        } catch (e: Exception) {
            emit(Error(e))
        }
    }

    override fun getFirebaseAuthState() = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser != null)
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun signInWithEmail(email: String, password: String) = flow {
        try {
            emit(Loading)
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val isNewUser = authResult.additionalUserInfo?.isNewUser ?: true
            emit(Success(isNewUser))
        } catch (e: Exception) {
            emit(Error(e))
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String) = flow {
        try {
            emit(Loading)
            auth.createUserWithEmailAndPassword(email,password).await()
            emit(Success(true))
        }  catch (e: Exception) {
            emit(Error(e))
        }
    }

    override suspend fun oneTapSignInWithGoogle() = flow {
        try {
            emit(Loading)
            val result = oneTapClient.beginSignIn(signInRequest).await()
            emit(Success(result))
        } catch (e: Exception) {
            emit(Error(e))
        }
    }

    override suspend fun oneTapSignUpWithGoogle() = flow {
        try {
            emit(Loading)
            val result = oneTapClient.beginSignIn(signUpRequest).await()
            emit(Success(result))
        } catch (e: Exception) {
            emit(Error(e))
        }
    }

    override suspend fun firebaseSignInWithGoogle(googleCredential: AuthCredential) = flow {
        try {
            emit(Loading)
            val authResult = auth.signInWithCredential(googleCredential).await()
            val isNewUser = authResult.additionalUserInfo?.isNewUser ?: true
            emit(Success(isNewUser))
        } catch (e: Exception) {
            emit(Error(e))
        }
    }

    override suspend fun createUserInFirestore() = flow {
        try {
            emit(Loading)
            auth.currentUser?.apply {
                db.collection(USERS_REF).document(uid).set(mapOf(
                    DISPLAY_NAME to displayName,
                    EMAIL to email,
                    PHOTO_URL to photoUrl?.toString(),
                    CREATED_AT to serverTimestamp()
                )).await()
                emit(Success(true))
            }
        } catch (e: Exception) {
            emit(Error(e))
        }
    }

    override suspend fun revokeAccess() = flow {
        try {
            emit(Loading)
            auth.currentUser?.apply {
                db.collection(USERS_REF).document(uid).delete().await()
                delete().await()
                signInClient.revokeAccess().await()
                oneTapClient.signOut().await()
            }
            emit(Success(true))
        } catch (e: Exception) {
            emit(Error(e))
        }
    }

    override fun getDisplayName() = auth.currentUser?.displayName ?: ""

    override fun getPhotoUrl() = auth.currentUser?.photoUrl.toString()
}