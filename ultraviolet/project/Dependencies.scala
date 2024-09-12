import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt.{Def, _}

object Dependencies {

  object Versions {
    val munit = "0.7.29"
  }

  object Shared {
    val munit = Def.setting(Seq("org.scalameta" %%% "munit" % Versions.munit % Test))
  }

}
