package simple.program.authfirebase.util

object Constant {
    const val ERROR_MESSAGE = "Something went wrong please try again later!"
    //Collection References
    const val USERS_REF = "users"
    //User fields
    const val DISPLAY_NAME = "displayName"
    const val EMAIL = "email"
    const val PHOTO_URL = "photoUrl"
    const val CREATED_AT = "createdAt"

    //Names
    const val SIGN_IN_REQUEST = "signInRequest"
    const val SIGN_UP_REQUEST = "signUpRequest"

    //Messages
    const val SIGN_IN_ERROR_MESSAGE = "16: Cannot find a matching credential."
    const val REVOKE_ACCESS_MESSAGE = "You need to re-authenticate before revoking the access."
}