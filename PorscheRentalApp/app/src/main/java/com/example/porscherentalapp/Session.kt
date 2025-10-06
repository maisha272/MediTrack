package com.example.porscherentalapp


object Session {
    var balance: Int = 500
    val favourites = mutableSetOf<String>()
    val bookings = mutableListOf<Booking>()
}
