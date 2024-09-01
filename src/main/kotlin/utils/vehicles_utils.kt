package utils

import kotlin.random.Random

fun generateRandomCoordinate(): Pair<Double, Double> {
    val latitude = -90 + Random.nextDouble() * 180
    val longitude = -180 + Random.nextDouble() * 360
    return Pair(latitude, longitude)
}


