import kotlinx.coroutines.*

fun main() {

    // method 2
    val limitedParallelismContext = Dispatchers.IO.limitedParallelism(2)

    runBlocking {
        val jobs = (1..10).map {
            // swap out the dispatcher here
            launch(limitedParallelismContext) {
                println("it at ${System.currentTimeMillis()}")
                Thread.sleep(200)
            }
        }
        jobs.joinAll()
    }
}