package com.github.fedeoasi

import com.github.fedeoasi.model.Overview
import com.github.tototoshi.csv.CSVWriter
import com.typesafe.scalalogging.StrictLogging

object Main extends StrictLogging {

  private val Tickers = Seq(
    "AMGN",
    "ASML",
    "GOOG",
    "MELI",
    "SMR",
    "SNOW",
    "TEAM",
    "TSLA",
    "TTD",
    "TYL",
    "UNH",
    "VEEV"
  )


  def main(args: Array[String]): Unit = {
    val client = new AlphadvantageClient

    val writer = CSVWriter.open("out.csv")
    val header = Overview.Header ++ Seq("MostRecentPrice")
    println(header)
    writer.writeRow(header)

    try {
      Tickers.foreach { ticker =>
        val Some(overview) = client.overview(ticker)
        val entries = client.timeSeriesDaily(ticker)
        val mostRecentPrice = entries.mostRecentEntry.close
        val row = overview.toCsv ++ Seq(mostRecentPrice.amount)
        println(row)
        writer.writeRow(row)
      }
    } finally {
      writer.close()
    }
  }
}
