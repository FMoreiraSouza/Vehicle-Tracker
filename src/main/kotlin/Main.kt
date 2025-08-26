import presentation.SimulationRunner

fun main() {
    val simulationRunner = SimulationRunner()
    simulationRunner.startSimulation()

    Thread.sleep(Long.MAX_VALUE)
}