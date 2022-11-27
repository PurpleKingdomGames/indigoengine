package ultraviolet.indigoexamples

import ultraviolet.predef.indigo.*
import ultraviolet.syntax.*

object NoOp:

  object vertex:
    inline def shader =
      Shader {
        def vertex: Unit = {}
      }

    val output = shader.toGLSL[Indigo]

    val expected: String =
      """
      |void vertex(){
      |  
      |}
      |""".stripMargin.trim

  object fragment:
    inline def shader =
      Shader {
        def fragment: Unit  = {}
        def prepare: Unit   = {}
        def light: Unit     = {}
        def composite: Unit = {}
      }

    val output = shader.toGLSL[Indigo]

    val expected: String =
      """
      |void fragment(){
      |  
      |}
      |void prepare(){
      |  
      |}
      |void light(){
      |  
      |}
      |void composite(){
      |  
      |}
      |""".stripMargin.trim
