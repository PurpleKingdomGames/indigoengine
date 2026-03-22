package indigo.internal.models

import tyrian.extensions.ExtensionId

enum LaunchStatus:
  case Retry(extensionId: ExtensionId)
  case AttemptStart(extensionId: ExtensionId)
  case Started(extensionId: ExtensionId)
  case Failed(extensionId: ExtensionId)
