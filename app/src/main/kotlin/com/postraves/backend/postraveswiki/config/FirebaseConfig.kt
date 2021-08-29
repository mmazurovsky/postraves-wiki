package com.postraves.backend.postraveswiki.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.postraves.backend.postraveswiki.exception.FirebaseMessagingInitializationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn

@Configuration
class FirebaseConfig {

    @Bean
    fun initFirebaseApp(): FirebaseApp {
        return if (FirebaseApp.getApps().isEmpty()) {
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
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
