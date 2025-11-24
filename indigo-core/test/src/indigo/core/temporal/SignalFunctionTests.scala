package indigo.core.temporal

import indigoengine.shared.datatypes.Radians
import indigo.core.datatypes.Vector2
import indigoengine.shared.datatypes.Seconds

class SignalFunctionTests extends munit.FunSuite {

  val f: Int => String =
    (i: Int) => "count: " + i.toString

  val g: String => Boolean =
    (s: String) => s.length > 10

  val x: Int => Boolean =
    (i: Int) => i > 10

  test("lift / apply / arr (construction)") {
    assertEquals((Signal.fixed(10) |> SignalFunction(f)).at(Seconds.zero), "count: 10")
    assertEquals((Signal.fixed(20) |> SignalFunction.arr(f)).at(Seconds.zero), "count: 20")
    assertEquals((Signal.fixed(30) |> SignalFunction.lift(f)).at(Seconds.zero), "count: 30")
  }

  test("andThen / >>>") {
    assertEquals((Signal.fixed(10) |> (SignalFunction(f) `andThen` SignalFunction(g))).at(Seconds.zero), false)
    assertEquals((Signal.fixed(10000) |> (SignalFunction(f) >>> SignalFunction(g))).at(Seconds.zero), true)
  }

  test("parallel / &&& / and") {
    assertEquals(
      (Signal.fixed(100) |> (SignalFunction(f) `and` SignalFunction(x))).at(Seconds.zero),
      ("count: 100", true)
    )
    assertEquals((Signal.fixed(1) |> (SignalFunction(f) &&& SignalFunction(x))).at(Seconds.zero), ("count: 1", false))
  }

  test("SignalFunctions should be able to compose signal functions") {
    val f = SignalFunction.lift((i: Int) => s"$i")
    val g = SignalFunction.lift((s: String) => s.length < 2)

    val h: SignalFunction[Int, Boolean] = f `andThen` g

    assertEquals(h.run(Signal.fixed(1)).at(Seconds.zero), true)
    assertEquals(h.run(Signal.fixed(1000)).at(Seconds.zero), false)
  }

  test("SignalFunctions should be able to run signal functions and parallel") {
    val f = SignalFunction.lift((i: Int) => s"$i")
    val g = SignalFunction.lift((i: Int) => i < 10)

    val h: SignalFunction[Int, (String, Boolean)] = f `and` g

    assertEquals(h.run(Signal.fixed(1)).at(Seconds.zero), ("1", true))
    assertEquals(h.run(Signal.fixed(1000)).at(Seconds.zero), ("1000", false))
  }

  test("Fuller example") {
    val makeRange: SignalFunction[Boolean, List[Int]] =
      SignalFunction { p =>
        val num = if (p) 10 else 5
        (1 to num).toList
      }

    val chooseCatsOrDogs: SignalFunction[Boolean, String] =
      SignalFunction(p => if (p) "dog" else "cat")

    val howManyPets: SignalFunction[(List[Int], String), List[String]] =
      SignalFunction { case (l, str) =>
        l.map(_.toString() + " " + str)
      }

    val signal = Signal.Pulse(Seconds(1))

    val signalFunction = (makeRange &&& chooseCatsOrDogs) >>> howManyPets

    val actual1   = (signal |> signalFunction).at(Seconds.zero)
    val expected1 = List("1 dog", "2 dog", "3 dog", "4 dog", "5 dog", "6 dog", "7 dog", "8 dog", "9 dog", "10 dog")
    assertEquals(actual1, expected1)

    val actual2   = (signal |> signalFunction).at(Seconds(1))
    val expected2 = List("1 cat", "2 cat", "3 cat", "4 cat", "5 cat")
    assertEquals(actual2, expected2)
  }

  import indigo.core.temporal.SignalFunction as SF

  test("lerp") {
    val sf = Signal.Time |> SF.lerp(Seconds(10))

    assert(sf.at(Seconds(-1)) == 0.0) // clamped
    assert(sf.at(Seconds(0)) == 0.0)
    assert(sf.at(Seconds(1)) == 0.1)
    assert(sf.at(Seconds(2)) == 0.2)
    assert(sf.at(Seconds(3)) == 0.3)
    assert(sf.at(Seconds(4)) == 0.4)
    assert(sf.at(Seconds(5)) == 0.5)
    assert(sf.at(Seconds(6)) == 0.6)
    assert(sf.at(Seconds(7)) == 0.7)
    assert(sf.at(Seconds(8)) == 0.8)
    assert(sf.at(Seconds(9)) == 0.9)
    assert(sf.at(Seconds(10)) == 1.0)
    assert(sf.at(Seconds(11)) == 1.0) // clamped
  }

  test("easeIn") {
    val sf = Signal.Time |> SF.easeIn(Seconds(10))

    assert(clue(round(sf.at(Seconds(0)))) == clue(0.0))
    assert(clue(round(sf.at(Seconds(1)))) == clue(0.01))
    assert(clue(round(sf.at(Seconds(2)))) == clue(0.04))
    assert(clue(round(sf.at(Seconds(3)))) == clue(0.09))
    assert(clue(round(sf.at(Seconds(4)))) == clue(0.16))
    assert(clue(round(sf.at(Seconds(5)))) == clue(0.25))
    assert(clue(round(sf.at(Seconds(6)))) == clue(0.36))
    assert(clue(round(sf.at(Seconds(7)))) == clue(0.48))
    assert(clue(round(sf.at(Seconds(8)))) == clue(0.64))
    assert(clue(round(sf.at(Seconds(9)))) == clue(0.81))
    assert(clue(round(sf.at(Seconds(10)))) == clue(1.0))
  }

  test("easeOut") {
    val sf = Signal.Time |> SF.easeOut(Seconds(10))

    assert(clue(round(sf.at(Seconds(0)))) == clue(0.0))
    assert(clue(round(sf.at(Seconds(1)))) == clue(0.18))
    assert(clue(round(sf.at(Seconds(2)))) == clue(0.35))
    assert(clue(round(sf.at(Seconds(3)))) == clue(0.51))
    assert(clue(round(sf.at(Seconds(4)))) == clue(0.64))
    assert(clue(round(sf.at(Seconds(5)))) == clue(0.75))
    assert(clue(round(sf.at(Seconds(6)))) == clue(0.84))
    assert(clue(round(sf.at(Seconds(7)))) == clue(0.9))
    assert(clue(round(sf.at(Seconds(8)))) == clue(0.96))
    assert(clue(round(sf.at(Seconds(9)))) == clue(0.99))
    assert(clue(round(sf.at(Seconds(10)))) == clue(1.0))
  }

  test("easeInOut") {
    val sf = Signal.Time |> SF.easeInOut(Seconds(10))

    assert(clue(round(sf.at(Seconds(0)))) == clue(0.0))
    assert(clue(round(sf.at(Seconds(1)))) == clue(0.02))
    assert(clue(round(sf.at(Seconds(2)))) == clue(0.09))
    assert(clue(round(sf.at(Seconds(3)))) == clue(0.2))
    assert(clue(round(sf.at(Seconds(4)))) == clue(0.34))
    assert(clue(round(sf.at(Seconds(5)))) == clue(0.49))
    assert(clue(round(sf.at(Seconds(6)))) == clue(0.65))
    assert(clue(round(sf.at(Seconds(7)))) == clue(0.79))
    assert(clue(round(sf.at(Seconds(8)))) == clue(0.9))
    assert(clue(round(sf.at(Seconds(9)))) == clue(0.97))
    assert(clue(round(sf.at(Seconds(10)))) == clue(1.0))
  }

  test("wrap") {
    val sf = Signal.Time |> SF.wrap(Seconds(10))

    assert(sf.at(Seconds(0)) == Seconds(0))
    assert(sf.at(Seconds(5)) == Seconds(5))
    assert(sf.at(Seconds(10)) == Seconds(0))
    assert(sf.at(Seconds(15)) == Seconds(5))
    assert(sf.at(Seconds(20)) == Seconds(0))
    assert(sf.at(Seconds(21)) == Seconds(1))
  }

  test("clamp") {
    val sf = Signal.Time |> SF.clamp(Seconds(2), Seconds(5))

    assert(sf.at(Seconds(0)) == Seconds(2))
    assert(sf.at(Seconds(2)) == Seconds(2))
    assert(sf.at(Seconds(3)) == Seconds(3))
    assert(sf.at(Seconds(4)) == Seconds(4))
    assert(sf.at(Seconds(5)) == Seconds(5))
    assert(sf.at(Seconds(6)) == Seconds(5))
    assert(sf.at(Seconds(10)) == Seconds(5))
  }

  test("step") {
    val sf = Signal.Time |> SF.step(Seconds(10))

    assert(sf.at(Seconds(0)) == false)
    assert(sf.at(Seconds(5)) == false)
    assert(sf.at(Seconds(10)) == true)
    assert(sf.at(Seconds(15)) == true)
    assert(sf.at(Seconds(20)) == true)
  }

  test("sin") {
    val sf = Signal.Time |> SF.sin

    assert(sf.at(Seconds(-1)) == 0.0d)
    assert(sf.at(Seconds(-0.25)) == -1.0d)
    assert(sf.at(Seconds(0)) == 0.0d)
    assert(sf.at(Seconds(0.25)) == 1.0d)
    assert(sf.at(Seconds(1)) == 0.0d)
  }

  test("cos") {
    val sf = Signal.Time |> SF.cos

    assert(sf.at(Seconds(-1)) == 1.0d)
    assert(sf.at(Seconds(-0.5)) == -1.0d)
    assert(sf.at(Seconds(0)) == 1.0d)
    assert(sf.at(Seconds(0.5)) == -1.0d)
    assert(sf.at(Seconds(1)) == 1.0d)
  }

  test("orbit") {
    val sf = Signal.Time |> SF.orbit(Vector2(0), 1, Radians.zero)

    assert(clue(sf.at(Seconds(0))) ~== clue(Vector2(0, 1)))
    assert(clue(sf.at(Seconds(0.25))) ~== clue(Vector2(1, 0)))
    assert(clue(sf.at(Seconds(0.5))) ~== clue(Vector2(0, -1)))
    assert(clue(sf.at(Seconds(0.75))) ~== clue(Vector2(-1, 0)))
    assert(clue(sf.at(Seconds(1.0))) ~== clue(Vector2(0, 1)))
  }

  test("pulse") {
    val sf = Signal.Time |> SF.pulse(Seconds(10))

    assert(sf.at(Seconds(0)) == true)
    assert(sf.at(Seconds(1)) == true)
    assert(sf.at(Seconds(10)) == false)
    assert(sf.at(Seconds(11)) == false)
    assert(sf.at(Seconds(20)) == true)
    assert(sf.at(Seconds(23)) == true)
    assert(sf.at(Seconds(1234)) == false)
    assert(sf.at(Seconds(1243)) == true)
  }

  test("smoothPulse") {
    val sf = Signal.Time |> SF.smoothPulse

    assert(round(sf.at(Seconds(0))) == 0.0)
    assert(round(sf.at(Seconds(0.25))) == 0.5)
    assert(round(sf.at(Seconds(0.5))) == 1.0)
    assert(round(sf.at(Seconds(0.75))) == 0.5)
    assert(round(sf.at(Seconds(1.0))) == 0.0)
    assert(round(sf.at(Seconds(1.5))) == 1.0)
    assert(round(sf.at(Seconds(2.0))) == 0.0)
  }

  test("multiply") {
    val sf = Signal.Time |> SF.multiply(Seconds(10))

    assert(sf.at(Seconds(0)) == Seconds(0))
    assert(sf.at(Seconds(1)) == Seconds(10))
    assert(sf.at(Seconds(2)) == Seconds(20))
    assert(sf.at(Seconds(30)) == Seconds(300))
  }

  def round(d: Double): Double =
    Math.floor(d * 100d) / 100d

}
