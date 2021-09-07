package com.postraves.backend.postraveswiki.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.postraves.backend.postraveswiki.exception.FirebaseMessagingInitializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn

@Configuration
class FirebaseConfig {

    @Value("\${FIREBASE_TYPE}")
    val firebaseType: String? = null
    @Value("\${FIREBASE_PROJECT_ID}")
    val firebaseProjectId: String? = null
    @Value("\${FIREBASE_PRIVATE_KEY_ID}")
    val firebasePrivateKeyId: String? = null
    @Value("\${FIREBASE_PRIVATE_KEY}")
    val firebasePrivateKey: String? = null
    @Value("\${FIREBASE_CLIENT_EMAIL}")
    val firebaseClientEmail: String? = null
    @Value("\${FIREBASE_CLIENT_ID}")
    val firebaseClientId: String? = null
    @Value("\${FIREBASE_AUTH_URI}")
    val firebaseAuthUri: String? = null
    @Value("\${FIREBASE_TOKEN_URI}")
    val firebaseTokenUri: String? = null
    @Value("\${FIREBASE_AUTH_CERT_URL}")
    val firebaseAuthProviderX509CertUrl: String? = null
    @Value("\${FIREBASE_CLIENT_CERT_URL}")
    val clientX509CertUrl: String? = null

    @Bean
    fun initFirebaseApp(): FirebaseApp {

        fun createValidFirebaseToken(): String {
            val tailLength = 24
            val quantityOfIntervalsWith64CharLength = (firebasePrivateKey!!.length - tailLength) / 64

            var validToken = "-----BEGIN PRIVATE KEY-----\n"
            (0 until quantityOfIntervalsWith64CharLength).forEach {
                validToken += "${firebasePrivateKey!!.substring(it*64, (it+1)*64)}\n"
            }
            validToken += firebasePrivateKey!!.substring(firebasePrivateKey!!.length - tailLength)
            validToken += "\n-----END PRIVATE KEY-----\n"
            return validToken
        }

        return if (FirebaseApp.getApps().isEmpty()) {

            val credentialsAsMap: MutableMap<String, String> = mutableMapOf()
            logger.info("key id = $firebasePrivateKeyId")
            logger.info("key = $firebasePrivateKey")
            credentialsAsMap["type"] = firebaseType!!
            credentialsAsMap["project_id"] = firebaseProjectId!!
            credentialsAsMap["private_key_id"] = firebasePrivateKeyId!!
            credentialsAsMap["private_key"] = createValidFirebaseToken()
            credentialsAsMap["client_email"] = firebaseClientEmail!!
            credentialsAsMap["client_id"] = firebaseClientId!!
            credentialsAsMap["auth_uri"] = firebaseAuthUri!!
            credentialsAsMap["token_uri"] = firebaseTokenUri!!
            credentialsAsMap["auth_provider_x509_cert_url"] = firebaseAuthProviderX509CertUrl!!
            credentialsAsMap["client_x509_cert_url"] = clientX509CertUrl!!

            val credentialsAsJsonString = Json.encodeToString(credentialsAsMap)
            val credentialsAsInputStream = credentialsAsJsonString.byteInputStream()

            val options = FirebaseOptions.builder()
                .setCredentials(
                    GoogleCredentials.fromStream(credentialsAsInputStream)
                )
                .build()
            FirebaseApp.initializeApp(options)
        }
        else FirebaseApp.getInstance()
    }

    @Bean
    @DependsOn("initFirebaseApp")
    fun initFirebaseMessaging(
        @Autowired firebaseApp: FirebaseApp
    ): FirebaseMessaging {
        return FirebaseMessaging.getInstance(firebaseApp) ?: throw FirebaseMessagingInitializationException()
    }
}
