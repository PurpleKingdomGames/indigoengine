package indigoplugin

enum ElectronInstall derives CanEqual:
  case Global
  case Version(version: String)
  case Latest
  case PathToExecutable(path: String)

  def executable: String =
    this match
      case ElectronInstall.Global                 => "electron"
      case ElectronInstall.Version(_)             => "npx --no-install electron"
      case ElectronInstall.Latest                 => "npx --no-install electron"
      case ElectronInstall.PathToExecutable(path) => path

  def devDependencies: String =
    this match
      case ElectronInstall.Global              => ""
      case ElectronInstall.Version(version)    => s""""electron": "${version}""""
      case ElectronInstall.Latest              => ""
      case ElectronInstall.PathToExecutable(_) => ""
