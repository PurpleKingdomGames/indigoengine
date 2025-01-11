import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt.{Def, _}

object Dependencies {

  object Versions {
    val munit = "1.0.4"
  }

  object Shared {
    val munit = Def.setting(Seq("org.scalameta" %%% "munit" % Versions.munit % Test))
  }

}
