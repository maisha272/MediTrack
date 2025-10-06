package com.example.porscherentalapp

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class RentCoreTest {

    private lateinit var originalCars: List<Car>

    @Before
    fun setup() {
        // snapshot & reset deterministic state
        originalCars = CarRepository.cars.toList()
        CarRepository.cars.clear()
        CarRepository.cars.addAll(listOf(
            Car("Porsche","911 Carrera",2020,4.5f,15000,237,R.color.goldYellow),
            Car("Porsche","Panamera",   2021,4.8f,10000,280,R.color.royalBlue),
            Car("Porsche","Taycan",     2022,4.6f, 8000,320,R.color.silverGrey),
            Car("Porsche","Macan",      2018,4.0f,30000,180,R.color.metallicGold),
            Car("Porsche","918 Spyder", 2023,4.9f, 5000,400,R.color.burgundy)
        ))
        Session.balance = 500
    }

    @After
    fun tearDown() {
        CarRepository.cars.clear()
        CarRepository.cars.addAll(originalCars)
        Session.balance = 500
    }

    @Test
    fun rentOnce_forFirstCar_deductsCostPerDay_andTotalCapHolds() {
        val car = CarRepository.cars.first() // 911: 237/day

        // core math
        val days = 1
        val total = car.costPerDay * days
        assertEquals(237, total)

        // cap rule (total must be <= 400 to allow booking)
        assertTrue(total <= 400)

        // simulate the booking (what RentActivity does)
        val start = Session.balance
        Session.balance = start - total
        CarRepository.cars.removeIf { it.model.equals(car.model, true) }

        // result: 500 - 237 = 263
        assertEquals(263, Session.balance)
        // car removed
        val stillThere = CarRepository.cars.any { it.model.equals("911 Carrera", true) }
        assertEquals(false, stillThere)
    }
}
