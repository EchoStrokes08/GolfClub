package com.example.golfclub.models

import android.content.Context
import androidx.room.*
import androidx.room.Room

@Database(entities = [Reserva::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reservaDao(): ReservaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "golf_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}