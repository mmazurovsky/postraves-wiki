package com.postraves.backend.postraveswiki.security

import lombok.extern.slf4j.Slf4j
import com.postraves.backend.postraveswiki.security.FirebaseConfig
import java.io.FileInputStream
import java.io.FileNotFoundException
import com.google.firebase.FirebaseOptions
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.postraves.backend.postraveswiki.config.logger
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.io.IOException
import javax.annotation.PostConstruct

@Component
@Slf4j
class FirebaseConfig {
    @PostConstruct
    private fun initFirebaseApp() {
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
        FirebaseApp.initializeApp(options)
    }

    companion object {
        var serviceAccount: FileInputStream? = null
    }
}