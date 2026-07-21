package indigoengine.sdl.facades.sdl

import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

@extern
object SDL:

  def SDL_Init(flags: UInt): Boolean = extern
  def SDL_Quit(): Unit               = extern

  type SDL_Window = Ptr[Byte]

  def SDL_CreateWindow(
      title: CString,
      w: CInt,
      h: CInt,
      flags: UInt
  ): SDL_Window = extern

  def SDL_DestroyWindow(window: SDL_Window): Unit  = extern
  def SDL_RaiseWindow(window: SDL_Window): Boolean = extern

  def SDL_SetHint(name: CString, value: CString): Boolean = extern

  type SDL_GLContext = Ptr[Byte]

  def SDL_GL_CreateContext(window: SDL_Window): SDL_GLContext              = extern
  def SDL_GL_DestroyContext(context: SDL_GLContext): Boolean               = extern
  def SDL_GL_SwapWindow(window: SDL_Window): Boolean                       = extern
  def SDL_GL_SetAttribute(attr: CInt, value: CInt): CInt                   = extern
  def SDL_GL_MakeCurrent(window: SDL_Window, context: SDL_GLContext): CInt = extern

  // SDL_Event is a union, largest member is 128 bytes in SDL3.
  type SDL_Event = CArray[Byte, Nat.Digit3[Nat._1, Nat._2, Nat._8]]

  def SDL_PollEvent(event: Ptr[SDL_Event]): CInt = extern

  def SDL_Delay(ms: UInt): Unit = extern
  def SDL_GetError(): CString   = extern

  // SDL_Locale { const char *language; const char *country; }
  type SDL_Locale = CStruct2[CString, CString]

  def SDL_GetPreferredLocales(count: Ptr[CInt]): Ptr[Ptr[SDL_Locale]] = extern
  def SDL_free(mem: Ptr[Byte]): Unit                                  = extern
