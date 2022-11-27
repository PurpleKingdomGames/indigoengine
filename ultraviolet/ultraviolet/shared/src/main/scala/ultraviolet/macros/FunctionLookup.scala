package ultraviolet.macros

import ultraviolet.datatypes.ShaderAST

final case class FunctionLookup(fn: ShaderAST.Function, userDefined: Boolean)
