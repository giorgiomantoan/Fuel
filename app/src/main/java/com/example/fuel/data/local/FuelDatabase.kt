package com.example.fuel.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fuel.data.local.dao.AnagraficaDao
import com.example.fuel.data.local.dao.PrezzoDao
import com.example.fuel.data.local.entity.AnagraficaImpianto
import com.example.fuel.data.local.entity.PrezzoCarburante

@Database(
    entities = [
        AnagraficaImpianto::class,
        PrezzoCarburante::class
    ],
    version = 1,
    exportSchema = true
)
abstract class FuelDatabase : RoomDatabase() {

    abstract fun anagraficaDao(): AnagraficaDao
    abstract fun prezzoDao(): PrezzoDao

    companion object {
        private const val DB_NAME = "fuel_database.db"

        @Volatile
        private var INSTANCE: FuelDatabase? = null

        fun getInstance(context: Context): FuelDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): FuelDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                FuelDatabase::class.java,
                DB_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
