package indigoplugin.generators

class AssetListingTests extends munit.FunSuite {

  val projectDir  = "/home/some-user/some-project"
  val projectPath = os.Path(projectDir)

  test("should be able to convert file and folder names into something safe (using default)") {
    def toSafeName(name: String) = AssetListing.toDefaultSafeName((n, _) => n)(name, "")

    assertEquals(toSafeName("hello"), "hello")
    assertEquals(toSafeName("hello-there-01"), "helloThere01")
    assertEquals(toSafeName("hello-there-01.jpg"), "helloThere01Jpg")
    assertEquals(toSafeName("^hello!there_0 1.jpg"), "helloThere01Jpg")
    assertEquals(toSafeName("00-hello"), "_00Hello")
  }

  test("It should be able to render a simple tree of assets") {

    val paths: List[os.RelPath] =
      List(
        os.RelPath.rel / "assets" / "some_text.txt",
        os.RelPath.rel / "assets" / "images" / "fancy logo!.svg"
      )

    val actual =
      AssetListing.renderContent(projectPath, paths, AssetListing.toDefaultSafeName((n, _) => n))

    val expected =
      s"""
  object assets:
    object images:
      val fancyLogo: AssetName               = AssetName("fancy logo!.svg")
      val fancyLogoMaterial: Material.Bitmap = Material.Bitmap(fancyLogo)

      def assetSetRelativeTo(basePath: String): Set[AssetType] =
        Set(
          AssetType.Image(fancyLogo, AssetPath(basePath + "assets/images/fancy logo!.svg"), Option(AssetTag("images")))
        )
      def assetSetRelative: Set[AssetType] = assetSetRelativeTo("./")
      def assetSetAbsolute: Set[AssetType] = assetSetRelativeTo("$projectDir")

      def assetNameSet: Set[AssetName] =
        Set(
          fancyLogo
        )

    val someText: AssetName = AssetName("some_text.txt")

    def assetSetRelativeTo(basePath: String): Set[AssetType] =
      Set(
        AssetType.Text(someText, AssetPath(basePath + "assets/some_text.txt"))
      )
    def assetSetRelative: Set[AssetType] = assetSetRelativeTo("./")
    def assetSetAbsolute: Set[AssetType] = assetSetRelativeTo("$projectDir")

    def assetNameSet: Set[AssetName] =
      Set(
        someText
      )

      """.trim

    assertNoDiff(actual.trim, expected.trim)

  }

  test("It should be able to render a tree of asset details") {

    val paths: List[os.RelPath] =
      List(
        os.RelPath.rel / "assets" / "folderA" / "folderB" / "d.jpg",
        os.RelPath.rel / "assets" / "folderC" / "f.mp3",
        os.RelPath.rel / "assets" / "folderA" / "folderB" / "b.png",
        os.RelPath.rel / "assets" / "a.txt",
        os.RelPath.rel / "assets" / "folderA" / "folderB" / "c.png",
        os.RelPath.rel / "assets" / "folderA" / "e.svg"
      )

    val rename: (String, String) => String = {
      case ("e", "svg") => "ee"
      case (n, _)       => n
    }

    val actual =
      AssetListing.renderContent(projectPath, paths, AssetListing.toDefaultSafeName(rename))

    val expected =
      s"""
  object assets:
    object folderA:
      object folderB:
        val b: AssetName               = AssetName("b.png")
        val bMaterial: Material.Bitmap = Material.Bitmap(b)
        val c: AssetName               = AssetName("c.png")
        val cMaterial: Material.Bitmap = Material.Bitmap(c)
        val d: AssetName               = AssetName("d.jpg")
        val dMaterial: Material.Bitmap = Material.Bitmap(d)

        def assetSetRelativeTo(basePath: String): Set[AssetType] =
          Set(
            AssetType.Image(b, AssetPath(basePath + "assets/folderA/folderB/b.png"), Option(AssetTag("folderB"))),
            AssetType.Image(c, AssetPath(basePath + "assets/folderA/folderB/c.png"), Option(AssetTag("folderB"))),
            AssetType.Image(d, AssetPath(basePath + "assets/folderA/folderB/d.jpg"), Option(AssetTag("folderB")))
          )
        def assetSetRelative: Set[AssetType] = assetSetRelativeTo("./")
        def assetSetAbsolute: Set[AssetType] = assetSetRelativeTo("$projectDir")

        def assetNameSet: Set[AssetName] =
          Set(
            b,
            c,
            d
          )

      val ee: AssetName               = AssetName("e.svg")
      val eeMaterial: Material.Bitmap = Material.Bitmap(ee)

      def assetSetRelativeTo(basePath: String): Set[AssetType] =
        Set(
          AssetType.Image(ee, AssetPath(basePath + "assets/folderA/e.svg"), Option(AssetTag("folderA")))
        )
      def assetSetRelative: Set[AssetType] = assetSetRelativeTo("./")
      def assetSetAbsolute: Set[AssetType] = assetSetRelativeTo("$projectDir")

      def assetNameSet: Set[AssetName] =
        Set(
          ee
        )

    object folderC:
      val f: AssetName            = AssetName("f.mp3")
      val fPlay: PlaySound        = PlaySound(f, Volume.Max)
      val fSceneAudio: SceneAudio = SceneAudio(SceneAudioSource(BindingKey("f.mp3"), PlaybackPattern.SingleTrackLoop(Track(f))))

      def assetSetRelativeTo(basePath: String): Set[AssetType] =
        Set(
          AssetType.Audio(f, AssetPath(basePath + "assets/folderC/f.mp3"))
        )
      def assetSetRelative: Set[AssetType] = assetSetRelativeTo("./")
      def assetSetAbsolute: Set[AssetType] = assetSetRelativeTo("$projectDir")

      def assetNameSet: Set[AssetName] =
        Set(
          f
        )

    val a: AssetName = AssetName("a.txt")

    def assetSetRelativeTo(basePath: String): Set[AssetType] =
      Set(
        AssetType.Text(a, AssetPath(basePath + "assets/a.txt"))
      )
    def assetSetRelative: Set[AssetType] = assetSetRelativeTo("./")
    def assetSetAbsolute: Set[AssetType] = assetSetRelativeTo("$projectDir")

    def assetNameSet: Set[AssetName] =
      Set(
        a
      )
      """.trim

    assertNoDiff(actual.trim, expected.trim)

  }

}
