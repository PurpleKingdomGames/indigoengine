package indigoengine.shared.logging

/** The level at which a message is logged. `StdOut` logs the message exactly as given, with no decoration. */
enum LogLevel derives CanEqual:
  case Info, Warn, Error, Debug, StdOut

  def label: String =
    this match
      case Info   => "INFO"
      case Warn   => "WARN"
      case Error  => "ERROR"
      case Debug  => "DEBUG"
      case StdOut => "STDOUT"
