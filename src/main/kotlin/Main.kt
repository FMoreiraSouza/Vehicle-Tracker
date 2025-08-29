import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import presentation.SimulationRunner

suspend fun main() {
    val simulationRunner = SimulationRunner()

    runBlocking {
        simulationRunner.startSimulation()
    }

    withContext(Dispatchers.IO) {
        Thread.sleep(Long.MAX_VALUE)
    }
}