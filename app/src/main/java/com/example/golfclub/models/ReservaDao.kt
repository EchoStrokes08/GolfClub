package com.example.golfclub.models

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservaDao {

    @Query("SELECT * FROM reservas ORDER BY estado,fecha, hora, cancha, cliente DESC")
    fun getAll(): Flow<List<Reserva>>

    @Insert
    suspend fun insert(reserva: Reserva)

    @Update
    suspend fun update(reserva: Reserva)

    @Delete
    suspend fun delete(reserva: Reserva)
}