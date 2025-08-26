package infrastructure.persistence

import domain.model.VehicleState
import domain.repository.VehicleStateRepository
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class FileVehicleStateRepositoryImpl : VehicleStateRepository {
    private val file = File("vehicle_state.dat")
    private val states = loadStates() ?: mutableMapOf()

    override fun saveState(imei: String, state: VehicleState) {
        states[imei] = state
        try {
            ObjectOutputStream(file.outputStream()).use { it.writeObject(states) }
        } catch (e: Exception) {
            println("Falha ao salvar estados dos veículos: ${e.message}")
        }
    }

    override fun loadState(imei: String): VehicleState? {
        return states[imei]
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadStates(): MutableMap<String, VehicleState>? {
        if (!file.exists()) return null
        return try {
            ObjectInputStream(file.inputStream()).use { input ->
                val rawData = input.readObject()
                when (rawData) {
                    is MutableMap<*, *> -> {
                        if (rawData.values.firstOrNull() is Pair<*, *>) {
                            val oldStates = rawData as MutableMap<String, Pair<Pair<Double, Double>, Double>>
                            val newStates = mutableMapOf<String, VehicleState>()
                            oldStates.forEach { (imei, pair) ->
                                val (coords, mileage) = pair
                                val (latitude, longitude) = coords
                                newStates[imei] = VehicleState(latitude, longitude, mileage)
                            }
                            ObjectOutputStream(file.outputStream()).use { it.writeObject(newStates) }
                            newStates
                        } else if (rawData.values.firstOrNull() is VehicleState) {
                            // Dados já estão no formato novo (Map<String, VehicleState>)
                            rawData as MutableMap<String, VehicleState>
                        } else {
                            println("Formato de dados inválido em vehicle_state.dat, reiniciando arquivo")
                            file.delete()
                            null
                        }
                    }
                    else -> {
                        println("Formato de dados inválido em vehicle_state.dat, reiniciando arquivo")
                        file.delete()
                        null
                    }
                }
            }
        } catch (e: Exception) {
            println("Falha ao carregar estados dos veículos: ${e.message}, reiniciando arquivo")
            file.delete() // Remove o arquivo corrompido
            null
        }
    }
}