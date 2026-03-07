// package ultraviolet.acceptance

// // import ultraviolet.DebugAST
// import ultraviolet.datatypes.ShaderResult
// import ultraviolet.syntax.*

// import scala.annotation.nowarn

// // TODO: Rethink or remove. Look at the old ShaderPrinter[ShaderToy] instance.

// @nowarn("msg=unused")
// class GLSLValidationTests extends munit.FunSuite {

//   test("GLSL printer validation error are represented") {
//     inline def fragment =
//       Shader {
//         val a = 1
//       }

//     // given p: ShaderPrinter[WebGL2] = new ShaderPrinter[WebGL2] {

//     //   def isValid(
//     //       inType: Option[String],
//     //       outType: Option[String],
//     //       functions: List[ShaderAST],
//     //       body: ShaderAST
//     //   ): ShaderValid = ShaderValid.Invalid(List("boom"))

//     //   def transformer: PartialFunction[ShaderAST, ShaderAST] = { case x =>
//     //     x
//     //   }

//     //   // def printer: PartialFunction[ShaderAST, List[String]] = { case x =>
//     //   //   Nil
//     //   // }
//     // }

//     val actual = fragment.toGLSL300

//     // DebugAST.toAST(fragment)
//     // println(actual)

//     assertEquals(
//       actual,
//       ShaderResult.Error("[ultraviolet] Shader failed to validate because: [boom]")
//     )
//   }

// }
