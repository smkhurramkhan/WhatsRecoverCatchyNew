package com.catchyapps.whatsdelete.roomdb.appdatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFolders
import com.catchyapps.whatsdelete.roomdb.AppConverterType
import com.catchyapps.whatsdelete.roomdb.appdao.DaoChats
import com.catchyapps.whatsdelete.roomdb.appdao.DaoMessages
import com.catchyapps.whatsdelete.roomdb.appdao.DaoScreenshots
import com.catchyapps.whatsdelete.roomdb.appdao.DaoStatuses
import com.catchyapps.whatsdelete.roomdb.appdao.DaoStatusesFolder
import com.catchyapps.whatsdelete.roomdb.appentities.EntityChats
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFiles
import com.catchyapps.whatsdelete.roomdb.appentities.EntityMessages
import com.catchyapps.whatsdelete.roomdb.appentities.EntityScreenShots
import com.catchyapps.whatsdelete.roomdb.appentities.EntityStatuses

@Database(
    entities = [
        EntityChats::class,
        EntityMessages::class,
        EntityFiles::class,
        EntityScreenShots::class,
        EntityFolders::class,
        EntityStatuses::class
    ],
    version = 2
)
@TypeConverters(
    AppConverterType::class
)
abstract class  DatabaseRecover : RoomDatabase() {
    abstract val messagesDao: DaoMessages
    abstract val chatsDao: DaoChats
    abstract val screenshotsDao: DaoScreenshots
    abstract val statusesFolderDao: DaoStatusesFolder
    abstract val statusesDao: DaoStatuses


    companion object {
        private const val DBNAME = "RecoverDb"


        @Volatile
        private var dbInstance: DatabaseRecover? = null


        @Synchronized
        fun getDbInstance(context: Context?): DatabaseRecover? {
            if (dbInstance == null) {
                dbInstance = Room.databaseBuilder(
                    context!!,
                    DatabaseRecover::class.java,
                    DBNAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return dbInstance
        }
    }
}