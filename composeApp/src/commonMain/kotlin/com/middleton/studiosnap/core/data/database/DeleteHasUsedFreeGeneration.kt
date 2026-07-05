package com.middleton.studiosnap.core.data.database

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec

@DeleteColumn(tableName = "user_preferences", columnName = "hasUsedFreeGeneration")
class DeleteHasUsedFreeGeneration : AutoMigrationSpec
