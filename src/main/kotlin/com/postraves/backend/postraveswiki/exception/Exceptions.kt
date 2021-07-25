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

abstract class InitializationException(customMessage: String): Exception(customMessage)
class RedisInitializationException: InitializationException("${ExMessage.initFailed} Redis")
class PostgresInitializationException: InitializationException("${ExMessage.initFailed} Postgres")

abstract class NullException(customMessage: String): Exception(customMessage)
class RecordFieldNullException(fieldName: String): NullException("${ExMessage.canNotBeNull} $fieldName")

class NotFoundException(entity: String, id: String): NullException("${ExMessage.canNotFind} $entity with id or name $id")
class SaveException(entity: String, name: String): Exception("${ExMessage.canNotSave} $entity with id or name $name")
class UpdateException(entity: String, name: String): Exception("${ExMessage.canNotUpdate} $entity with id or name $name")

class FollowingException(userId: String, entity: String, entityId: String, message: String): Exception("User $userId has following problem with $entity id $entityId: $message")

class DeleteException(sourceOfDeletion: String, entity: String, entityId: String): Exception("${ExMessage.canNotDelete} from $sourceOfDeletion $entity with id $entityId")
class NotAuthenticated: Exception(ExMessage.notAuthenticated)