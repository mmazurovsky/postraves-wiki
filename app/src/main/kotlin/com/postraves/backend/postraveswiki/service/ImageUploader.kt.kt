package com.postraves.backend.postraveswiki.service

import com.google.firebase.cloud.StorageClient
import com.postraves.backend.postraveswiki.util.DateTimeProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

abstract class ImageUploaderAbstract(
    private val bucketNameAndPath: String,
) {

    @Autowired
    @Lazy
    private lateinit var storageClient: StorageClient
    @Autowired
    @Lazy
    private lateinit var dateTimeProvider: DateTimeProvider
    @Value("\${spring.profiles.active}")
    private val activeProfile: String = "undefined"


    //'${_possibleTestBucketPrefix}images/$folderName/image-${DateTime.now().toUtc()}.jpg');

    // TODO images resizing

    fun uploadImage(imageByteArray: ByteArray): String {
        val bucketServerTypePrefix = if (activeProfile == "test") "test/" else ""
        val bucketNameAndPathWithPrefix = "$bucketServerTypePrefix$bucketNameAndPath"
        val imagePath = "image-${dateTimeProvider.getNow()}.jpg"
        val uploaded = storageClient.bucket(bucketNameAndPathWithPrefix).create(imagePath, imageByteArray, "image/jpg")
        val urlOfUploaded = uploaded.signUrl(36500, TimeUnit.DAYS)
        return urlOfUploaded.toString()
    }
}

@Service
class EventImageUploader : ImageUploaderAbstract("images/event/")
@Service
class ArtistImageUploader : ImageUploaderAbstract("images/artist/")
@Service
class UnityImageUploader : ImageUploaderAbstract("images/unity/")
@Service
class PlaceImageUploader : ImageUploaderAbstract("images/place/")