import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import presentation.SimulationRunner

suspend fun main() {
    val simulationRunner = SimulationRunner()
    simulationRunner.startSimulation()

    withContext(Dispatchers.IO) {
        Thread.sleep(Long.MAX_VALUE)
    }
}