package com.example.furnexa.data

data class RoomConfig(
    val roomName: String = "",
    val roomType: String = "Living Room", // Living Room, Bedroom, Study Room
    val roomShape: String = "Rectangle", // Rectangle, Square
    val lengthCm: String = "400",
    val widthCm: String = "300",
    val interiorStyle: String = "Modern", // Modern, Minimalist, Traditional
    val budgetPkr: String = "150000"
)
