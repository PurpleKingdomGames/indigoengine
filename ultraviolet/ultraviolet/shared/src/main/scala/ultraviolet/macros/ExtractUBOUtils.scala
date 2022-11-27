package ultraviolet.macros

import ultraviolet.datatypes.ShaderError
import ultraviolet.datatypes.UBODef
import ultraviolet.datatypes.UBOField

import scala.quoted.Quotes

@SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
class ExtractUBOUtils[Q <: Quotes](using val qq: Q):
  import qq.reflect.*

  def extractUBO(uboTerm: Term): UBODef =
    def extractTypeNames(terms: List[Term]): List[String] =
      terms.flatMap {
        case Inlined(
              _,
              Nil,
              Typed(Block(List(ValDef(_, _, Some(Ident(typeclassName)))), _), _)
            ) =>
          // e.g. "given_ShaderTypeOf_Float"
          val label = typeclassName.split("_").last match
            case "Int"   => "int"
            case "Float" => "float"
            case l       => l

          List(label)

        case _ =>
          Nil
      }

    def extractLabels(terms: List[Term]): List[String] =
      terms.flatMap {
        case Inlined(
              _,
              _,
              Typed(
                Block(
                  List(
                    ValDef(
                      _,
                      _,
                      Some(
                        TypeApply(
                          Select(
                            Select(
                              Apply(
                                _,
                                List(Literal(StringConstant(label)))
                              ),
                              _
                            ),
                            _
                          ),
                          _
                        )
                      )
                    )
                  ),
                  _
                ),
                _
              )
            ) =>
          List(label)

        case _ =>
          Nil
      }

    def extractPrecisions(terms: List[Term]): List[Option[String]] =
      terms.flatMap {
        case Inlined(
              _,
              _,
              Typed(
                Block(
                  List(
                    ValDef(
                      _,
                      _,
                      Some(
                        Inlined(
                          _,
                          _,
                          Typed(
                            Inlined(
                              _,
                              _,
                              Apply(
                                TypeApply(Select(Ident("Some"), "apply"), List(Inferred())),
                                List(Inlined(None, Nil, Literal(StringConstant(precision))))
                              )
                            ),
                            _
                          )
                        )
                      )
                    )
                  ),
                  _
                ),
                _
              )
            ) =>
          List(Option(precision))

        case Inlined(
              _,
              _,
              Typed(
                Block(
                  List(
                    ValDef(
                      _,
                      _,
                      Some(
                        Inlined(
                          _,
                          _,
                          Typed(
                            Inlined(_, _, Ident("None")),
                            _
                          )
                        )
                      )
                    )
                  ),
                  _
                ),
                _
              )
            ) =>
          List(None)

        case _ =>
          Nil
      }

    def findTermsOf(ident: String): Term => List[Term] =
      // Match
      case res @ Inlined(Some(TypeApply(Ident(id), _)), _, t) if id == ident =>
        List(res) ++ findTermsOf(ident)(t)

      case Apply(t, ts) =>
        (t :: ts).flatMap(findTermsOf(ident))

      case Block(List(DefDef(_, _, _, _)), _) =>
        Nil

      case Block(ss, t) =>
        findTermsOf(ident)(t)

      case Ident(_) =>
        Nil

      case Inlined(_, _, t) =>
        findTermsOf(ident)(t)

      case Select(t, _) =>
        findTermsOf(ident)(t)

      case Typed(t, _) =>
        findTermsOf(ident)(t)

      case TypeApply(t, ts) =>
        findTermsOf(ident)(t)

      case _ =>
        Nil

    def walkUBOTerm: Term => UBODef =
      // Things to skip over
      case Inlined(Some(Apply(TypeApply(Ident("ubo"), _), _)), _, term) =>
        walkUBOTerm(term)

      case Inlined(Some(Apply(TypeApply(Select(_, "readUBO"), _), _)), _, term) =>
        walkUBOTerm(term)

      // Hooks we care about
      case Apply(Select(Ident("UBODef"), "apply"), List(Literal(StringConstant(uboName)), term)) =>
        val pn = extractPrecisions(findTermsOf("summonPrecision")(term))
        val lb = extractLabels(findTermsOf("summonLabels")(term))
        val tn = extractTypeNames(findTermsOf("summonTypeName")(term))

        UBODef(
          uboName,
          pn.zip(lb.zip(tn)).map(p => UBOField(p._1, p._2._2, p._2._1))
        )

      // General traversal

      case Apply(term, _) =>
        walkUBOTerm(term)

      case Inlined(_, _, term) =>
        walkUBOTerm(term)

      case Typed(term, _) =>
        walkUBOTerm(term)

      case TypeApply(term, trees) =>
        walkUBOTerm(term)

      case Select(term, _) =>
        walkUBOTerm(term)

      case x =>
        throw ShaderError.UBORead("UBO Term match failed at: " + Printer.TreeStructure.show(x))

    walkUBOTerm(uboTerm)
