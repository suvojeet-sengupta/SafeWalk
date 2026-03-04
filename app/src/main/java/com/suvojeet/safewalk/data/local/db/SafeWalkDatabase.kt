package com.suvojeet.safewalk.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.suvojeet.safewalk.data.local.db.dao.ContactDao
import com.suvojeet.safewalk.data.local.db.entity.ContactEntity

@Database(
    entities = [ContactEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class SafeWalkDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}
