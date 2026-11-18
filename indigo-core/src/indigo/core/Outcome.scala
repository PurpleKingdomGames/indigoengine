package indigo.core

import indigo.core.events.GlobalEvent
import indigo.core.events.LogEvent
import indigo.core.utils.IndigoLogger
import indigoengine.shared.collections.Batch
import indigoengine.shared.collections.NonEmptyBatch
import indigoengine.shared.collections.NonEmptyList
import indigoengine.shared.logging.LogEntry
import indigoengine.shared.logging.LogLevel

import scala.annotation.tailrec
import scala.annotation.targetName
import scala.util.control.NonFatal

/** An `Outcome` represents the result of some part of a frame update. It contains a value or an error (exception), and
  * optionally a list of events to be processed on the next frame.
  */
sealed trait Outcome[+A] derives CanEqual:

  def isResult: Boolean
  def isError: Boolean

  def unsafeGet: A
  def getOrElse[B >: A](b: => B): B
  def orElse[B >: A](b: => Outcome[B]): Outcome[B]

  def unsafeGlobalEvents: Batch[GlobalEvent]
  def globalEventsOrNil: Batch[GlobalEvent]

  def handleError[B >: A](recoverWith: Throwable => Outcome[B]): Outcome[B]

  /** Log a message to stdout, with no decoration. */
  def log(message: String): Outcome[A]

  /** Log a message at the given level. */
  def log(message: String, level: LogLevel): Outcome[A]

  /** Log a message to stdout, with no decoration, but only log each message once. */
  def logOnce(message: String): Outcome[A]

  /** Log a message at the given level, but only log each message once. */
  def logOnce(message: String, level: LogLevel): Outcome[A]

  def logCrash(reporter: PartialFunction[Throwable, String]): Outcome[A]

  @targetName("Outcome-addGlobalEvents-repeat")
  def addGlobalEvents(newEvents: => GlobalEvent*): Outcome[A]

  def addGlobalEvents(newEvents: => Batch[GlobalEvent]): Outcome[A]

  def createGlobalEvents(f: A => Batch[GlobalEvent]): Outcome[A]

  def clearGlobalEvents: Outcome[A]

  def replaceGlobalEvents(f: Batch[GlobalEvent] => Batch[GlobalEvent]): Outcome[A]

  def eventsAsOutcome: Outcome[Batch[GlobalEvent]]

  def mapAll[B](f: A => B, g: Batch[GlobalEvent] => Batch[GlobalEvent]): Outcome[B]

  def map[B](f: A => B): Outcome[B]

  def mapGlobalEvents(f: GlobalEvent => GlobalEvent): Outcome[A]

  def ap[B](of: Outcome[A => B]): Outcome[B]

  def merge[B, C](other: Outcome[B])(f: (A, B) => C): Outcome[C]

  def combine[B](other: Outcome[B]): Outcome[(A, B)]

  def flatMap[B](f: A => Outcome[B]): Outcome[B]

object Outcome:

  final case class Result[+A](state: A, globalEvents: Batch[GlobalEvent]) extends Outcome[A] {

    def isResult: Boolean = true
    def isError: Boolean  = false

    def unsafeGet: A =
      state
    def getOrElse[B >: A](b: => B): B =
      state
    def orElse[B >: A](b: => Outcome[B]): Outcome[B] =
      this

    def unsafeGlobalEvents: Batch[GlobalEvent] =
      globalEvents
    def globalEventsOrNil: Batch[GlobalEvent] =
      globalEvents

    def handleError[B >: A](recoverWith: Throwable => Outcome[B]): Outcome[B] =
      this

    def log(message: String): Outcome[A] =
      log(message, LogLevel.StdOut)
    def log(message: String, level: LogLevel): Outcome[A] =
      addGlobalEvents(LogEvent(LogEntry.Log(level, message)))
    def logOnce(message: String): Outcome[A] =
      logOnce(message, LogLevel.StdOut)
    def logOnce(message: String, level: LogLevel): Outcome[A] =
      addGlobalEvents(LogEvent(LogEntry.LogOnce(level, message)))

    def logCrash(reporter: PartialFunction[Throwable, String]): Outcome[A] =
      this

    @targetName("Outcome-addGlobalEvents-repeat")
    def addGlobalEvents(newEvents: => GlobalEvent*): Outcome[A] =
      addGlobalEvents(Batch.fromSeq(newEvents))

    def addGlobalEvents(newEvents: => Batch[GlobalEvent]): Outcome[A] =
      Outcome(state, globalEvents ++ newEvents)

    def createGlobalEvents(f: A => Batch[GlobalEvent]): Outcome[A] =
      Outcome(state, globalEvents ++ f(state))

    def clearGlobalEvents: Outcome[A] =
      Outcome(state)

    def replaceGlobalEvents(f: Batch[GlobalEvent] => Batch[GlobalEvent]): Outcome[A] =
      Outcome(state, f(globalEvents))

    def eventsAsOutcome: Outcome[Batch[GlobalEvent]] =
      Outcome(globalEvents)

    def mapAll[B](f: A => B, g: Batch[GlobalEvent] => Batch[GlobalEvent]): Outcome[B] =
      Outcome(f(state), g(globalEvents))

    def map[B](f: A => B): Outcome[B] =
      Outcome(f(state), globalEvents)

    def mapGlobalEvents(f: GlobalEvent => GlobalEvent): Outcome[A] =
      Outcome(state, globalEvents.map(f))

    def ap[B](of: Outcome[A => B]): Outcome[B] =
      of match {
        case err: Error =>
          err

        case Result(s, es) =>
          map(s).addGlobalEvents(es)
      }

    def merge[B, C](other: Outcome[B])(f: (A, B) => C): Outcome[C] =
      flatMap(a => other.map(b => (a, b))).map(p => f(p._1, p._2))

    def combine[B](other: Outcome[B]): Outcome[(A, B)] =
      other match {
        case err: Error =>
          err

        case Result(s, es) =>
          Outcome((state, s), globalEvents ++ es)
      }

    def flatMap[B](f: A => Outcome[B]): Outcome[B] =
      try
        f(state) match {
          case err: Error =>
            err

          case Result(s, es) =>
            Outcome(s, globalEvents ++ es)
        }
      catch {
        case NonFatal(e) =>
          Outcome.Error(e)
      }

  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  final case class Error(
      e: Throwable,
      crashReporter: PartialFunction[Throwable, String],
      logs: Batch[LogEntry]
  ) extends Outcome[Nothing] {

    def isResult: Boolean = false
    def isError: Boolean  = true

    def unsafeGet: Nothing =
      throw e
    def getOrElse[B >: Nothing](b: => B): B =
      b
    def orElse[B >: Nothing](b: => Outcome[B]): Outcome[B] =
      try b
      catch {
        case NonFatal(thrown) =>
          this.copy(e = thrown)
      }

    def unsafeGlobalEvents: Batch[GlobalEvent] =
      throw e
    def globalEventsOrNil: Batch[GlobalEvent] =
      Batch.empty

    def handleError[B >: Nothing](recoverWith: Throwable => Outcome[B]): Outcome[B] =
      try recoverWith(e)
      catch {
        case NonFatal(thrown) =>
          this.copy(e = thrown)
      }

    def log(message: String): Outcome[Nothing] =
      log(message, LogLevel.StdOut)
    def log(message: String, level: LogLevel): Outcome[Nothing] =
      this.copy(logs = logs :+ LogEntry.Log(level, message))
    def logOnce(message: String): Outcome[Nothing] =
      logOnce(message, LogLevel.StdOut)
    def logOnce(message: String, level: LogLevel): Outcome[Nothing] =
      this.copy(logs = logs :+ LogEntry.LogOnce(level, message))

    def logCrash(reporter: PartialFunction[Throwable, String]): Outcome[Nothing] =
      this.copy(crashReporter = reporter)

    def reportCrash: String =
      crashReporter.orElse[Throwable, String] { case e: Throwable =>
        e.getMessage + "\n" + e.getStackTrace.mkString("\n")
      }(e)

    /** Immediately logs any outstanding log entries, the final words, and the crash report, then throws the error. */
    private[indigo] def crash(finalWords: LogEntry*): Nothing =
      logs.foreach(IndigoLogger.unsafeLogNow)
      finalWords.foreach(IndigoLogger.unsafeLogNow)
      IndigoLogger.unsafeLogNow(LogEntry.error(reportCrash))
      throw e

    @targetName("Outcome-addGlobalEvents-repeat")
    def addGlobalEvents(newEvents: => GlobalEvent*): Error                             = this
    def addGlobalEvents(newEvents: => Batch[GlobalEvent]): Error                       = this
    def createGlobalEvents(f: Nothing => Batch[GlobalEvent]): Error                    = this
    def clearGlobalEvents: Error                                                       = this
    def replaceGlobalEvents(f: Batch[GlobalEvent] => Batch[GlobalEvent]): Error        = this
    def eventsAsOutcome: Outcome[Batch[GlobalEvent]]                                   = this
    def mapAll[B](f: Nothing => B, g: Batch[GlobalEvent] => Batch[GlobalEvent]): Error = this
    def map[B](f: Nothing => B): Error                                                 = this
    def mapGlobalEvents(f: GlobalEvent => GlobalEvent): Error                          = this
    def ap[B](of: Outcome[Nothing => B]): Outcome[B]                                   = this
    def merge[B, C](other: Outcome[B])(f: (Nothing, B) => C): Error                    = this
    def combine[B](other: Outcome[B]): Error                                           = this
    def flatMap[B](f: Nothing => Outcome[B]): Error                                    = this

  }

  object Error {
    def apply(e: Throwable): Error =
      Error(e, { case ee: Throwable => ee.getMessage }, Batch.empty)
  }

  extension [A](b: Batch[Outcome[A]]) def sequence: Outcome[Batch[A]]                 = Outcome.sequenceBatch(b)
  extension [A](b: NonEmptyBatch[Outcome[A]]) def sequence: Outcome[NonEmptyBatch[A]] = Outcome.sequenceNonEmptyBatch(b)
  extension [A](l: List[Outcome[A]]) def sequence: Outcome[List[A]]                   = Outcome.sequenceList(l)
  extension [A](l: NonEmptyList[Outcome[A]]) def sequence: Outcome[NonEmptyList[A]]   = Outcome.sequenceNonEmptyList(l)

  extension [A, B](t: (Outcome[A], Outcome[B]))
    def combine: Outcome[(A, B)] =
      t._1.combine(t._2)
    def merge[C](f: (A, B) => C): Outcome[C] =
      t._1.merge(t._2)(f)
    def map2[C](f: (A, B) => C): Outcome[C] =
      merge(f)

  extension [A, B, C](t: (Outcome[A], Outcome[B], Outcome[C]))
    def combine: Outcome[(A, B, C)] =
      t match {
        case (Result(s1, es1), Result(s2, es2), Result(s3, es3)) =>
          Outcome((s1, s2, s3), es1 ++ es2 ++ es3)

        case (err: Error, _, _) =>
          err

        case (_, err: Error, _) =>
          err

        case (_, _, err: Error) =>
          err
      }
    def merge[D](f: (A, B, C) => D): Outcome[D] =
      for {
        aa <- t._1
        bb <- t._2
        cc <- t._3
      } yield f(aa, bb, cc)
    def map3[D](f: (A, B, C) => D): Outcome[D] =
      merge(f)

  def apply[A](state: => A): Outcome[A] =
    try Outcome.Result[A](state, Batch.empty)
    catch {
      case NonFatal(e) =>
        Outcome.Error(e)
    }

  def apply[A](state: => A, globalEvents: => Batch[GlobalEvent]): Outcome[A] =
    try Outcome.Result[A](state, globalEvents)
    catch {
      case NonFatal(e) =>
        Outcome.Error(e)
    }

  def unapply[A](outcome: Outcome[A]): Option[(A, Batch[GlobalEvent])] =
    outcome match {
      case Outcome.Error(_, _, _) =>
        None

      case Outcome.Result(s, es) =>
        Some((s, es))
    }

  def fromOption[A](opt: Option[A], error: => Throwable): Outcome[A] =
    opt match
      case None        => Outcome.raiseError(error)
      case Some(value) => Outcome(value)

  def raiseError(throwable: Throwable): Outcome.Error =
    Outcome.Error(throwable)

  def sequenceBatch[A](l: Batch[Outcome[A]]): Outcome[Batch[A]] =
    @tailrec
    def rec(remaining: Batch[Outcome[A]], accA: Batch[A], accEvents: Batch[GlobalEvent]): Outcome[Batch[A]] =
      if remaining.isEmpty then Outcome(accA).addGlobalEvents(accEvents)
      else
        val h = remaining.head
        val t = remaining.tail
        h match
          case err: Error    => err
          case Result(s, es) =>
            rec(t, accA ++ Batch(s), accEvents ++ es)

    rec(l, Batch.empty, Batch.empty)

  def sequenceNonEmptyBatch[A](l: NonEmptyBatch[Outcome[A]]): Outcome[NonEmptyBatch[A]] =
    sequence(l.toBatch).map(bb => NonEmptyBatch.fromBatch(bb).get) // Use of get is safe, we know it is non-empty

  def sequenceList[A](l: List[Outcome[A]]): Outcome[List[A]] =
    @tailrec
    def rec(remaining: List[Outcome[A]], accA: List[A], accEvents: List[GlobalEvent]): Outcome[List[A]] =
      remaining match {
        case Nil =>
          Outcome(accA).addGlobalEvents(Batch.fromList(accEvents))

        case (err: Error) :: _ =>
          err

        case Result(s, es) :: xs =>
          rec(xs, accA ++ List(s), accEvents ++ es.toList)
      }

    rec(l, Nil, Nil)

  def sequenceNonEmptyList[A](l: NonEmptyList[Outcome[A]]): Outcome[NonEmptyList[A]] =
    sequence(l.toList).map(ll => NonEmptyList.fromList(ll).get) // Use of get is safe, we know it is non-empty

  def merge[A, B, C](oa: Outcome[A], ob: Outcome[B])(f: (A, B) => C): Outcome[C] =
    oa.merge(ob)(f)
  def map2[A, B, C](oa: Outcome[A], ob: Outcome[B])(f: (A, B) => C): Outcome[C] =
    merge(oa, ob)(f)
  def merge3[A, B, C, D](oa: Outcome[A], ob: Outcome[B], oc: Outcome[C])(f: (A, B, C) => D): Outcome[D] =
    for {
      aa <- oa
      bb <- ob
      cc <- oc
    } yield f(aa, bb, cc)
  def map3[A, B, C, D](oa: Outcome[A], ob: Outcome[B], oc: Outcome[C])(f: (A, B, C) => D): Outcome[D] =
    merge3(oa, ob, oc)(f)

  def combine[A, B](oa: Outcome[A], ob: Outcome[B]): Outcome[(A, B)] =
    oa.combine(ob)
  def combine3[A, B, C](oa: Outcome[A], ob: Outcome[B], oc: Outcome[C]): Outcome[(A, B, C)] =
    (oa, ob, oc) match {
      case (Result(s1, es1), Result(s2, es2), Result(s3, es3)) =>
        Outcome((s1, s2, s3), es1 ++ es2 ++ es3)

      case (err: Error, _, _) =>
        err

      case (_, err: Error, _) =>
        err

      case (_, _, err: Error) =>
        err
    }

  def join[A](faa: Outcome[Outcome[A]]): Outcome[A] =
    faa match {
      case err: Error =>
        err

      case Result(err: Error, _) =>
        err

      case Result(Result(s, es2), es) =>
        Outcome(s, es ++ es2)
    }
  def flatten[A](faa: Outcome[Outcome[A]]): Outcome[A] =
    join(faa)
