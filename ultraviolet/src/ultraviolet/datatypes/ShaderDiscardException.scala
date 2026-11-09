package ultraviolet.datatypes

import scala.util.control.NoStackTrace

/** Thrown by `discard()` so that the call has type `Nothing`. It is never expected to be seen at runtime but can be
  * caught during scala unit testing. the shader macro translates the throw into the GLSL `discard` statement.
  */
final class ShaderDiscardException
    extends Exception("[ultraviolet] A shader terminated early by calling 'discard'.")
    with NoStackTrace
