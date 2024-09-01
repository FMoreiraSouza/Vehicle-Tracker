import controller.VehicleController
import kotlin.random.Random
import java.util.Timer
import java.util.TimerTask
import kotlin.math.*
import java.io.File
import java.io.ObjectOutputStream
import java.io.ObjectInputStream

fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371.0 // Raio da Terra em km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}

fun saveVehicleState(vehicleStates: Map<String, Pair<Pair<Double, Double>, Double>>) {
    val file = File("vehicle_state.dat")
    ObjectOutputStream(file.outputStream()).use { it.writeObject(vehicleStates) }
}

fun loadVehicleState(): MutableMap<String, Pair<Pair<Double, Double>, Double>>? {
    val file = File("vehicle_state.dat")
    if (!file.exists()) return null

    ObjectInputStream(file.inputStream()).use {
        return it.readObject() as? MutableMap<String, Pair<Pair<Double, Double>, Double>>
    }
}

fun main() {
    val vehicleController = VehicleController()
    val vehicles = vehicleController.getVehicles()

    if (vehicles.isNullOrEmpty()) {
        println("Nenhum veículo encontrado para atualização.")
        return
    }

    val timer = Timer()
    val vehicleStates = loadVehicleState() ?: mutableMapOf()
    val vehicleSpeeds = mutableMapOf<String, Double>()
    val vehiclePaused = mutableMapOf<String, Boolean>()
    val vehiclePauseTime = mutableMapOf<String, Long>()
    val vehicleLastUpdateTime = mutableMapOf<String, Long>()
    val vehicleHasDefect = mutableMapOf<String, Boolean>()

    vehicles.forEach { vehicle ->
        if (vehicleStates[vehicle.imei] == null) {
            val initialLat = Random.nextDouble(-90.0, 90.0)
            val initialLon = Random.nextDouble(-180.0, 180.0)
            val initialMileage = Random.nextDouble(0.0, 10000.0)
            vehicleStates[vehicle.imei] = Pair(Pair(initialLat, initialLon), initialMileage)
        }

        vehicleSpeeds[vehicle.imei] = Random.nextDouble(40.0, 80.0)
        vehiclePaused[vehicle.imei] = false
        vehiclePauseTime[vehicle.imei] = 0
        vehicleLastUpdateTime[vehicle.imei] = System.currentTimeMillis()
        vehicleHasDefect[vehicle.imei] = false

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                val state = vehicleStates[vehicle.imei]
                if (state != null) {
                    val (prevCoords, prevMileage) = state
                    val (prevLat, prevLon) = prevCoords
                    val speed = vehicleSpeeds[vehicle.imei]!!

                    // Verifica se o veículo já teve um defeito
                    if (vehicleHasDefect[vehicle.imei] == true) {
                        println("Veículo ${vehicle.id} está parado devido a um defeito técnico. Aguarde suporte.")
                        return // Não faz mais atualizações
                    }

                    // Verifica se o veículo está em pausa
                    if (vehiclePaused[vehicle.imei] == true) {
                        if (currentTime >= vehiclePauseTime[vehicle.imei]!!) {
                            vehiclePaused[vehicle.imei] = false
                            println("Veículo ${vehicle.id} retomou a movimentação após a pausa.")
                        } else {
                            println("Veículo ${vehicle.id} está parado e não está atualizando as coordenadas.")
                            return // Não atualiza as coordenadas ou quilometragem
                        }
                    }

                    // Decidir aleatoriamente se o veículo deve parar
                    if (Random.nextDouble() < 0.5) { // 50% chance de parar a cada execução
                        vehiclePaused[vehicle.imei] = true
                        vehiclePauseTime[vehicle.imei] = currentTime + Random.nextLong(10000, 30000) // Pausa por 10 a 30 segundos

                        // Verificar se há defeito técnico
                        if (Random.nextDouble() < 0.2) { // 20% de chance de ter um defeito técnico
                            vehicleHasDefect[vehicle.imei] = true
                            println("Veículo ${vehicle.id} parou devido a um defeito técnico. Problema no motor ou pneu.")
                            return // Não atualiza mais até o suporte ser solicitado
                        } else {
                            println("Veículo ${vehicle.id} parou para uma pausa normal (abastecimento, descarregamento, etc.).")
                        }
                    }

                    // Calcula o tempo decorrido em horas
                    val elapsedTimeHours = (currentTime - vehicleLastUpdateTime[vehicle.imei]!!) / 3600000.0

                    // Calcula a distância percorrida com base na velocidade e no tempo decorrido
                    val distance = speed * elapsedTimeHours

                    // Gera novas coordenadas aleatórias com base na distância percorrida
                    val displacementLat = Random.nextDouble(-0.01, 0.01) * distance
                    val displacementLon = Random.nextDouble(-0.01, 0.01) * distance
                    val newLat = prevLat + displacementLat
                    val newLon = prevLon + displacementLon

                    // Atualiza a quilometragem
                    val newMileage = prevMileage + distance
                    val isMileageUpdated = vehicleController.updateMileage(vehicle.id, newMileage)

                    // Atualiza as coordenadas
                    val isCoordinatesUpdated = vehicleController.updateVehicleCoordinates(vehicle.imei, newLat, newLon)

                    // Formata os valores para duas casas decimais
                    val formattedMileage = String.format("%.2f", newMileage)
                    val formattedSpeed = String.format("%.2f", speed)
                    val formattedDistance = String.format("%.2f", distance)

                    if (isCoordinatesUpdated) {
                        println("Veículo ${vehicle.id} atualizado com sucesso para coordenadas ($newLat, $newLon)")
                    } else {
                        println("Falha ao atualizar o veículo ${vehicle.id} com as coordenadas.")
                    }

                    if (isMileageUpdated) {
                        println("Veículo ${vehicle.id} atualizado com sucesso com quilometragem $formattedMileage Km")
                        println("Velocidade atual: $formattedSpeed Km/h")
                        println("Distância percorrida neste intervalo: $formattedDistance Km")
                    } else {
                        println("Falha ao atualizar o veículo ${vehicle.id} com a quilometragem.")
                    }

                    // Atualiza o estado do veículo
                    vehicleStates[vehicle.imei] = Pair(Pair(newLat, newLon), newMileage)

                    // Atualiza o tempo da última atualização
                    vehicleLastUpdateTime[vehicle.imei] = currentTime
                }
            }
        }, 0, 5000) // Atualiza a cada 5 segundos
    }

    // Salva o estado dos veículos antes de encerrar a aplicação
    Runtime.getRuntime().addShutdownHook(Thread {
        saveVehicleState(vehicleStates)
    })

    // Impede que o main termine imediatamente
    Thread.sleep(Long.MAX_VALUE)
}
