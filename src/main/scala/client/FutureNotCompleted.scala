
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}


object FutureNotCompleted {

  def threadNumber: String = f"${Thread.currentThread().getId.toInt}%2d"

  /////////////////////////////////////////////////////
  // Future completion control
  /////////////////////////////////////////////////////

  var futures: ListBuffer[Future[Any]] = ListBuffer()

  def add(future: Future[Any]): Unit = synchronized(futures += future)

  def remove(future: Future[Any]): Unit = synchronized(futures = futures.filter(_ != future))

  def loopTillCompleted: Unit = {
    var futuresOnList = true;

    while (futuresOnList) {
      Thread.sleep(100)
      for (future <- futures) {
        if (future.isCompleted) {
          future.value.get match {
            case Success(v) => println(s"${threadNumber} Success: ${v}")
            case Failure(e) => println(s"${threadNumber} Error: ${e}")
          }
          remove(future)
        }
      }
      if (futures.size == 0) futuresOnList = false
    }
  }

  /////////////////////////////////////////////////////
  // Future factory
  /////////////////////////////////////////////////////

  def createRegisteredFuture: Future[Int] = {
    val future = createFuture
    add(future)
    future
  }

  def createFuture: Future[Int] = Future {
    val i = (Math.random() * 1000).toInt
    println(s"${threadNumber} Future work start: ${i}")
    Thread.sleep((Math.random() * 1000).toLong)
    println(s"${threadNumber} Future work stop:  ${i}")
    if (Math.random > 0.7) throw new RuntimeException(s"${threadNumber} Error for ${i}")
    i
  }

  def futureDoesNotComplete: Unit = {

    val f1 = createRegisteredFuture
    val f2 = f1.map {
      i => createRegisteredFuture
    }
    // This is never completed at the time the 'onComplete' callback is called
    f2.onComplete({
      case Success(j) => println(s"${threadNumber} j: ${j} ")
      case Failure(e) => println(s"${threadNumber} f2 Failure: ${e}")
    })

    loopTillCompleted
    println(s"${threadNumber} All done.")
  }

  def futureCompletes: Unit = {

    val f1 = createRegisteredFuture

    f1.onComplete({
      case Success(i) => {
        val f2 = createRegisteredFuture
        f2.onComplete({
          case Success(j) => println(s"${threadNumber} i: ${i} j: ${j} ${i}+${j}=${i + j}")
          case Failure(e) => println(s"${threadNumber} f2 Failure: ${e}")
        })
      }
      case Failure(e) => println(s"${threadNumber} f1 Failure: ${e}")
    })

    loopTillCompleted
    println(s"${threadNumber} All done.")
  }

  def futureCompletesFor: Unit = {

    for {
      f1 <- createRegisteredFuture
      f2 <- createRegisteredFuture
    } yield {
      println(s"f1: ${f1} f2: ${f2}: f1+f2=${f1+f2}")
    }

    loopTillCompleted
    println(s"${threadNumber} All done.")
  }

  def main(a: Array[String]): Unit = {
    //    futureCompletes
    futureDoesNotComplete
//    futureCompletesFor
  }

}
