package com.github.fedeoasi

import com.github.fedeoasi.model.{DailyEntries, DailyEntry, EarningsScheduleEntry, Overview, OverviewDto}
import com.github.tototoshi.csv.CSVReader
import com.typesafe.scalalogging.StrictLogging
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization
import org.json4s.{DefaultFormats, JObject, JString}
import squants.market.USD
import sttp.client3.logging.slf4j.Slf4jLoggingBackend
import sttp.client3._

import java.io.{FileWriter, StringReader}
import java.nio.file.{Files, Paths}
import java.time.LocalDate
import scala.util.{Failure, Success, Try}

class AlphadvantageClient extends StrictLogging {

  // example:
  //   https://www.alphavantage.co/query?function=EARNINGS&apikey=*****&symbol=TTD
  private val ApiKey = sys.env("ALPHADVANTAGE_API_KEY")

  private implicit val serialization: Serialization.type = org.json4s.native.Serialization
  private implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

  private val backend = Slf4jLoggingBackend(HttpClientSyncBackend())

  private val MaxRequestsPerMinute = 3

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
          if (value.contains("Our standard API rate limit is 25 requests per day")) {
            None
          } else {
            writeCache("overview", ticker, value)
            Some(value)
          }
      }
    }.map { json =>
      Try {
        Overview(Serialization.read[OverviewDto](json))
      } match {
        case Failure(exception) =>
          logger.error(s"Error while parsing json: $json", exception)
          throw exception
        case Success(value) => value
      }
    }
  }

  def timeSeriesDaily(ticker: String): DailyEntries = {
    val entries = getFromCache("timeSeriesDaily", ticker).orElse {
      val response = httpResponse("TIME_SERIES_DAILY", Map("symbol" -> ticker))

      println(response)
      println(response.code)
      println(response.statusText)

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

  def earningsCalendar: Seq[EarningsScheduleEntry] = {
    getFromCache("earningsCalendar", "") match {
      case Some(value) =>
        val reader = CSVReader.open(new StringReader(value))
        reader.allWithHeaders().map { obj =>
          EarningsScheduleEntry(
            symbol = obj("symbol"),
            name = obj("name"),
            reportDate = LocalDate.parse(obj("reportDate")),
            fiscalDateEnding = LocalDate.parse(obj("fiscalDateEnding")),
            estimate = Overview.safeBigDecimal(obj("estimate")),
            currency = obj("currency")
          )
        }
      case None =>
        // https://www.alphavantage.co/query?function=EARNINGS_CALENDAR&apikey=6E9VHGE2SK5O2I85
        logger.warn(s"Please download the earnings calendar csv and place it in the cache at $CachePath/earningsCalendar-")
        Seq.empty
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
    val cacheKey = s"$operation-$key"
    val filePath = CachePath.resolve(cacheKey)
    if (filePath.toFile.exists()) {
      println(s"Reading from cache $cacheKey")
      Some(new String(Files.readAllBytes(filePath)))
    } else {
      None
    }
  }
}
