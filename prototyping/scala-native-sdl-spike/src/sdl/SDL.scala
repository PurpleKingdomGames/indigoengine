package sdl

import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

@extern
object SDL:
  // --- Init / Quit ---
  def SDL_Init(flags: UInt): Boolean = extern
  def SDL_Quit(): Unit = extern

  // --- Window ---
  type SDL_Window = Ptr[Byte]

  def SDL_CreateWindow(
      title: CString,
      w: CInt,
      h: CInt,
      flags: UInt
  ): SDL_Window = extern

  def SDL_DestroyWindow(window: SDL_Window): Unit = extern

  // --- GL context ---
  type SDL_GLContext = Ptr[Byte]

  def SDL_GL_CreateContext(window: SDL_Window): SDL_GLContext = extern
  def SDL_GL_DestroyContext(context: SDL_GLContext): Boolean = extern
  def SDL_GL_SwapWindow(window: SDL_Window): Boolean = extern
  def SDL_GL_SetAttribute(attr: CInt, value: CInt): CInt = extern

  // --- Events ---
  // SDL_Event is a union, largest member is 128 bytes in SDL3. We treat it as opaque.
  type SDL_Event = CArray[Byte, Nat.Digit3[Nat._1, Nat._2, Nat._8]]

  def SDL_PollEvent(event: Ptr[SDL_Event]): CInt = extern

  // --- Misc ---
  def SDL_Delay(ms: UInt): Unit = extern
  def SDL_GetError(): CString = extern

object SDLConstants:
  // SDL_Init flags
  val SDL_INIT_VIDEO: UInt = 0x00000020.toUInt

  // SDL_CreateWindow flags
  val SDL_WINDOW_OPENGL: UInt = 0x00000002.toUInt

  // SDL_GL_SetAttribute enums
  val SDL_GL_DOUBLEBUFFER: CInt = 5
  val SDL_GL_CONTEXT_MAJOR_VERSION: CInt = 17
  val SDL_GL_CONTEXT_MINOR_VERSION: CInt = 18

  // SDL_EventType — we only care about SDL_EVENT_QUIT
  val SDL_EVENT_QUIT: UInt = 0x100.toUInt
