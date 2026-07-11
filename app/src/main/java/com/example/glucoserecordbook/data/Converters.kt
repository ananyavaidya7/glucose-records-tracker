package com.example.glucoserecordbook.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter fun fromType(type: ReadingType): String = type.name
    @TypeConverter fun toType(value: String): ReadingType = ReadingType.valueOf(value)
    @TypeConverter fun fromStatus(status: ReadingStatus): String = status.name
    @TypeConverter fun toStatus(value: String): ReadingStatus = ReadingStatus.valueOf(value)
}
