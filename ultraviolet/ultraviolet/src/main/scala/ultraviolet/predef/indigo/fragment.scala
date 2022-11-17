package ultraviolet.predef.indigo

import ultraviolet.syntax.*

@SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
object fragment:

  // Variables
  def UV: vec2                       = null // Unscaled texture coordinates
  def SIZE: vec2                     = null // Width / height of the objects
  def CHANNEL_0: vec4                = null // Pixel value from texture channel 0
  def CHANNEL_1: vec4                = null // Pixel value from texture channel 1
  def CHANNEL_2: vec4                = null // Pixel value from texture channel 2
  def CHANNEL_3: vec4                = null // Pixel value from texture channel 3
  def CHANNEL_0_TEXTURE_COORDS: vec2 = null // Scaled texture coordinates
  def CHANNEL_1_TEXTURE_COORDS: vec2 = null // Scaled texture coordinates
  def CHANNEL_2_TEXTURE_COORDS: vec2 = null // Scaled texture coordinates
  def CHANNEL_3_TEXTURE_COORDS: vec2 = null // Scaled texture coordinates
  def CHANNEL_0_POSITION: vec2       = null // top left position of this texture on the atlas in UV coords
  def CHANNEL_1_POSITION: vec2       = null // top left position of this texture on the atlas in UV coords
  def CHANNEL_2_POSITION: vec2       = null // top left position of this texture on the atlas in UV coords
  def CHANNEL_3_POSITION: vec2       = null // top left position of this texture on the atlas in UV coords
  def CHANNEL_0_SIZE: vec2           = null // size of this texture on the atlas in UV coords
  def SCREEN_COORDS: vec2            = null
  def ROTATION: Float                = 0.0f
  def TEXTURE_SIZE: vec2             = null // Size of the texture in pixels
  def ATLAS_SIZE: vec2               = null // Size of the atlas this texture is on, in pixels
  def INSTANCE_ID: Int               = 0    // The current instance id

  // Light information
  def LIGHT_INDEX: Int        = 0
  def LIGHT_COUNT: Int        = 0
  def LIGHT_ACTIVE: Int       = 0
  def LIGHT_TYPE: Int         = 0
  def LIGHT_FAR_CUT_OFF: Int  = 0
  def LIGHT_FALLOFF_TYPE: Int = 0
  def LIGHT_COLOR: vec4       = null
  def LIGHT_SPECULAR: vec4    = null
  def LIGHT_POSITION: vec2    = null
  def LIGHT_ROTATION: Float   = 0.0f
  def LIGHT_NEAR: Float       = 0.0f
  def LIGHT_FAR: Float        = 0.0f
  def LIGHT_ANGLE: Float      = 0.0f
  def LIGHT_INTENSITY: Float  = 0.0f

  // Constants
  def PI: Float    = Math.PI.toFloat
  def PI_2: Float  = PI / 2
  def PI_4: Float  = PI / 4
  def TAU: Float   = 2 * PI
  def TAU_2: Float = TAU / 2
  def TAU_4: Float = TAU / 4
  def TAU_8: Float = TAU / 8

  // Outputs
  var COLOR: vec4 = null
