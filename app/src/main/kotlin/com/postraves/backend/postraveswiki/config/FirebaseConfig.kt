package com.postraves.backend.postraveswiki.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.postraves.backend.postraveswiki.exception.FirebaseMessagingInitializationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
import org.springframework.util.ResourceUtils
import java.io.FileInputStream

@Component
class FirebaseConfig {

    private val firebaseConfigFile = ResourceUtils.getFile("classpath:secret/postraves-firebase-adminsdk-2s69q-3648f1af4e.json")

    @Bean
    fun initFirebaseApp(): FirebaseApp {
        return if (FirebaseApp.getApps().isEmpty()) {
            val serviceAccount =
                FileInputStream(firebaseConfigFile)
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()
            FirebaseApp.initializeApp(options)
        } else
            FirebaseApp.getInstance()
    }

    @Bean
    @DependsOn("initFirebaseApp")
    fun initFirebaseMessaging(
        @Autowired firebaseApp: FirebaseApp
    ): FirebaseMessaging {
        return FirebaseMessaging.getInstance(firebaseApp) ?: throw FirebaseMessagingInitializationException()
    }
}
