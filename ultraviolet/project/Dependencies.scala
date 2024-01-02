import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt.{Def, _}

object Dependencies {

  object Versions {
    val munit  = "0.7.29"
    val indigo = "0.15.2"
  }

  object Shared {
    val munit        = Def.setting(Seq("org.scalameta" %%% "munit" % Versions.munit % Test))
    val indigo       = Def.setting(Seq("io.indigoengine" %%% "indigo" % Versions.indigo))
    val indigoExtras = Def.setting(Seq("io.indigoengine" %%% "indigo-extras" % Versions.indigo))
    val indigoJson   = Def.setting(Seq("io.indigoengine" %%% "indigo-json-circe" % Versions.indigo))
  }

}
