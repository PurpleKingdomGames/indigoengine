package ultraviolet.predef

import ultraviolet.syntax.*

@SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
object shadertoy:

  def iResolution: vec3 = null // viewport resolution (in pixels)
  def iTime: Float      = 0.0  // shader playback time (in seconds)
  def iTimeDelta: Float = 0.0  // render time (in seconds)
  def iFrameRate: Float = 0.0  // shader frame rate
  def iFrame: Int       = 0    // shader playback frame
  // def iChannelTime[4]: Float // channel playback time (in seconds)
  // def iChannelResolution[4]: vec3 // channel resolution (in pixels)
  def iMouse: vec4 = null // mouse pixel coords. xy: current (if MLB down) = null zw: click
  // def iChannel0..3: samplerXX // input channel. XX = 2D/Cube
  def iDate: vec4        = null // (year = null month = null day = null time in seconds)
  def iSampleRate: Float = 0.0  // sound sample rate (i.e. = null 44100)
  def fragCoord: vec2    = null // UV coordinates // Unoffical, from the main function definition
  var fragColor: vec4    = null // output variable // Unoffical, from the main function definition
