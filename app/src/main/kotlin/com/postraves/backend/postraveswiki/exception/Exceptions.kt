package com.postraves.backend.postraveswiki.exception

object ExMessage{
    const val initFailed = "Initialization failed for: "
    const val canNotBeNull: String = "This can't be null: "
    const val canNotFind: String = "Can't find: "
    const val canNotSave: String = "Can't save: "
    const val canNotUpdate: String = "Can't update: "
    const val canNotDelete: String = "Can't delete: "
    const val notAuthenticated: String = "User not authenticated"
}

abstract class ServerInternalException(customMessage: String): Exception(customMessage)
class RedisInitializationException: ServerInternalException("${ExMessage.initFailed} Redis")
class PostgresInitializationException: ServerInternalException("${ExMessage.initFailed} Postgres")
class FirebaseMessagingInitializationException: ServerInternalException("${ExMessage.initFailed} Firebase Messaging")
class RecordFieldNullException(fieldName: String): ServerInternalException("${ExMessage.canNotBeNull} $fieldName")

abstract class BadRequestException(customMessage: String): Exception(customMessage)
class NotFoundException(entity: String, id: String): BadRequestException("${ExMessage.canNotFind} $entity with id or name $id")
class SaveException(entity: String, name: String): BadRequestException("${ExMessage.canNotSave} $entity with id or name $name")
class UpdateException(entity: String, name: String): BadRequestException("${ExMessage.canNotUpdate} $entity with id or name $name")
class FollowingException(userId: Long, entity: String, entityId: String, message: String): BadRequestException("User $userId has following problem with $entity id $entityId: $message")
class DeleteException(sourceOfDeletion: String, entity: String, entityId: String): BadRequestException("${ExMessage.canNotDelete} from $sourceOfDeletion $entity with id $entityId")

abstract class AuthenticationException(customMessage: String): Exception(customMessage)
class NotAuthenticated: AuthenticationException(ExMessage.notAuthenticated)

abstract class FullyInternalException(customMessage: String): Exception(customMessage)
class WeeklyBestSettingException(message: String) : FullyInternalException(message)
