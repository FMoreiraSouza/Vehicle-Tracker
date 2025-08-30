package application.service

class RouteVerificationService {

    private val routePolygons = listOf(
        listOf(
            Pair(-2.90, -41.50),
            Pair(-3.30, -41.00),
            Pair(-4.40, -38.50),
            Pair(-5.20, -38.00),
            Pair(-7.50, -38.50),
            Pair(-7.20, -39.50),
            Pair(-4.00, -41.00),
            Pair(-2.90, -41.50)
        ),

        listOf(
            Pair(-3.60, -38.70),
            Pair(-3.90, -38.40),
            Pair(-4.00, -38.30),
            Pair(-3.70, -38.20),
            Pair(-3.50, -38.50),
            Pair(-3.60, -38.70)
        ),

        listOf(
            Pair(-7.10, -39.40),
            Pair(-7.30, -39.20),
            Pair(-7.40, -39.30),
            Pair(-7.20, -39.50),
            Pair(-7.10, -39.40)
        )
    )

    fun isOnRoute(lat: Double, lon: Double): Boolean {
        return isPointInPolygon(lat, lon, routePolygons)
    }

    private fun isPointInPolygon(lat: Double, lon: Double, polygons: List<List<Pair<Double, Double>>>): Boolean {
        for (polygon in polygons) {
            if (isPointInSinglePolygon(lat, lon, polygon)) {
                return true
            }
        }
        return false
    }

    private fun isPointInSinglePolygon(lat: Double, lon: Double, polygon: List<Pair<Double, Double>>): Boolean {
        var inside = false
        var j = polygon.size - 1

        for (i in polygon.indices) {
            val vertexI = polygon[i]
            val vertexJ = polygon[j]

            if ((vertexI.second > lon) != (vertexJ.second > lon) &&
                lat < (vertexJ.first - vertexI.first) * (lon - vertexI.second) /
                (vertexJ.second - vertexI.second) + vertexI.first) {
                inside = !inside
            }
            j = i
        }

        return inside
    }
}