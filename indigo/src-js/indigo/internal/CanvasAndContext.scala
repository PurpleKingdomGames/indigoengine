package indigo.internal

import indigo.render.facades.WebGL2RenderingContext
import indigo.core.utils.IndigoLogger
import org.scalajs.dom
import org.scalajs.dom.Element
import org.scalajs.dom.html

import scala.annotation.nowarn
import scala.scalajs.js.Dynamic

object CanvasAndContext:

  @SuppressWarnings(
    Array(
      "scalafix:DisableSyntax.null",
      "scalafix:DisableSyntax.var"
    )
  )
  @nowarn("msg=unused")
  def setupCanvas(width: Int, height: Int, parentElement: Element): html.Canvas =
    val defaultName = "indigo-container"

    val parentElementId =
      Option(parentElement.id)
        .map(id => if id.isEmpty then defaultName else id)
        .getOrElse(defaultName)

    val name = s"$parentElementId-canvas"

    var canvas: html.Canvas =
      dom.document.getElementById(name).asInstanceOf[html.Canvas]

    if canvas == null then
      canvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]

      parentElement.appendChild(canvas)

      canvas.id = name
      canvas.width = width
      canvas.height = height

    canvas

  @SuppressWarnings(
    Array(
      "scalafix:DisableSyntax.null",
      "scalafix:DisableSyntax.var"
    )
  )
  @nowarn("msg=unused")
  def createNamedCanvas(width: Int, height: Int, name: String): html.Canvas =
    var canvas: html.Canvas =
      dom.document.getElementById(name).asInstanceOf[html.Canvas]

    if canvas == null then
      canvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]
      canvas.id = name
      canvas.width = width
      canvas.height = height

    canvas

  def setupContext(canvas: html.Canvas): WebGL2RenderingContext =
    // TODO: These should come from somewhere.
    val antiAliasing: Boolean          = false
    val premultipliedAlpha: Boolean    = true
    val transparentBackground: Boolean = true

    val args =
      Dynamic.literal(
        "premultipliedAlpha" -> premultipliedAlpha,
        "alpha"              -> transparentBackground,
        "antialias"          -> antiAliasing
      )

    ensureRenderingTechnologyAvailable(args)

    canvas.getContext("webgl2", args).asInstanceOf[WebGL2RenderingContext]

  @SuppressWarnings(
    Array(
      "scalafix:DisableSyntax.null",
      "scalafix:DisableSyntax.throw"
    )
  )
  private def ensureRenderingTechnologyAvailable(
      args: Dynamic
  ): Unit =
    /* This test relies on a temporary canvas not attached to the document.
     */
    val tempCanvas = createNamedCanvas(1, 1, "indigowebgl2test")

    val gl2 = tempCanvas.getContext("webgl2", args).asInstanceOf[WebGL2RenderingContext]

    if (gl2 == null)
      throw new Exception("WebGL 2.0 required by indigo game. This browser does not appear to support WebGL 2.0.")
    else if (!isWebGL2ReallySupported(gl2))
      throw new Exception(
        "WebGL 2.0 required by indigo game. This browser claims to support WebGL 2.0, but does not meet indigo's requirements."
      )
    else {
      IndigoLogger.info("Using WebGL 2.0")
      ()
    }

  @SuppressWarnings(
    Array(
      "scalafix:DisableSyntax.null"
    )
  )

  private def isWebGL2ReallySupported(gl2: WebGL2RenderingContext): Boolean = {
    IndigoLogger.info("Checking WebGL 2.0 availability...")

    def testWebGL2Compatibility(param: Int, min: Int, name: String): Boolean =
      try {
        val value = gl2.getParameter(param).asInstanceOf[Int]
        if (!value.toFloat.isNaN() && value >= min) true
        else {
          IndigoLogger.info(
            s" - WebGL 2.0 check '$name' failed. [min: ${min.toString}] [actual: ${value.toFloat.toString}]"
          )
          false
        }
      } catch {
        case _: Throwable => false
      }

    val tests = List(
      (WebGL2RenderingContext.MAX_3D_TEXTURE_SIZE, 256, "MAX_3D_TEXTURE_SIZE"),
      (WebGL2RenderingContext.MAX_DRAW_BUFFERS, 4, "MAX_DRAW_BUFFERS"),
      (WebGL2RenderingContext.MAX_COLOR_ATTACHMENTS, 4, "MAX_COLOR_ATTACHMENTS"),
      (WebGL2RenderingContext.MAX_VERTEX_UNIFORM_BLOCKS, 12, "MAX_VERTEX_UNIFORM_BLOCKS"),
      (WebGL2RenderingContext.MAX_VERTEX_TEXTURE_IMAGE_UNITS, 16, "MAX_VERTEX_TEXTURE_IMAGE_UNITS"),
      (WebGL2RenderingContext.MAX_FRAGMENT_INPUT_COMPONENTS, 60, "MAX_FRAGMENT_INPUT_COMPONENTS"),
      (WebGL2RenderingContext.MAX_UNIFORM_BUFFER_BINDINGS, 24, "MAX_UNIFORM_BUFFER_BINDINGS"),
      (WebGL2RenderingContext.MAX_COMBINED_UNIFORM_BLOCKS, 24, "MAX_COMBINED_UNIFORM_BLOCKS"),
      (WebGL2RenderingContext.MAX_VARYING_VECTORS, 15, "MAX_VARYING_VECTORS")
    )

    gl2 != null && tests.forall(t => testWebGL2Compatibility(t._1, t._2, t._3))
  }
