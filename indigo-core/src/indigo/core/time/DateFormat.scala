package indigo.core.time

/** The format to use when formatting a date
  */
enum DateFormat derives CanEqual:
  case YearMonthDay, DayMonthYear, MonthDayYear
