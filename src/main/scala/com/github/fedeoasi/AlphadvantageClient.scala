package com.github.fedeoasi

import com.github.fedeoasi.model.{Overview, OverviewDto}
import com.typesafe.scalalogging.StrictLogging
import org.json4s.native.Serialization
import sttp.client3.logging.slf4j.Slf4jLoggingBackend
import sttp.client3.{HttpClientSyncBackend, basicRequest}

import sttp.client3._

import java.io.FileWriter
import java.nio.file.{Files, Paths}

class AlphadvantageClient extends StrictLogging {

  private val ApiKey = sys.env("ALPHADVANTAGE_API_KEY")

  private implicit val serialization = org.json4s.native.Serialization
  private implicit val formats = org.json4s.DefaultFormats

  def overview(ticker: String): Option[Overview] = {
    getFromCache("overview", ticker).orElse {
      val request = basicRequest.get(uri"https://www.alphavantage.co/query?function=OVERVIEW&symbol=$ticker&apikey=$ApiKey")

      val backend = Slf4jLoggingBackend(HttpClientSyncBackend())
      val response = request
        .send(backend)

      // TODO use a rate limiter
      Thread.sleep(12000)

      response.body match {
        case Left(value) =>
          logger.error(value)
          None
        case Right(value) =>
          writeCache("overview", ticker, value)
          Some(value)
      }
    }.map { json =>
      Overview(Serialization.read[OverviewDto](json))
    }
  }

  val CachePath = Paths.get("alphadvantage.cache")

  def writeCache(operation: String, key: String, value: String): Unit = {
    CachePath.toFile.mkdir()
    val cacheKey = s"$operation-$key"
    logger.info(s"Writing cache entry to $cacheKey")
    val writer = new FileWriter(CachePath.resolve(cacheKey).toFile)
    writer.write(value)
    writer.close()
  }

  def getFromCache(operation: String, key: String): Option[String] = {
    CachePath.toFile.mkdir()
    val filePath = CachePath.resolve(s"$operation-$key")
    if (filePath.toFile.exists()) {
      Some(new String(Files.readAllBytes(filePath)))
    } else {
      None
    }
  }
}
