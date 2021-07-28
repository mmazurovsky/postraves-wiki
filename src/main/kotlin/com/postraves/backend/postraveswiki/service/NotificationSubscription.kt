package com.postraves.backend.postraveswiki.service

import com.google.firebase.messaging.FirebaseMessaging
import com.postraves.backend.postraveswiki.config.logger

abstract class NotificationSubscription (
    private val firebaseMessaging: FirebaseMessaging,
    private val topicTitle: String
) {

    fun subscribeToTopic(registrationToken: List<String>, topicDetail: String) {
        val response = firebaseMessaging.subscribeToTopic(registrationToken, "$topicTitle/$topicDetail")
        if (response.errors.size != 0) {
            logger.debug("Errors encountered during subscription to topic $topicTitle/$topicDetail")
        } else {
            logger.debug("Successfully subscribed to topic $topicTitle/$topicDetail")
        }
    }

    fun unsubscribeFromTopic(registrationToken: List<String>, topicDetail: String) {
        val response = firebaseMessaging.unsubscribeFromTopic(registrationToken, "$topicTitle/$topicDetail")
        if (response.errors.size != 0) {
            logger.debug("Errors encountered during unsubscription from topic $topicTitle/$topicDetail")
        } else {
            logger.debug("Successfully unsubscribed from topic $topicTitle/$topicDetail")
        }
    }
}