package com.github.fedeoasi.model

import java.time.LocalDate

case class EarningsScheduleEntry(
  symbol: String,
  name: String,
  reportDate: LocalDate,
  fiscalDateEnding: LocalDate,
  estimate: Option[BigDecimal],
  currency: String
)