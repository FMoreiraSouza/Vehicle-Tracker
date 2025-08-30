package domain.model

import kotlin.random.Random

data class RouteArea(
    val minLat: Double,
    val maxLat: Double,
    val minLon: Double,
    val maxLon: Double
) {
    companion object {
        private val routeArea = RouteArea(
            minLat = -5.8,
            maxLat = -4.5,
            minLon = -38.8,
            maxLon = -37.5
        )

        val minLat: Double get() = routeArea.minLat
        val maxLat: Double get() = routeArea.maxLat
        val minLon: Double get() = routeArea.minLon
        val maxLon: Double get() = routeArea.maxLon

        fun generateRandomCoordinate(): Pair<Double, Double> {
            val random = Random
            val lat = random.nextDouble(routeArea.minLat, routeArea.maxLat)
            val lon = random.nextDouble(routeArea.minLon, routeArea.maxLon)
            return Pair(lat, lon)
        }

        fun isInRouteArea(lat: Double, lon: Double): Boolean {
            return lat in routeArea.minLat..routeArea.maxLat &&
                    lon in routeArea.minLon..routeArea.maxLon
        }

        fun generateNearbyCoordinate(lat: Double, lon: Double): Pair<Double, Double> {
            val variation = 0.01
            val newLat = (lat + Random.nextDouble(-variation, variation))
                .coerceIn(routeArea.minLat, routeArea.maxLat)
            val newLon = (lon + Random.nextDouble(-variation, variation))
                .coerceIn(routeArea.minLon, routeArea.maxLon)
            return Pair(newLat, newLon)
        }
    }
}