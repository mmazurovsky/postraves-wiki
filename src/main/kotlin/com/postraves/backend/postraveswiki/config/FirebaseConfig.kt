package com.postraves.backend.postraveswiki.config

import lombok.extern.slf4j.Slf4j
import java.io.FileInputStream
import java.io.FileNotFoundException
import com.google.firebase.FirebaseOptions
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.postraves.backend.postraveswiki.exception.FirebaseMessagingInitializationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.io.IOException
import javax.annotation.PostConstruct

@Component
@Slf4j
class FirebaseConfig {

    @Bean
    private fun initFirebaseApp(): FirebaseApp? {
        try {
            serviceAccount =
                FileInputStream("src/main/resources/secret/postraves-firebase-adminsdk-2s69q-3648f1af4e.json")
        } catch (e: FileNotFoundException) {

            logger.info("Firebase file not found", e)
        }
        var options: FirebaseOptions? = null
        try {
            options = FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()
        } catch (e: IOException) {
            logger.debug("Can't set Firebase credentials", e)
        }
        assert(options != null)
        return if (FirebaseApp.getApps().isEmpty())
            FirebaseApp.initializeApp(options)
        else
            null
    }

    companion object {
        var serviceAccount: FileInputStream? = null
    }

    @Bean
    private fun initFirebaseMessaging(
        @Autowired firebaseApp: FirebaseApp
    ): FirebaseMessaging {
        return FirebaseMessaging.getInstance(firebaseApp) ?: throw FirebaseMessagingInitializationException()
    }
}