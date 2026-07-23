package indigo.shared

import indigo.core.datatypes.FontKey
import indigo.core.datatypes.Rectangle
import indigo.core.dice.Dice
import indigo.core.events.InputState
import indigo.core.locale.Locale as CoreLocale
import indigo.core.time.DateTime as CoreDateTime
import indigo.core.time.GameTime
import indigo.platform.locale.LocaleService
import indigo.scenegraph.SceneNode
import indigo.scenegraph.TextLine
import indigo.scenegraph.registers.BoundaryLocator
import indigoengine.shared.collections.Batch
import indigo.platform.time.DateTimeService
import indigo.core.time.DateFormat
import indigo.core.time.TimeFormat

/** The Context is the context in which the current frame will be processed.
  *
  * This is divided into two areas:
  *
  *   1. Frame: The data that is specific to the current frame, such as the current time, input state, and dice (pseudo
  *      random number generated seeded on the game's running time at the beginning of the frame), and if only frame
  *      values are used, then calls to functions like `updateModel` can be considered referentially transparent.
  *   2. Services: The services that are available to the game, such as measuring text, finding the bounds of on-screen
  *      elements, or accessing a long running Random instance. Services are side-effecting, long running, and / or
  *      stateful.
  */
final class Context(
    val frame: Context.Frame,
    val services: Context.Services
):

  def withFrame(newFrame: Context.Frame): Context =
    new Context(newFrame, services)
  def modifyFrame(modify: Context.Frame => Context.Frame): Context =
    withFrame(modify(frame))

  def withServices(newServices: Context.Services): Context =
    new Context(frame, newServices)
  def modifyServices(modify: Context.Services => Context.Services): Context =
    withServices(modify(services))

object Context:

  def initial: Context =
    new Context(Frame.initial, Services.noop)

  def apply(frame: Frame): Context =
    new Context(frame, Services.noop)

  def apply(services: Services): Context =
    new Context(Frame.initial, services)

  def apply(frame: Frame, services: Services): Context =
    new Context(frame, services)

  def apply(
      gameTime: GameTime,
      dice: Dice,
      inputState: InputState,
      viewport: Rectangle,
      boundaryLocator: BoundaryLocator,
      localeService: LocaleService,
      dateTimeService: DateTimeService
  ): Context =
    new Context(
      Frame(
        dice,
        gameTime,
        inputState,
        viewport
      ),
      Services(
        boundaryLocator,
        scala.util.Random(Dice.DefaultSeed),
        localeService,
        dateTimeService
      )
    )

  /** The data that is specific to the current frame, such as the current time, input state, and dice.
    */
  final class Frame(
      val dice: Dice,
      val time: GameTime,
      val input: InputState,
      val viewport: Rectangle
  ):

    def withDice(newDice: Dice): Frame =
      new Frame(newDice, time, input, viewport)

    def withTime(newTime: GameTime): Frame =
      new Frame(dice, newTime, input, viewport)

    def withInput(newInput: InputState): Frame =
      new Frame(dice, time, newInput, viewport)

    def withViewport(newViewport: Rectangle): Frame =
      new Frame(dice, time, input, newViewport)

  object Frame:
    def apply(dice: Dice, time: GameTime, input: InputState, viewport: Rectangle): Frame =
      new Frame(dice, time, input, viewport)

    val initial: Frame =
      new Frame(Dice.default, GameTime.zero, InputState.default, Rectangle.zero)

  /** The services that are available to the game, such as measuring text, finding the bounds of anything on screen, or
    * accessing a long running Random instance. Services are side-effecting, long running, and / or stateful.
    */
  trait Services:
    def bounds: Services.Bounds
    def random: Services.Random

    /** Accesses locales on the user's system
      *
      * @return
      */
    def locale: Services.Locale

    /** Accesses the systems current date and time
      *
      * @return
      */
    def datetime: Services.DateTime

  object Services:

    def apply(
        boundaryLocator: BoundaryLocator,
        _random: scala.util.Random,
        localeService: LocaleService,
        dateTimeService: DateTimeService
    ): Services =
      new Services:
        def bounds: Services.Bounds     = Bounds(boundaryLocator)
        def random: Services.Random     = Random(_random)
        def locale: Services.Locale     = Services.Locale(localeService)
        def datetime: Services.DateTime = Services.DateTime(dateTimeService)

    def noop: Services =
      new Services:
        def bounds: Bounds              = Bounds.noop
        def random: Random              = Random.noop
        def locale: Services.Locale     = Services.Locale.noop
        def datetime: Services.DateTime = Services.DateTime.noop

    trait Bounds:
      /** Safely finds the bounds of any given scene node, if the node has bounds. It is not possible to sensibly
        * measure the bounds of some node types, such as clones, and some nodes are dependant on external data that may
        * be missing.
        */
      def find(sceneNode: SceneNode): Option[Rectangle]

      /** Finds the bounds or returns a `Rectangle` of size zero for convenience.
        */
      def get(sceneNode: SceneNode): Rectangle

      def textAsLinesWithBounds(text: String, fontKey: FontKey, letterSpacing: Int, lineHeight: Int): Batch[TextLine]

    object Bounds:
      def apply(boundaryLocator: BoundaryLocator): Bounds =
        new Bounds:
          def find(sceneNode: SceneNode): Option[Rectangle] = boundaryLocator.findBounds(sceneNode)
          def get(sceneNode: SceneNode): Rectangle          = boundaryLocator.bounds(sceneNode)
          def textAsLinesWithBounds(
              text: String,
              fontKey: FontKey,
              letterSpacing: Int,
              lineHeight: Int
          ): Batch[TextLine] =
            boundaryLocator.textAsLinesWithBounds(text, fontKey, letterSpacing, lineHeight)

      def noop: Bounds =
        new Bounds:
          def find(sceneNode: SceneNode): Option[Rectangle] = None
          def get(sceneNode: SceneNode): Rectangle          = Rectangle.zero
          def textAsLinesWithBounds(
              text: String,
              fontKey: FontKey,
              letterSpacing: Int,
              lineHeight: Int
          ): Batch[TextLine] =
            Batch.empty

    trait Random:
      def nextBoolean: Boolean
      def nextDouble: Double
      def between(minInclusive: Double, maxExclusive: Double): Double
      def nextFloat: Float
      def between(minInclusive: Float, maxExclusive: Float): Float
      def nextInt: Int
      def nextInt(n: Int): Int
      def between(minInclusive: Int, maxExclusive: Int): Int
      def nextLong: Long
      def nextLong(n: Long): Long
      def between(minInclusive: Long, maxExclusive: Long): Long
      def nextString(length: Int): String
      def nextPrintableChar(): Char
      def setSeed(seed: Long): Unit
      def shuffle[A](xs: List[A]): List[A]
      def shuffle[A](xs: Batch[A]): Batch[A]
      def alphanumeric(take: Int): List[Char]

    object Random:

      def apply(_random: scala.util.Random): Random =
        new Random:
          def nextBoolean: Boolean                                        = _random.nextBoolean()
          def nextDouble: Double                                          = _random.nextDouble()
          def between(minInclusive: Double, maxExclusive: Double): Double = _random.between(minInclusive, maxExclusive)
          def nextFloat: Float                                            = _random.nextFloat()
          def between(minInclusive: Float, maxExclusive: Float): Float    = _random.between(minInclusive, maxExclusive)
          def nextInt: Int                                                = _random.nextInt()
          def nextInt(n: Int): Int                                        = _random.nextInt(n)
          def between(minInclusive: Int, maxExclusive: Int): Int          = _random.between(minInclusive, maxExclusive)
          def nextLong: Long                                              = _random.nextLong()
          def nextLong(n: Long): Long                                     = _random.nextLong(n)
          def between(minInclusive: Long, maxExclusive: Long): Long       = _random.between(minInclusive, maxExclusive)
          def nextString(length: Int): String                             = _random.nextString(length)
          def nextPrintableChar(): Char                                   = _random.nextPrintableChar()
          def setSeed(seed: Long): Unit                                   = _random.setSeed(seed)
          def shuffle[A](xs: List[A]): List[A]                            = _random.shuffle(xs)
          def shuffle[A](xs: Batch[A]): Batch[A]                          = Batch.fromList(_random.shuffle(xs.toList))
          def alphanumeric(take: Int): List[Char]                         = _random.alphanumeric.take(take).toList

      val noop: Random =
        new Random:
          def nextBoolean: Boolean                                        = false
          def nextDouble: Double                                          = 0.0
          def between(minInclusive: Double, maxExclusive: Double): Double = 0.0
          def nextFloat: Float                                            = 0.0f
          def between(minInclusive: Float, maxExclusive: Float): Float    = 0.0f
          def nextInt: Int                                                = 0
          def nextInt(n: Int): Int                                        = 0
          def between(minInclusive: Int, maxExclusive: Int): Int          = 0
          def nextLong: Long                                              = 0L
          def nextLong(n: Long): Long                                     = 0L
          def between(minInclusive: Long, maxExclusive: Long): Long       = 0L
          def nextString(length: Int): String                             = ""
          def nextPrintableChar(): Char                                   = ' '
          def setSeed(seed: Long): Unit                                   = ()
          def shuffle[A](xs: List[A]): List[A]                            = xs
          def shuffle[A](xs: Batch[A]): Batch[A]                          = xs
          def alphanumeric(take: Int): List[Char]                         = List.fill(take)(' ')

    trait Locale:
      /** The current active system locale. None if no locale has been specified
        *
        * @return
        */
      def current: Option[CoreLocale]

      /** A `Batch` of locales available to the game in user preference order (most preferred to least)
        *
        * @return
        */
      def preferred: Batch[CoreLocale]

    object Locale:
      def apply(service: LocaleService): Services.Locale =
        new Services.Locale:
          def current: Option[CoreLocale]  = service.current
          def preferred: Batch[CoreLocale] = service.preferred

      val noop: Locale =
        new Locale:
          def current: Option[CoreLocale]  = None
          def preferred: Batch[CoreLocale] = Batch.empty

    trait DateTime:
      /** The current system date/time
        *
        * @return
        */
      def current: CoreDateTime

      def dateformat: DateFormat

      def timeformat: TimeFormat

    object DateTime:
      def apply(service: DateTimeService): Services.DateTime =
        new Services.DateTime:
          def current: CoreDateTime  = service.current
          def dateformat: DateFormat = service.dateformat
          def timeformat: TimeFormat = service.timeformat

      val noop: DateTime =
        new DateTime:
          def current: CoreDateTime  = CoreDateTime(0L, 0)
          def dateformat: DateFormat = DateFormat.YearMonthDay
          def timeformat: TimeFormat = TimeFormat.TwentyFourHour
