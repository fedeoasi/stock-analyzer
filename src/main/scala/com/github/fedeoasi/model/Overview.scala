package com.github.fedeoasi.model

import squants.market.{Money, USD}

import scala.util.Try

case class OverviewDto(
  Symbol: String,
  Name: String,
  Currency: String,
  Sector: String,
  MarketCapitalization: String,
  EBITDA: String,
  PERatio: String,
  EPS: String,
  ProfitMargin: String,
  AnalystTargetPrice: String,
  DividendYield: String
)

case class Overview(
  symbol: String,
  name: String,
  sector: String,
  marketCapitalization: Money,
  ebitda: Money,
  pERatio: Option[BigDecimal],
  ePS: BigDecimal,
  profitMargin: BigDecimal,
  analystTargetPrice: Money,
  dividendYield: BigDecimal
) {

  def toCsv: Seq[String] = Seq(
    symbol,
    name,
    sector,
    marketCapitalization.amount,
    ebitda.amount,
    pERatio.getOrElse("N/A"),
    ePS,
    profitMargin,
    analystTargetPrice.amount,
    dividendYield
  ).map(_.toString)
}

object Overview {
  val Header = Seq(
    "Symbol",
    "Name",
    "Sector",
    "MarketCapitalization",
    "EBITDA",
    "PERatio",
    "EPS",
    "ProfitMargin",
    "AnalystTargetPrice",
    "DividendYield"
  )

  def apply(dto: OverviewDto): Overview = {
    if (dto.Currency != "USD") throw new RuntimeException(s"Unknown currency $dto.Currency")
    Overview(
      dto.Symbol,
      dto.Name,
      dto.Sector,
      USD(BigDecimal(dto.MarketCapitalization)),
      USD(BigDecimal(dto.EBITDA.toDouble)),
      safeBigDecimal(dto.PERatio),
      BigDecimal(dto.EPS.toDouble),
      BigDecimal(dto.ProfitMargin.toDouble),
      USD(BigDecimal(dto.AnalystTargetPrice.toDouble)),
      BigDecimal(dto.DividendYield.toDouble),
    )
  }

  def safeBigDecimal(value: String): Option[BigDecimal] = Try(BigDecimal(value)).toOption
}