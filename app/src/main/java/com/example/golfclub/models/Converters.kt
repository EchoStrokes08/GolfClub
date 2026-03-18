package com.example.golfclub.models

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromEstado(estado: Estado): String = estado.name

    @TypeConverter
    fun toEstado(value: String): Estado = Estado.valueOf(value)
}