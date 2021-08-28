package com.postraves.backend.postraveswiki.security.dataclass

import com.google.firebase.auth.FirebaseToken

data class Credentials (
    var type: CredentialType? = null,
    var decodedToken: FirebaseToken? = null,
    var idToken: String? = null,
    var session: String? = null
)