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
  EVToEBITDA: String,
  EVToRevenue: String,
  PERatio: String,
  EPS: String,
  ProfitMargin: String,
  RevenueTTM: String,
  AnalystTargetPrice: String,
  DividendYield: String
)

case class Overview(
  symbol: String,
  name: String,
  sector: String,
  marketCapitalization: Money,
  ebitda: Money,
  evToEbitda: Money,
  evToRevenue: Money,
  pERatio: Option[BigDecimal],
  ePS: BigDecimal,
  profitMargin: BigDecimal,
  revenueTTM: Money,
  analystTargetPrice: Money,
  dividendYield: BigDecimal
) {

  def toCsv: Seq[String] = Seq(
    symbol,
    name,
    sector,
    marketCapitalization.amount,
    ebitda.amount,
    evToEbitda.amount,
    evToRevenue.amount,
    pERatio.getOrElse("N/A"),
    ePS,
    profitMargin,
    revenueTTM.amount,
    dividendYield,
    analystTargetPrice.amount
  ).map(_.toString)
}

object Overview {
  val Header = Seq(
    "Symbol",
    "Name",
    "Sector",
    "MarketCapitalization",
    "EBITDA",
    "EVToEBITDA",
    "EVToRevenue",
    "PERatio",
    "EPS",
    "ProfitMargin",
    "RevenueTTM",
    "DividendYield",
    "AnalystTargetPrice"
  )

  def apply(dto: OverviewDto): Overview = {
    if (dto.Currency != "USD") throw new RuntimeException(s"Unknown currency $dto.Currency")
    Overview(
      symbol = dto.Symbol,
      name = dto.Name,
      sector = dto.Sector,
      marketCapitalization = USD(BigDecimal(dto.MarketCapitalization)),
      ebitda = USD(BigDecimal(dto.EBITDA.toDouble)),
      evToEbitda = USD(BigDecimal(dto.EVToEBITDA.toDouble)),
      evToRevenue = USD(BigDecimal(dto.EVToRevenue.toDouble)),
      pERatio = safeBigDecimal(dto.PERatio),
      ePS = BigDecimal(dto.EPS.toDouble),
      profitMargin = BigDecimal(dto.ProfitMargin.toDouble),
      revenueTTM = USD(BigDecimal(dto.RevenueTTM.toDouble)),
      analystTargetPrice = USD(BigDecimal(dto.AnalystTargetPrice.toDouble)),
      dividendYield = BigDecimal(dto.DividendYield.toDouble),
    )
  }

  def safeBigDecimal(value: String): Option[BigDecimal] = Try(BigDecimal(value)).toOption
}