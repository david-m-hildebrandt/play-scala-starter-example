package client


import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}
import scala.util.{Failure, Success, Try}

object FutureRegistrar {

  var futures: ListBuffer[Future[Any]] = ListBuffer()

  def main(a: Array[String]): Unit = {

    for (i <- 0 to 10) {
      add(Future[Int] {
        Thread.sleep((Math.random() * 10000).longValue())
        if (Math.random > 0.7) throw new RuntimeException(s"Error for ${i}")
        i
      })
    }

    loopTillCompleted

  }

  def add(future: Future[Any]): Unit = {
    synchronized(
      futures += future
    )
  }

  def remove(future: Future[Any]): Unit = {
    synchronized(
      futures = futures.filter(_ != future)
    )
  }

  def loopTillCompleted: Unit = {
    var futuresOnList = true;

    while (futuresOnList) {
      Thread.sleep(100)
      //      println(s"${threadNumber} loop")

      for (future <- futures) {
        if (future.isCompleted) {
          future.value.get match {
            case Success(v) => println(s"${threadNumber} Success: ${v}")
            case Failure(e) => println(s"${threadNumber} Error: ${e}")
          }
          remove(future)
        }
      }

      if (futures.size == 0) {
        futuresOnList = false
      }
    }
  }

  def threadNumber: String = {
    f"${Thread.currentThread().getId.toInt}%2d"
  }

}


object FutureStudy {

  def threadNumber: String = {
    FutureRegistrar.threadNumber
  }

  def main(a: Array[String]): Unit = {
    //    exampleManagedMultipleCallbacksRegistered
    // exampleConditionalCallChained
    //    exampleConditionalCallChainedFlatMapped
    //exampleConditionalCallChainedMapped
    // exampleMappedFutures
    // exampleFuturesWithReturn
    //examplePromise
//    examplePromiseMultipleFutures
    val r = exampleCompositionClient
    println(s"r: ${r}")
  }

  def exampleCompositionClient : (Int, Int) = {

    val r: Future[(Int, Int)] = exampleComposition.map(v=> (v._1, v._2))

    Await.ready(r, 1000 milli)

    if (r.value.get.isSuccess)
      r.value.get.get
    else
      (0,0)
  }


  def exampleComposition: Future[(Int,Int)] = {

    val f1 = createRegisteredFuture
    val f2 = createRegisteredFuture

    val promise = Promise[(Int, Int)]

    f1.onComplete({
      case Success(i) => {
        f2.onComplete({
          case Success(j) => promise.trySuccess((i, j))
          case Failure(e) => promise.tryFailure(e)
        })
      }
      case Failure(e) => {
        promise.tryFailure(e)
      }
    })

    promise.future
  }

  /**
    * promise used
    */
  def examplePromiseMultipleFutures: Unit = {

    val f1 = createRegisteredFuture
    val f2 = createRegisteredFuture

    def getFirstCompleted(f: Future[Int], g: Future[Int]) : Future[Int] = {

      val promise = Promise[Int]

      f.onComplete {
        case Success(i)  => promise.trySuccess(i)
        case Failure(e)  => promise.tryFailure(e)
      }

      g.onComplete {
        case Success(i)  => promise.trySuccess(i)
        case Failure(e)  => promise.tryFailure(e)
      }

      promise.future
    }

    val f3 = getFirstCompleted(f1,f2)
    FutureRegistrar.add(f3)

    f3.onComplete{
      case Success(i) => println(s"${threadNumber} The winning value is: ${i}")
      case Failure(e) => println(s"${threadNumber} NO WINNER: Failure: ${e}")
    }

    FutureRegistrar.loopTillCompleted
    println(s"${threadNumber} All done.")
  }


  /**
    * promise used
    */
  def examplePromise: Unit = {

    val promise = Promise[Int]
    val future = promise.future
    FutureRegistrar.add(future)

    val producer = Future {
      val i = 24
      //
      promise success i
    }

    FutureRegistrar.add(producer)

    val consumer = Future {
      future onComplete {
        case Success(i) => println(s"${threadNumber} Success: ${i}")
      }
    }
    FutureRegistrar.loopTillCompleted
    println(s"${threadNumber} All done.")
  }

  /**
    * future completion with conditional call, flat mapped
    */
  def exampleConditionalCallChainedFlatMapped: Unit = {

    val f1 = createRegisteredFuture
    val f2 = f1.flatMap {
      // a second future is created
      i => createRegisteredFuture
    }
    f2.onComplete({
      case Success(j) => println(s"${threadNumber} j: ${j} ")
      case Failure(e) => println(s"${threadNumber} f2 Failure: ${e}")
    })

    FutureRegistrar.loopTillCompleted
    println(s"${threadNumber} All done.")
  }

  /**
    * future completion with conditional call, flat mapped
    */
  def exampleConditionalCallChainedMapped: Unit = {

    val f1 = createRegisteredFuture
    val f2 = f1.map {
      // no second future is created
      i => i
    }
    f2.onComplete({
      case Success(j) => println(s"${threadNumber} j: ${j} ")
      case Failure(e) => println(s"${threadNumber} f2 Failure: ${e}")
    })

    FutureRegistrar.loopTillCompleted
    println(s"${threadNumber} All done.")
  }


  /**
    * futures mapped from one to the other
    */
  def exampleMappedFutures: Unit = {

    createRegisteredFuture.map(v1 =>
      createRegisteredFuture.map(v2 =>
        createRegisteredFuture.map(v3 => {
          println(s"${threadNumber} v1: ${v1} v2: ${v2} v3: ${v3}")
        })))

    FutureRegistrar.loopTillCompleted
    println(s"${threadNumber} All done.")
  }

  /**
    * futures mapped from one to the other
    */

  def exampleFuturesWithReturn: Unit = {

    val r: (Int, Int) = {

      val f1: Future[Int] = createRegisteredFuture
      val f2 = createRegisteredFuture

      Await.ready(f1, 1000 milli)
      Await.result(f2, 1000 milli)

      def getValue(f: Future[Int]): Int = if (f.isCompleted && f.value.get.isSuccess) f.value.get.get else 0

      val f1_v = getValue(f1)
      val f2_v = getValue(f2)

      (f1_v, f2_v)
    }

    println(s"${threadNumber} r: ${r}")

    FutureRegistrar.loopTillCompleted
    println(s"${threadNumber} All done.")
  }


  /**
    * future completion with conditional call
    */
  def exampleConditionalCallChained: Unit = {

    val f1 = createRegisteredFuture

    f1.onComplete({
      case Success(i) => {
        if (i > 2) {
          val f2 = createRegisteredFuture
          f2.onComplete({
            case Success(j) => println(s"${threadNumber} i: ${i} j: ${j} ${i}+${j}=${i + j}")
            case Failure(e) => println(s"${threadNumber} f2 Failure: ${e}")
          })
        }
      }
      case Failure(e) => println(s"${threadNumber} f1 Failure: ${e}")
    })

    FutureRegistrar.loopTillCompleted
    println(s"${threadNumber} All done.")
  }

  /**
    * Registers more than one callback, using a future added to the FutureRegistrar list.
    */
  def exampleManagedMultipleCallbacksRegistered: Unit = {

    for (i <- 0 to 10) {
      var future = createRegisteredFuture

      future.onComplete({
        case Success(v) => println(s"${threadNumber} CallbackA: future(${i}) Success: v: ${v}")
        case Failure(e) => println(s"${threadNumber} CallbackA: future(${i}) Failure: e: ${e}")
      })

      future.onComplete({
        case Success(v) => println(s"${threadNumber} CallbackB: future(${i}) Success: v: ${v}")
        case Failure(e) => println(s"${threadNumber} CallbackB: future(${i}) Failure: e: ${e}")
      })
    }

    FutureRegistrar.loopTillCompleted
    println(s"${threadNumber} All done.")

  }

  /**
    * Registers more than one callback
    */
  def exampleManagedMultipleCallbacks: Unit = {

    for (i <- 0 to 10) {
      var future = createFuture
      FutureRegistrar.add(future)

      future.onComplete({
        case Success(v) => println(s"${threadNumber} CallbackA: future(${i}) Success: v: ${v}")
        case Failure(e) => println(s"${threadNumber} CallbackA: future(${i}) Failure: e: ${e}")
      })

      future.onComplete({
        case Success(v) => println(s"${threadNumber} CallbackB: future(${i}) Success: v: ${v}")
        case Failure(e) => println(s"${threadNumber} CallbackB: future(${i}) Failure: e: ${e}")
      })
    }

    FutureRegistrar.loopTillCompleted
    println(s"${threadNumber} All done.")

  }

  def exampleManaged: Unit = {

    for (i <- 0 to 10) {
      var future = createFuture
      FutureRegistrar.add(future)

      future.onComplete({
        case Success(v) => println(s"${threadNumber} future(${i}) Success: v: ${v}")
        case Failure(e) => println(s"${threadNumber} future(${i}) Failure: e: ${e}")
      })
    }

    FutureRegistrar.loopTillCompleted
    println(s"${threadNumber} All done.")
  }

  def createRegisteredFuture: Future[Int] = {
    val future = createFuture
    FutureRegistrar.add(future)
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

  def example_4: Unit = {

    def h(f: Future[Int], g: Future[Int]): Future[(Int, Int)] = {

      val p = Promise[(Int, Int)]()

      var fValue = 0
      f.onComplete({
        case Success(value) => fValue = value
      }
      )

      var gValue = 0
      g.onComplete({
        case Success(value) => gValue = value
      }
      )

      import scala.concurrent.duration._
      Await.result(f, 1000 milli)
      Await.result(g, 1000 milli)

      if (f.isCompleted && g.isCompleted) {
        p.trySuccess(fValue, gValue)
      }

      p.future
    }

    val f = Future {
      Thread.sleep((Math.random() * 1000).toLong)
      1
    }
    val g = Future {
      Thread.sleep((Math.random() * 1000).toLong)
      2
    }

    h(f, g).onComplete({
      case Success(value) => {
        println(s"h: ${value}")
      }
    })

  }


  def example_3: Unit = {

    def h(f: Future[Int], g: Future[Int]): Future[Int] = {

      val p = Promise[Int]()

      f.onComplete({
        case Success(value) => p.trySuccess(value)
      }
      )

      g.onComplete({
        case Success(value) => p.trySuccess(value)
      }
      )

      p.future
    }

    val f = Future {
      Thread.sleep((Math.random() * 1000).toLong)
      1
    }
    val g = Future {
      Thread.sleep((Math.random() * 1000).toLong)
      2
    }

    h(f, g).onComplete({
      case Success(value) => {
        println(s"h: ${value}")
      }
    })

  }

  def example_2: Unit = {

    val f = Future {
      1
    }
    val g = Future {
      2
    }

    f.onComplete({
      case Success(value) => println(s"f: value: ${value}")
    })

    g.onComplete({
      case Success(value) => println(s"g: value: ${value}")
    })

  }

  def example_1: Unit = {
    val f = f1

    f.onComplete({
      case Success(v) => {
        println(s"f complete: t: ${v}")
        v
      }
      case Failure(v) => {
        println(s"f failure: t: ${v}")
        Int.MaxValue
      }
    })

    f.andThen { case _ => "andThen" }

    println("Before sleep")
    Thread.sleep(1000)
    println("After sleep")

  }


  def displayThread(name: String, thread: Thread): Unit = {
    println(s"${name} Thread: ${thread}")
  }

  def delay(): Unit = {
    Thread.sleep((1000 * Math.random()).longValue())
  }

  def f1: Future[Int] = Future {
    println(s"f1: ${Thread.currentThread()}")
    delay
    displayThread("f1", Thread.currentThread())
    1
  }

  def f2: Future[Int] = Future {
    println(s"f2: ${Thread.currentThread()}")
    delay
    displayThread("f2", Thread.currentThread())
    2
  }

  def f3: Future[Int] = Future {
    println(s"f3: ${Thread.currentThread()}")
    delay
    displayThread("f3", Thread.currentThread())
    3
  }

  def complete(t: Try[Int]): Int = {
    t match {
      case Success(v) => {
        println(s"fAll complete: t: ${v}")
        v
      }
      case Failure(v) => {
        println(s"fAll failure: t: ${v}")
        Int.MaxValue
      }
    }
  }

  //  Await.result(f1, 10000000 milli)
  //  Await.result(f2, 10000000 milli)
  //  Await.result(f3, 10000000 milli)

  //  printValue(f1)
  //  printValue(f2)
  //  printValue(f3)

  def printValue(f: Future[Int]): Unit = {
    println(s"f.value.get.get: ${f.value.get.get}")
  }

  /*
  val fAll: Future[(Int, Int, Int)] = {
    for {
      f1_value: Int <- f1
      f2_value <- f2
      f3_value <- f3
    } yield (f1_value, f2_value, f3_value)
  }

  Await.result(fAll, 1000000000 milli)

  println(s"fAll ${fAll.value.get.get}" )

  fAll.onComplete({
    case Success(t) => println(s"fAll complete: t: ${t}")
    case Failure(t) => println(s"fAll failure: t: ${t}")
  })

*/
}
