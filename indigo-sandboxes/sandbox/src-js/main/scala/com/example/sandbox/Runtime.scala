package com.example.sandbox

import cats.effect.IO
import indigo.*
import tyrian.*
import tyrian.classic.cmds.LocalStorage

import scala.scalajs.js.annotation.*

@JSExportTopLevel("IndigoGame")
object Runtime extends BasicGameRuntime[Unit]:

  def game: Game[?, ?, ?] =
    SandboxGame()

  def settings: Settings =
    Settings.default
      .withFrameRatePolicy(FrameRatePolicy.Skip(FPS.`60`))

  def eventMapping: PartialIso[GlobalMsg, GlobalEvent] =
    val to: GlobalMsg => Option[GlobalEvent] =
      case StorageMsgs.Loaded(k, v) =>
        Some(StorageEvents.Loaded(k, v))

      case _ =>
        None

    val from: GlobalEvent => Option[GlobalMsg] =
      case StorageEvents.Store(k, v) =>
        Some(StorageMsgs.Store(k, v))

      case StorageEvents.Load(k) =>
        Some(StorageMsgs.Load(k))

      case _ =>
        None

    PartialIso(to, from)

  def init(flags: Map[String, String]): Result[Unit] =
    Result(())

  def update(model: Unit): GlobalMsg => Result[Unit] =
    case StorageMsgs.Store(k, v) =>
      Result(())
        .addCmds(
          LocalStorage.setItem[IO, GlobalMsg](k, v, _ => LogThis("Saved!"))
        )

    case StorageMsgs.Load(k) =>
      val toMsg: Either[LocalStorage.Result.NotFound, LocalStorage.Result.Found] => GlobalMsg =
        case Left(_) =>
          LogThis(s"Not Found! Tried to load key '$k' from local storage.")

        case Right(LocalStorage.Result.Found(data)) =>
          StorageMsgs.Loaded(k, data)

      Result(())
        .addCmds(
          LocalStorage.getItem[IO, GlobalMsg](k, toMsg)
        )

    case LogThis(msg) =>
      Result(())
        .log(msg)

    case _ =>
      Result(())

enum StorageMsgs extends GlobalMsg:
  case Store(key: String, value: String)
  case Load(key: String)
  case Loaded(key: String, value: String)

final case class LogThis(msg: String) extends GlobalMsg
