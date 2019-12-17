package arrow.benchmarks

import arrow.fx.IO
import arrow.fx.flatMap
import arrow.fx.handleErrorWith
import arrow.fx.value
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.CompilerControl
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@Fork(2)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10)
@CompilerControl(CompilerControl.Mode.DONT_INLINE)
open class HandleRaisedError {

  @Param("10000")
  var size: Int = 0

  private val dummy = RuntimeException("dummy")

  private fun ioErrorRaisedloop(i: Int): IO<Int> =
    if (i < size)
      IO.raiseException<Int>(dummy)
        .flatMap { x -> IO.just(x + 1) }
        .flatMap { x -> IO.just(x + 1) }
        .handleErrorWith { ioErrorRaisedloop(i + 1) }
    else
      IO.just(i)

  @Benchmark
  fun io(): Int =
    ioErrorRaisedloop(0).unsafeRunSync().value()
}
