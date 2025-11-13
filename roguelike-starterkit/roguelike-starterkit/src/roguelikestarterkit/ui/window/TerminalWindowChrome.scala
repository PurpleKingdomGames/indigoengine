package roguelikestarterkit.ui.window

import indigo.shared.collections.Batch
import indigo.shared.datatypes.BindingKey
import indigo.shared.datatypes.RGBA
import indigoextras.ui.*
import roguelikestarterkit.Tile
import roguelikestarterkit.ui.*

/** Provides simple terminal window chrome with a title, close button, resize button, and vertical
  * scroll bar.
  */
final case class TerminalWindowChrome[ReferenceData](
    parentWindowId: WindowId,
    charSheet: CharSheet,
    title: Option[String],
    isResizable: Boolean,
    isDraggable: Boolean,
    isClosable: Boolean,
    isScrollable: Boolean
):
  def withParent(parentWindowId: WindowId): TerminalWindowChrome[ReferenceData] =
    this.copy(parentWindowId = parentWindowId)

  def withCharSheet(charSheet: CharSheet): TerminalWindowChrome[ReferenceData] =
    this.copy(charSheet = charSheet)

  def withTitle(newTitle: String): TerminalWindowChrome[ReferenceData] =
    this.copy(title = Some(newTitle))
  def noTitle: TerminalWindowChrome[ReferenceData] =
    this.copy(title = None)

  def withResizable(value: Boolean): TerminalWindowChrome[ReferenceData] =
    this.copy(isResizable = value)
  def resizable: TerminalWindowChrome[ReferenceData] =
    this.copy(isResizable = true)
  def noResize: TerminalWindowChrome[ReferenceData] =
    this.copy(isResizable = false)

  def withDraggable(value: Boolean): TerminalWindowChrome[ReferenceData] =
    this.copy(isDraggable = value)
  def draggable: TerminalWindowChrome[ReferenceData] =
    this.copy(isDraggable = true)
  def noDrag: TerminalWindowChrome[ReferenceData] =
    this.copy(isDraggable = false)

  def withClosable(value: Boolean): TerminalWindowChrome[ReferenceData] =
    this.copy(isClosable = value)
  def closable: TerminalWindowChrome[ReferenceData] =
    this.copy(isClosable = true)
  def noClose: TerminalWindowChrome[ReferenceData] =
    this.copy(isClosable = false)

  def withScrollable(value: Boolean): TerminalWindowChrome[ReferenceData] =
    this.copy(isScrollable = value)
  def scrollable: TerminalWindowChrome[ReferenceData] =
    this.copy(isScrollable = true)
  def noScroll: TerminalWindowChrome[ReferenceData] =
    this.copy(isScrollable = false)

  def build(content: ComponentGroup[ReferenceData]): ComponentGroup[ReferenceData] =
    ComponentGroup()
      .withBoundsMode(BoundsMode.inherit)
      .withLayout(ComponentLayout.Vertical(Padding(3, 1, 1, 1)))
      .anchorConditional(isScrollable)(
        TerminalScrollPane(
          BindingKey(parentWindowId.toString + "-scroll-pane"),
          BoundsMode.offset(-2, if title.isDefined then -4 else -2),
          content,
          charSheet
        ),
        Anchor.TopLeft.withPadding(Padding(if title.isDefined then 3 else 1, 1, 1, 1))
      )
      .anchorConditional(!isScrollable)(
        MaskedPane(
          BoundsMode.offset(-2, if title.isDefined then -4 else -2),
          content
        ),
        Anchor.TopLeft.withPadding(Padding(if title.isDefined then 3 else 1, 1, 1, 1))
      )
      .anchorOptional(
        title.map { _title =>
          val btn =
            TerminalButton[ReferenceData](
              _title,
              TerminalButton
                .Theme(
                  charSheet,
                  RGBA.White,
                  RGBA.Black
                )
                .addBorder
                .modifyBorderTiles(
                  _.withBottomLeft(Tile.`├`)
                    .withBottomRight(Tile.`┤`)
                )
            )

          if isDraggable then
            btn
              .onDrag { (_: ReferenceData, dragData) =>
                Batch(
                  WindowEvent
                    .Move(
                      parentWindowId,
                      dragData.position - dragData.offset,
                      Space.Screen
                    )
                )
              }
              .reportDrag
              .withBoundsType(BoundsType.FillWidth(3, Padding(0)))
          else btn.withBoundsType(BoundsType.FillWidth(3, Padding(0)))
        },
        Anchor.TopLeft
      )
      .anchorConditional(isResizable)(
        TerminalButton
          .fromTile(
            Tile.BLACK_DOWN_POINTING_TRIANGLE,
            TerminalButton.Theme(
              charSheet,
              RGBA.Black -> RGBA.Silver,
              RGBA.Black -> RGBA.White,
              RGBA.White -> RGBA.Black,
              hasBorder = false
            )
          )
          .onDrag { (_: ReferenceData, dragData) =>
            Batch(
              WindowEvent
                .Resize(
                  parentWindowId,
                  dragData.position.toDimensions + Dimensions(1),
                  Space.Screen
                )
            )
          }
          .reportDrag,
        Anchor.BottomRight
      )
      .anchorConditional(isClosable)(
        TerminalButton
          .fromTile(
            Tile.x,
            TerminalButton.Theme(
              charSheet,
              RGBA.Black -> RGBA.Silver,
              RGBA.Black -> RGBA.White,
              RGBA.White -> RGBA.Black,
              hasBorder = false
            )
          )
          .onClick(
            WindowEvent.Close(parentWindowId)
          ),
        Anchor.TopRight
      )

object TerminalWindowChrome:

  def apply[ReferenceData](
      parentWindowId: WindowId,
      charSheet: CharSheet
  ): TerminalWindowChrome[ReferenceData] =
    TerminalWindowChrome(parentWindowId, charSheet, None, true, true, true, true)
