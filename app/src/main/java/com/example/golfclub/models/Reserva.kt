
package com.example.golfclub.models
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "reservas")
data class Reserva(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val cliente: String,
    val cancha: String,
    val fecha: String,
    val hora: String,
    val estado: Estado
)
