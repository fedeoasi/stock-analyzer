package com.github.fedeoasi.model

import squants.market.Money

import java.time.LocalDate

case class DailyEntry(
  date: LocalDate,
  close: Money
)

case class DailyEntries(values: Seq[DailyEntry]) {
  def mostRecentDate: LocalDate = mostRecentEntry.date
  def mostRecentEntry: DailyEntry = values.maxBy(_.date)
}