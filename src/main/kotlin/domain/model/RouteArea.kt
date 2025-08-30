package domain.model

import application.service.RouteVerificationService
import kotlin.math.pow
import kotlin.math.sqrt

data class RouteArea(
    val minLat: Double,
    val maxLat: Double,
    val minLon: Double,
    val maxLon: Double
) {
    companion object {
        private val routeVerificationService = RouteVerificationService()

        private val routeAreas = listOf(
            RouteArea(-3.90, -3.60, -38.70, -38.30),
            RouteArea(-4.90, -4.70, -39.20, -38.90),
            RouteArea(-7.30, -7.10, -39.50, -39.20),
            RouteArea(-3.80, -3.60, -40.40, -40.10),
            RouteArea(-6.40, -6.20, -39.40, -39.10),
            RouteArea(-5.30, -5.10, -40.80, -40.50),
            RouteArea(-5.20, -5.00, -38.10, -37.80),
            RouteArea(-6.00, -5.80, -40.30, -40.00),
            RouteArea(-4.95, -4.75, -37.95, -37.65),
            RouteArea(-2.95, -2.75, -40.90, -40.60),
            RouteArea(-3.55, -3.35, -39.65, -39.35),
            RouteArea(-4.40, -4.20, -39.35, -39.05),
            RouteArea(-4.60, -4.40, -37.80, -37.50),
            RouteArea(-4.20, -4.00, -38.50, -38.20),
            RouteArea(-3.90, -3.70, -38.60, -38.30)
        )

        fun getRandomRouteArea(): RouteArea {
            return routeAreas.random()
        }

        fun isInRouteArea(lat: Double, lon: Double): Boolean {
            return routeAreas.any { area ->
                lat >= area.minLat && lat <= area.maxLat &&
                        lon >= area.minLon && lon <= area.maxLon
            }
        }

        fun getNearestRouteCoordinate(lat: Double, lon: Double): Pair<Double, Double> {
            val nearestArea = routeAreas.minByOrNull { area ->
                val centerLat = (area.minLat + area.maxLat) / 2
                val centerLon = (area.minLon + area.maxLon) / 2
                sqrt((lat - centerLat).pow(2.0) + (lon - centerLon).pow(2.0))
            }

            return nearestArea?.let {
                var attempts = 0
                while (attempts < 100) {
                    val randomLat = kotlin.random.Random.nextDouble(it.minLat, it.maxLat)
                    val randomLon = kotlin.random.Random.nextDouble(it.minLon, it.maxLon)

                    if (routeVerificationService.isOnRoute(randomLat, randomLon)) {
                        return Pair(randomLat, randomLon)
                    }
                    attempts++
                }
                Pair((it.minLat + it.maxLat) / 2, (it.minLon + it.maxLon) / 2)
            } ?: Pair(-3.73, -38.52)
        }

        fun generateRouteCoordinateInArea(area: RouteArea): Pair<Double, Double> {
            var attempts = 0
            while (attempts < 50) {
                val lat = kotlin.random.Random.nextDouble(area.minLat, area.maxLat)
                val lon = kotlin.random.Random.nextDouble(area.minLon, area.maxLon)

                if (routeVerificationService.isOnRoute(lat, lon)) {
                    return Pair(lat, lon)
                }
                attempts++
            }
            return Pair((area.minLat + area.maxLat) / 2, (area.minLon + area.maxLon) / 2)
        }
    }
}