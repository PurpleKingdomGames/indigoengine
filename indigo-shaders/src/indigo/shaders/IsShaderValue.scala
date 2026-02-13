package indigo.shaders

import indigo.core.datatypes.Matrix4
import indigo.core.datatypes.mutable.CheapMatrix4
import indigoengine.shared.collections.Batch
import indigoengine.shared.datatypes.RGB
import indigoengine.shared.datatypes.RGBA

sealed trait IsShaderValue[T]:
  def giveLength: Int
  def toBatch: T => Batch[Float]

object IsShaderValue:
  def create[T](length: Int, valueToBatch: T => Batch[Float]): IsShaderValue[T] =
    new IsShaderValue[T]:
      def giveLength: Int            = length
      def toBatch: T => Batch[Float] = t => valueToBatch(t)

  given IsShaderValue[Float] =
    create(ShaderPrimitive.float.length, f => Batch(f))
  given IsShaderValue[RGB] =
    create(ShaderPrimitive.vec3.length, rgb => ShaderPrimitive.vec3.fromRGB(rgb).toBatch)
  given IsShaderValue[RGBA] =
    create(ShaderPrimitive.vec4.length, rgba => ShaderPrimitive.vec4.fromRGBA(rgba).toBatch)
  given IsShaderValue[Matrix4] =
    create(ShaderPrimitive.vec4.length, mat => ShaderPrimitive.mat4.fromMatrix4(mat).toBatch)
  given IsShaderValue[CheapMatrix4] =
    create(ShaderPrimitive.vec4.length, mat => ShaderPrimitive.mat4.fromCheapMatrix4(mat).toBatch)
