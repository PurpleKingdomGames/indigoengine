package roguelikestarterkit.ui

// UI Shaders

val uiShaders: Set[indigo.Shader] =
  Set(
    rogueui.shaders.LayerMask.shader
  )

// UI General Datatypes

type UiContext[StartUpData, A] = datatypes.UiContext[StartUpData, A]
val UiContext: datatypes.UiContext.type = datatypes.UiContext

type CharSheet = datatypes.CharSheet
val CharSheet: datatypes.CharSheet.type = datatypes.CharSheet

type Coords = datatypes.Coords
val Coords: datatypes.Coords.type = datatypes.Coords

type Dimensions = datatypes.Dimensions
val Dimensions: datatypes.Dimensions.type = datatypes.Dimensions

type Bounds = datatypes.Bounds
val Bounds: datatypes.Bounds.type = datatypes.Bounds

// UI Windows

val WindowManager: window.WindowManager.type = window.WindowManager

type WindowManagerModel[StartupData, A] = window.WindowManagerModel[StartupData, A]
val WindowManagerModel: window.WindowManagerModel.type = window.WindowManagerModel

type WindowManagerViewModel[StartupData, A] = window.WindowManagerViewModel[StartupData, A]
val WindowManagerViewModel: window.WindowManagerViewModel.type = window.WindowManagerViewModel

type WindowId = window.WindowId
val WindowId: window.WindowId.type = window.WindowId

type WindowModel[StartupData, CA, A] = window.WindowModel[StartupData, CA, A]
val WindowModel: window.WindowModel.type = window.WindowModel

type WindowViewModel = window.WindowViewModel
val WindowViewModel: window.WindowViewModel.type = window.WindowViewModel

type WindowEvent = window.WindowEvent
val WindowEvent: window.WindowEvent.type = window.WindowEvent

// UI Components

type Component[A] = component.Component[A]

type ComponentGroup = component.ComponentGroup
val ComponentGroup: component.ComponentGroup.type = component.ComponentGroup

type ComponentFragment = component.ComponentFragment
val ComponentFragment: component.ComponentFragment.type = component.ComponentFragment

type ComponentLayout = component.ComponentLayout
val ComponentLayout: component.ComponentLayout.type = component.ComponentLayout

type Overflow = component.Overflow
val Overflow: component.Overflow.type = component.Overflow

type Padding = component.Padding
val Padding: component.Padding.type = component.Padding

// UI Built in components

type Button = components.Button
val Button: components.Button.type = components.Button

type Label = components.Label
val Label: components.Label.type = components.Label
