package com.example.porscherentalapp


object CarRepository {
    val cars: MutableList<Car> = mutableListOf(
        Car("Porsche","911 Carrera",2020,4.5f,15000,237,R.color.goldYellow),
        Car("Porsche","Panamera", 2021,4.8f,10000,280,R.color.royalBlue),
        Car("Porsche","Taycan", 2022,4.6f, 8000,320,R.color.silverGrey),
        Car("Porsche","Macan", 2018,4.0f,30000,180,R.color.metallicGold),
        Car("Porsche","918 Spyder", 2023,4.9f, 5000,400,R.color.burgundy)
    )
}

