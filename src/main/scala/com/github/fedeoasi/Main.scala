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

    val results = Tickers.flatMap { ticker =>
      val result = client.overview(ticker)
      result
    }
    results.foreach(println)

    logger.info("hello")

    val writer = CSVWriter.open("out.csv")
    writer.writeRow(Overview.Header)
    writer.writeAll(results.map(_.toCsv))
  }
}
