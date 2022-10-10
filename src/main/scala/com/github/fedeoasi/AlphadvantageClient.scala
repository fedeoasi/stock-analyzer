package com.github.fedeoasi

import com.github.fedeoasi.model.{DailyEntries, DailyEntry, Overview, OverviewDto}
import com.typesafe.scalalogging.StrictLogging
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization
import org.json4s.{DefaultFormats, JObject, JString}
import squants.market.USD
import sttp.client3.logging.slf4j.Slf4jLoggingBackend
import sttp.client3._

import java.io.FileWriter
import java.nio.file.{Files, Paths}
import java.time.LocalDate

class AlphadvantageClient extends StrictLogging {

  private val ApiKey = sys.env("ALPHADVANTAGE_API_KEY")

  private implicit val serialization: Serialization.type = org.json4s.native.Serialization
  private implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

  private val backend = Slf4jLoggingBackend(HttpClientSyncBackend())

  private val MaxRequestsPerMinute = 5

  private def httpResponse(function: String, params: Map[String, String]) = {
    val request = basicRequest.get(uri"https://www.alphavantage.co/query?function=$function&apikey=$ApiKey&$params")
    val response = request.send(backend)
    Thread.sleep(60000 / MaxRequestsPerMinute) // TODO use a proper rate limiter
    response
  }

  def overview(ticker: String): Option[Overview] = {
    getFromCache("overview", ticker).orElse {
      val response = httpResponse("OVERVIEW", Map("symbol" -> ticker))
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

  def timeSeriesDaily(ticker: String): DailyEntries = {
    val entries = getFromCache("timeSeriesDaily", ticker).orElse {
      val response = httpResponse("TIME_SERIES_DAILY", Map("symbol" -> ticker))

      response.body match {
        case Left(value) =>
          logger.error(value)
          None
        case Right(value) =>
          writeCache("timeSeriesDaily", ticker, value)
          Some(value)
      }
    }.toSeq.flatMap { json =>
      val parsedJson = parse(json, useBigDecimalForDouble = true)
      parsedJson \ "Time Series (Daily)" match {
        case JObject(jObj) =>
          jObj.flatMap {
            case (key, entry: JObject) =>
              val JString(close) = entry \ "4. close"
              Seq(DailyEntry(LocalDate.parse(key), USD(BigDecimal(close))))
            case (_, _) =>
              Seq.empty[DailyEntry]
          }
        case other =>
          logger.warn(s"Unexpected JValue type: ${other.getClass.getSimpleName}")
          Seq.empty[DailyEntry]
      }
    }.sortBy(_.date)
    DailyEntries(entries)
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
