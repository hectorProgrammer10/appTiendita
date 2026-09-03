package com.tienditajhonyboy.tiendaapp.data.local.agent

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.tienditajhonyboy.tiendaapp.domain.model.InsightType

class AgentConverters {
    @TypeConverter
    fun fromInsightType(value: InsightType): String = value.name

    @TypeConverter
    fun toInsightType(value: String): InsightType = try {
        InsightType.valueOf(value)
    } catch (e: Exception) {
        InsightType.summary
    }
}

@Database(
    entities = [
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        AgentInsightEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(AgentConverters::class)
abstract class AgentDatabase : RoomDatabase() {
    abstract fun agentDao(): AgentDao

    companion object {
        @Volatile
        private var INSTANCE: AgentDatabase? = null

        fun getDatabase(context: Context): AgentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AgentDatabase::class.java,
                    "agent_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
