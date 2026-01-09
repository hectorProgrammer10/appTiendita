package com.tienditajhonyboy.tiendaapp.data.local

import androidx.room.TypeConverter
import com.tienditajhonyboy.tiendaapp.domain.model.CartItem
import com.tienditajhonyboy.tiendaapp.domain.model.PaymentType
import com.tienditajhonyboy.tiendaapp.domain.model.UnitType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromCartItemList(value: List<CartItem>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCartItemList(value: String): List<CartItem> {
        val type = object : TypeToken<List<CartItem>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromUnitType(value: UnitType): String {
        return value.name
    }

    @TypeConverter
    fun toUnitType(value: String): UnitType {
        return UnitType.valueOf(value)
    }

    @TypeConverter
    fun fromPaymentType(value: PaymentType): String {
        return value.name
    }

    @TypeConverter
    fun toPaymentType(value: String): PaymentType {
        return PaymentType.valueOf(value)
    }
}
