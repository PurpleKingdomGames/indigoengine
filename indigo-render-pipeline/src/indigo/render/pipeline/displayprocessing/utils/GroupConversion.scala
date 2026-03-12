package indigo.render.pipeline.displayprocessing.utils

import indigo.core.datatypes.mutable.CheapMatrix4
import indigo.scenegraph.Group
import indigoengine.shared.datatypes.Radians

object GroupConversion:

  def groupToMatrix(group: Group): CheapMatrix4 =
    CheapMatrix4.identity
      .scale(
        if (group.flip.horizontal) -1.0 else 1.0,
        if (group.flip.vertical) -1.0 else 1.0,
        1.0f
      )
      .translate(
        -group.ref.x.toFloat,
        -group.ref.y.toFloat,
        0.0f
      )
      .scale(group.scale.x.toFloat, group.scale.y.toFloat, 1.0f)
      .rotate(group.rotation.toFloat)
      .translate(
        group.position.x.toFloat,
        group.position.y.toFloat,
        0.0f
      )
