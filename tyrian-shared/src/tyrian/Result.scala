package tyrian

import cats.effect.IO
import indigoengine.shared.collections.Batch
import tyrian.classic.cmds.Logger
import tyrian.platform.Cmd

import scala.annotation.tailrec
import scala.annotation.targetName
import scala.util.control.NonFatal

/** An `Result` represents the result of some part of a frame update. It contains a value or an error (exception), and
  * optionally a list of actions to be processed on the next frame.
  */
sealed trait Result[+A] derives CanEqual:

  def isResult: Boolean
  def isError: Boolean

  def unsafeGet: A
  def getOrElse[B >: A](b: => B): B
  def orElse[B >: A](b: => Result[B]): Result[B]

  def unsafeActions: Batch[Action]
  def actionsOrNil: Batch[Action]

  def handleError[B >: A](recoverWith: Throwable => Result[B]): Result[B]

  def log(message: String): Result[A]
  def logCrash(reporter: PartialFunction[Throwable, String]): Result[A]

  def addActions(newActions: Action*): Result[A]
  def addActions(newActions: Batch[Action]): Result[A]
  @targetName("Result-addActions-fromCmd-repeat")
  def addActions(newCmds: Cmd[IO, GlobalMsg]*): Result[A] =
    addActions(Batch.fromSeq(newCmds.map(Action.fromCmd)))
  @targetName("Result-addActions-fromCmd-list")
  def addActions(newCmds: Batch[Cmd[IO, GlobalMsg]]): Result[A] =
    addActions(newCmds.map(Action.fromCmd))

  def addCmds(newCmds: Cmd[IO, GlobalMsg]*): Result[A] =
    addActions(Batch.fromSeq(newCmds))
  def addCmds(newCmds: Batch[Cmd[IO, GlobalMsg]]): Result[A] =
    addActions(newCmds)

  def addGlobalMsgs(msgs: Batch[GlobalMsg]): Result[A] =
    addActions(msgs.map(Action.emit))
  def addGlobalMsgs(msgs: GlobalMsg*): Result[A] =
    addGlobalMsgs(Batch.fromSeq(msgs))

  def createActions(f: A => Batch[Action]): Result[A]

  def clearActions: Result[A]

  def replaceActions(f: Batch[Action] => Batch[Action]): Result[A]

  def actionsAsOutcome: Result[Batch[Action]]

  def mapAll[B](f: A => B, g: Batch[Action] => Batch[Action]): Result[B]

  def map[B](f: A => B): Result[B]

  def mapActions(f: Action => Action): Result[A]

  def ap[B](of: Result[A => B]): Result[B]

  def merge[B, C](other: Result[B])(f: (A, B) => C): Result[C]

  def combine[B](other: Result[B]): Result[(A, B)]

  def flatMap[B](f: A => Result[B]): Result[B]

object Result:

  final case class Next[+A](state: A, actions: Batch[Action]) extends Result[A] {

    def isResult: Boolean = true
    def isError: Boolean  = false

    def unsafeGet: A =
      state
    def getOrElse[B >: A](b: => B): B =
      state
    def orElse[B >: A](b: => Result[B]): Result[B] =
      this

    def unsafeActions: Batch[Action] =
      actions
    def actionsOrNil: Batch[Action] =
      actions

    def handleError[B >: A](recoverWith: Throwable => Result[B]): Result[B] =
      this

    // TODO: Expand to all log types, also add to Outcome in Indigo
    def log(message: String): Result[A] =
      this.addActions(Logger.stdout[IO](message))
    def logCrash(reporter: PartialFunction[Throwable, String]): Result[A] =
      this

    def addActions(newActions: Action*): Result[A] =
      addActions(Batch.fromSeq(newActions))

    def addActions(newActions: Batch[Action]): Result[A] =
      Result(state, actions ++ newActions)

    def createActions(f: A => Batch[Action]): Result[A] =
      Result(state, actions ++ f(state))

    def clearActions: Result[A] =
      Result(state)

    def replaceActions(f: Batch[Action] => Batch[Action]): Result[A] =
      Result(state, f(actions))

    def actionsAsOutcome: Result[Batch[Action]] =
      Result(actions)

    def mapAll[B](f: A => B, g: Batch[Action] => Batch[Action]): Result[B] =
      Result(f(state), g(actions))

    def map[B](f: A => B): Result[B] =
      Result(f(state), actions)

    def mapActions(f: Action => Action): Result[A] =
      Result(state, actions.map(f))

    def ap[B](of: Result[A => B]): Result[B] =
      of match {
        case Error(e, r) =>
          Error(e, r)

        case Next(s, es) =>
          map(s).addActions(es)
      }

    def merge[B, C](other: Result[B])(f: (A, B) => C): Result[C] =
      flatMap(a => other.map(b => (a, b))).map(p => f(p._1, p._2))

    def combine[B](other: Result[B]): Result[(A, B)] =
      other match {
        case Error(e, r) =>
          Error(e, r)

        case Next(s, es) =>
          Result((state, s), actions ++ es)
      }

    def flatMap[B](f: A => Result[B]): Result[B] =
      f(state) match {
        case Error(e, r) =>
          Error(e, r)

        case Next(s, es) =>
          Result(s, actions ++ es)
      }

  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  final case class Error(e: Throwable, crashReporter: PartialFunction[Throwable, String]) extends Result[Nothing] {

    def isResult: Boolean = false
    def isError: Boolean  = true

    def unsafeGet: Nothing =
      throw e
    def getOrElse[B >: Nothing](b: => B): B =
      b
    def orElse[B >: Nothing](b: => Result[B]): Result[B] =
      b

    def unsafeActions: Batch[Action] =
      throw e
    def actionsOrNil: Batch[Action] =
      Batch.empty

    def handleError[B >: Nothing](recoverWith: Throwable => Result[B]): Result[B] =
      recoverWith(e)

    def log(message: String): Result[Nothing] =
      this.addActions(Logger.stdout[IO](message))
    def logCrash(reporter: PartialFunction[Throwable, String]): Result[Nothing] =
      this.copy(crashReporter = reporter)

    def reportCrash: String =
      crashReporter.orElse[Throwable, String] { case (e: Throwable) =>
        e.getMessage + "\n" + e.getStackTrace.mkString("\n")
      }(e)

    def addActions(newActions: Action*): Error                               = this
    def addActions(newActions: Batch[Action]): Error                         = this
    def createActions(f: Nothing => Batch[Action]): Error                    = this
    def clearActions: Error                                                  = this
    def replaceActions(f: Batch[Action] => Batch[Action]): Error             = this
    def actionsAsOutcome: Result[Batch[Action]]                              = this
    def mapAll[B](f: Nothing => B, g: Batch[Action] => Batch[Action]): Error = this
    def map[B](f: Nothing => B): Error                                       = this
    def mapActions(f: Action => Action): Error                               = this
    def ap[B](of: Result[Nothing => B]): Result[B]                           = this
    def merge[B, C](other: Result[B])(f: (Nothing, B) => C): Error           = this
    def combine[B](other: Result[B]): Error                                  = this
    def flatMap[B](f: Nothing => Result[B]): Error                           = this

  }

  object Error {
    def apply(e: Throwable): Error =
      Error(e, { case (ee: Throwable) => ee.getMessage })
  }

  extension [A](l: Batch[Result[A]]) def sequence: Result[Batch[A]] = Result.sequenceBatch(l)

  extension [A, B](t: (Result[A], Result[B]))
    def combine: Result[(A, B)] =
      t._1.combine(t._2)
    def merge[C](f: (A, B) => C): Result[C] =
      t._1.merge(t._2)(f)
    def map2[C](f: (A, B) => C): Result[C] =
      merge(f)

  extension [A, B, C](t: (Result[A], Result[B], Result[C]))
    def combine: Result[(A, B, C)] =
      t match {
        case (Next(s1, es1), Next(s2, es2), Next(s3, es3)) =>
          Result((s1, s2, s3), es1 ++ es2 ++ es3)

        case (Error(e, r), _, _) =>
          Error(e, r)

        case (_, Error(e, r), _) =>
          Error(e, r)

        case (_, _, Error(e, r)) =>
          Error(e, r)
      }
    def merge[D](f: (A, B, C) => D): Result[D] =
      for {
        aa <- t._1
        bb <- t._2
        cc <- t._3
      } yield f(aa, bb, cc)
    def map3[D](f: (A, B, C) => D): Result[D] =
      merge(f)

  /** Creates an outcome from a value, catching any exceptions and converting them to Error outcomes. */
  def apply[A](state: => A): Result[A] =
    try Result.Next[A](state, Batch.empty)
    catch {
      case NonFatal(e) =>
        Result.Error(e)
    }

  /** Creates an outcome from a value and actions, catching any exceptions and converting them to Error outcomes. */
  def apply[A](state: => A, actions: => Batch[Action]): Result[A] =
    try Result.Next[A](state, actions)
    catch {
      case NonFatal(e) =>
        Result.Error(e)
    }

  def unapply[A](outcome: Result[A]): Option[(A, Batch[Action])] =
    outcome match {
      case Result.Error(_, _) =>
        None

      case Result.Next(s, es) =>
        Some((s, es))
    }

  /** Converts an Option into an Result, using the provided error for None values. */
  def fromOption[A](opt: Option[A], error: => Throwable): Result[A] =
    opt match
      case None        => Result.raiseError(error)
      case Some(value) => Result(value)

  /** Creates an Error outcome with the given throwable. */
  def raiseError(throwable: Throwable): Result.Error =
    Result.Error(throwable)

  /** Converts a Batch of outcomes into an outcome containing a Batch. */
  def sequenceBatch[A](l: Batch[Result[A]]): Result[Batch[A]] =
    @tailrec
    def rec(remaining: Batch[Result[A]], accA: Batch[A], accActions: Batch[Action]): Result[Batch[A]] =
      if remaining.isEmpty then Result(accA).addActions(accActions)
      else
        val h = remaining.head
        val t = remaining.tail
        h match
          case Error(e, r) => Error(e, r)
          case Next(s, es) =>
            rec(t, accA ++ Batch(s), accActions ++ es)

    rec(l, Batch.empty, Batch.empty)

  /** Converts a list of outcomes into an outcome containing a list. */
  def sequenceList[A](l: List[Result[A]]): Result[List[A]] =
    @tailrec
    def rec(remaining: List[Result[A]], accA: List[A], accEvents: List[Action]): Result[List[A]] =
      remaining match {
        case Nil =>
          Result(accA).addActions(Batch.fromList(accEvents))

        case Error(e, r) :: _ =>
          Error(e, r)

        case Next(s, es) :: xs =>
          rec(xs, accA ++ List(s), accEvents ++ es.toList)
      }

    rec(l, Nil, Nil)

  /** Combines two outcomes by applying a function to their values. */
  def merge[A, B, C](oa: Result[A], ob: Result[B])(f: (A, B) => C): Result[C] =
    oa.merge(ob)(f)

  /** Combines two outcomes by applying a function to their values. (Alias for merge) */
  def map2[A, B, C](oa: Result[A], ob: Result[B])(f: (A, B) => C): Result[C] =
    merge(oa, ob)(f)

  /** Combines three outcomes by applying a function to their values. */
  def merge3[A, B, C, D](oa: Result[A], ob: Result[B], oc: Result[C])(f: (A, B, C) => D): Result[D] =
    for {
      aa <- oa
      bb <- ob
      cc <- oc
    } yield f(aa, bb, cc)
  def map3[A, B, C, D](oa: Result[A], ob: Result[B], oc: Result[C])(f: (A, B, C) => D): Result[D] =
    merge3(oa, ob, oc)(f)

  def combine[A, B](oa: Result[A], ob: Result[B]): Result[(A, B)] =
    oa.combine(ob)
  def combine3[A, B, C](oa: Result[A], ob: Result[B], oc: Result[C]): Result[(A, B, C)] =
    (oa, ob, oc) match {
      case (Next(s1, es1), Next(s2, es2), Next(s3, es3)) =>
        Result((s1, s2, s3), es1 ++ es2 ++ es3)

      case (Error(e, r), _, _) =>
        Error(e, r)

      case (_, Error(e, r), _) =>
        Error(e, r)

      case (_, _, Error(e, r)) =>
        Error(e, r)
    }

  def join[A](faa: Result[Result[A]]): Result[A] =
    faa match {
      case Error(e, r) =>
        Error(e, r)

      case Next(outcome, es) =>
        Result(outcome.unsafeGet, es ++ outcome.unsafeActions)
    }
  def flatten[A](faa: Result[Result[A]]): Result[A] =
    join(faa)
