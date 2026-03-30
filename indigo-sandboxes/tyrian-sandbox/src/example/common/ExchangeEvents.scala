package example.common

import indigo.aliases.GlobalEvent
import indigoengine.shared.aliases.PartialIso
import tyrian.GlobalMsg

object ExchangeEvents:

  lazy val mapping: PartialIso[GlobalMsg, GlobalEvent] =
    PartialIso(to, from)

  lazy val to: GlobalMsg => Option[GlobalEvent] =
    case ExchangeMsgs.IndigoToLog(id, msg) =>
      Some(ExchangeEvents.IndigoToLog(id, msg))

    case _ =>
      None

  lazy val from: GlobalEvent => Option[GlobalMsg] =
    case ExchangeEvents.TyrianToLog(msg) =>
      Some(ExchangeMsgs.TyrianToLog(msg))

    case _ =>
      None

enum ExchangeEvents extends GlobalEvent:
  case IndigoToLog(id: String, msg: String)
  case TyrianToLog(msg: String)

enum ExchangeMsgs extends GlobalMsg:
  case IndigoToLog(id: String, msg: String)
  case TyrianToLog(msg: String)
