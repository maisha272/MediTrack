package com.example.porscherentalapp


import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Car(
    val name: String,
    val model: String,
    val year: Int,
    val rating: Float,
    val kilometers: Int,
    val costPerDay: Int,
    val colorTheme: Int
) : Parcelable