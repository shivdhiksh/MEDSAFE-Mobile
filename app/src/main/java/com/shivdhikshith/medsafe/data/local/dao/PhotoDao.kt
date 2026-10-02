package com.shivdhikshith.medsafe.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shivdhikshith.medsafe.data.local.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity): Long

    @Query("SELECT * FROM photos WHERE medicineId = :medicineId ORDER BY createdAt DESC")
    fun observePhotosForMedicine(medicineId: Long): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE medicineId = :medicineId ORDER BY createdAt DESC")
    suspend fun getPhotosForMedicine(medicineId: Long): List<PhotoEntity>

    @Query("SELECT * FROM photos WHERE id = :photoId")
    suspend fun getPhotoById(photoId: Long): PhotoEntity?

    @Query("DELETE FROM photos WHERE id = :photoId")
    suspend fun deletePhotoById(photoId: Long)

    @Query("DELETE FROM photos WHERE medicineId = :medicineId")
    suspend fun deletePhotosForMedicine(medicineId: Long)

    @Query("SELECT * FROM photos ORDER BY createdAt DESC")
    suspend fun getAllPhotos(): List<PhotoEntity>

    @Query("DELETE FROM photos")
    suspend fun deleteAllPhotos()
}
