package tyrian.runtime

import tyrian.SDLMsg

import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters.*

final class SDLEventListeners():

  private val listeners = new ConcurrentHashMap[SDLEventListenerHandle, SDLEventListeners.ListenerEntry]()

  def addSDLEventListener(handle: SDLEventListenerHandle)(callback: SDLMsg => Unit): SDLEventListenerHandle =
    val entry = SDLEventListeners.ListenerEntry(callback)
    val _     = listeners.put(handle, entry)
    handle

  def removeSDLEventListener(handle: SDLEventListenerHandle): Unit =
    val _ = listeners.remove(handle)

  def dispatch(msg: SDLMsg): Unit =
    val entries = listeners.values().iterator().asScala.toList
    entries.foreach: entry =>
      entry.callback(msg)

object SDLEventListeners:
  final case class ListenerEntry(callback: SDLMsg => Unit)
