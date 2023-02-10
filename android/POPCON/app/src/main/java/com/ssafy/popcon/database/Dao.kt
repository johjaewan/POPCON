package com.ssafy.popcon.database

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.room.*
import com.ssafy.popcon.dto.MMSItem
import org.jetbrains.annotations.NotNull

@Dao
interface MMSItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mmsItem: MMSItem)

    @Query("SELECT * FROM mms_item")
    suspend fun load(): List<MMSItem>

    @Query("SELECT beforeDate FROM mms_item WHERE phoneNumber=:phoneNumber")
    suspend fun selectDate(phoneNumber: String): String

    @Query("UPDATE mms_item SET beforeDate=:beforeDate WHERE phoneNumber=:phoneNumber")
    suspend fun updateDate(phoneNumber: String, beforeDate : String)

    @Query("DELETE FROM mms_item")
    suspend fun deleteAll()
}