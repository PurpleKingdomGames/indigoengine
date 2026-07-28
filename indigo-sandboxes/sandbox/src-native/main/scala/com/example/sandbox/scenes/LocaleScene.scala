package com.example.sandbox.scenes

import com.example.sandbox.SandboxGameModel
import indigo.*
import indigo.core.time.DateFormat
import indigo.core.time.TimeFormat
import indigo.scenes.*

/** The native platform has no renderer, asset loading or pointer / keyboard input yet, so the locale strings cannot be
  * drawn, nor a window opened by clicking them. Until those land, this scene logs the locales the service reports. See
  * the JS sandbox for the scene this is standing in for.
  */
object LocaleScene extends Scene[SandboxGameModel]:

  type SceneModel = SandboxGameModel

  val name: SceneName =
    SceneName("LocaleScene")

  val modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext,
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] =
    case FrameTick if !model.loggedLocales =>
      val current =
        context.services.locale.current.map(_.toString).getOrElse("Unknown locale")

      val preferred =
        context.services.locale.preferred.map(_.toString).toList.mkString(", ")

      IndigoLogger.info("Current locale: " + current)
      IndigoLogger.info("Preferred locales: " + preferred)
      IndigoLogger.info("Current Date + Time: " + currentDateTimeString(context))

      Outcome(model.copy(loggedLocales = true))

    case _ =>
      Outcome(model)

  def present(
      context: SceneContext,
      model: SandboxGameModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)

  def currentDateTimeString(ctx: SceneContext): String =
    val date = ctx.services.datetime.current

    val year  = f"${date.year}%04d"
    val month = f"${date.month}%02d"
    val day   = f"${date.day}%02d"

    val dateStr = ctx.services.datetime.dateformat match
      case DateFormat.YearMonthDay => s"$year-$month-$day"
      case DateFormat.DayMonthYear => s"$day-$month-$year"
      case DateFormat.MonthDayYear => s"$month-$day-$year"

    val minute = f"${date.minute}%02d"
    val second = f"${date.second}%02d"
    val millis = f"${date.millisecond}%03d"

    val timeStr = ctx.services.datetime.timeformat match
      case TimeFormat.TwentyFourHour =>
        s"${f"${date.hour}%02d"}:$minute:$second.$millis"
      case TimeFormat.TwelveHour =>
        val hour12   = { val h = date.hour % 12; if h == 0 then 12 else h }
        val meridiem = if date.hour < 12 then "AM" else "PM"
        s"${f"$hour12%02d"}:$minute:$second.$millis $meridiem"

    s"$dateStr $timeStr"
