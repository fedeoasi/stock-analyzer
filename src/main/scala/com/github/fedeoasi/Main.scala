package com.github.fedeoasi

import com.github.fedeoasi.model.Overview
import com.github.tototoshi.csv.CSVWriter
import com.typesafe.scalalogging.StrictLogging

import java.io.File
import java.time.LocalDate
import scala.collection.immutable.Seq

object Main extends StrictLogging {

  def loadTickers: Seq[String] = {
    val source = scala.io.Source.fromFile(new File("tickers-full.txt"))
    val tickers = source.getLines.toSeq
    source.close()
    tickers
  }

  def main(args: Array[String]): Unit = {
    val client = new AlphadvantageClient

    val writer = CSVWriter.open(s"out-${LocalDate.now()}.csv")
    val header = Overview.Header ++ Seq("MostRecentPrice") ++ Seq("ExpectedTargetPercentage")
    writer.writeRow(header)

    val tickers = loadTickers

    try {
      tickers.foreach { ticker =>
        println(s"ticker $ticker")
        val Some(overview) = client.overview(ticker)
        val entries = client.timeSeriesDaily(ticker)
        val mostRecentPrice = entries.mostRecentEntry.close
        val row = overview.toCsv ++ Seq(mostRecentPrice.amount) ++ Seq(overview.analystTargetPrice.amount / mostRecentPrice.amount - 1)
        println(row)
        writer.writeRow(row)
      }
    } finally {
      writer.close()
    }

    val earnings = client.earningsCalendar.filter(e => tickers.contains(e.symbol))
    earnings.sortBy(_.reportDate).foreach { earningsScheduleEntry =>
      println(s"${earningsScheduleEntry.reportDate}: ${earningsScheduleEntry.symbol}")
    }
  }
}
