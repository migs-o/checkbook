package com.example.checkbook.data.converter

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateConverter {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun fromTimestamp(value: String?): LocalDate? {
        return try {
            value?.let { LocalDate.parse(it, formatter) }
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): String? {
        return try {
            date?.format(formatter)
        } catch (e: Exception) {
            null
        }
    }
} 