package com.tienditajhonyboy.tiendaapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ProductEntity::class, SaleEntity::class, WorkspaceEntity::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun workspaceDao(): WorkspaceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `workspaces` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                val now = System.currentTimeMillis()
                db.execSQL("""
                    INSERT OR IGNORE INTO `workspaces` (`id`, `name`, `createdAt`)
                    VALUES ('ws_default', 'Principal', $now)
                """.trimIndent())

                db.execSQL("ALTER TABLE `products` ADD COLUMN `workspaceId` TEXT NOT NULL DEFAULT 'ws_default'")
                db.execSQL("ALTER TABLE `sales` ADD COLUMN `workspaceId` TEXT NOT NULL DEFAULT 'ws_default'")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_workspaceId` ON `products` (`workspaceId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_workspaceId_createdAt` ON `products` (`workspaceId`, `createdAt`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sales_workspaceId` ON `sales` (`workspaceId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sales_workspaceId_date` ON `sales` (`workspaceId`, `date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sales_workspaceId_paymentType` ON `sales` (`workspaceId`, `paymentType`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val prefs = context.getSharedPreferences("tienda_prefs", Context.MODE_PRIVATE)
                val initialStoreName = prefs.getString("store_name", "Principal") ?: "Principal"

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tienda_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (instance.workspaceDao().getWorkspaceById("ws_default") == null) {
                            instance.workspaceDao().insertWorkspace(
                                WorkspaceEntity(
                                    id = "ws_default",
                                    name = initialStoreName,
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                instance
            }
        }
    }
}
