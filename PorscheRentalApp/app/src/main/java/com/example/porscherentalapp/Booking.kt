package com.example.porscherentalapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Booking(
    val car: Car,
    val days: Int,
    val total: Int
) : Parcelable
