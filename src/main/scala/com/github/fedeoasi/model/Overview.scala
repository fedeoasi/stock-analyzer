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
  Beta: String,
  RevenueTTM: String,
  QuarterlyEarningsGrowthYOY: String,
  QuarterlyRevenueGrowthYOY: String,
  AnalystTargetPrice: String,
  DividendYield: String
)

case class Overview(
  symbol: String,
  name: String,
  sector: String,
  marketCapitalization: Money,
  ebitda: Option[Money],
  evToEbitda: Option[Money],
  evToRevenue: Option[BigDecimal],
  pERatio: Option[BigDecimal],
  ePS: BigDecimal,
  profitMargin: BigDecimal,
  beta: BigDecimal,
  revenueTTM: Money,
  quarterlyEarningsGrowthYoy: Option[BigDecimal],
  quarterlyRevenueGrowthYoy: Option[BigDecimal],
  analystTargetPrice: Money,
  dividendYield: Option[BigDecimal]
) {

  def toCsv: Seq[String] = Seq(
    symbol,
    name,
    sector,
    marketCapitalization.amount,
    ebitda.map(_.amount).getOrElse("N/A"),
    evToEbitda.map(_.amount).getOrElse("N/A"),
    evToRevenue.getOrElse("N/A"),
    pERatio.getOrElse("N/A"),
    ePS,
    profitMargin,
    beta,
    revenueTTM.amount,
    quarterlyRevenueGrowthYoy.getOrElse("N/A"),
    quarterlyEarningsGrowthYoy.getOrElse("N/A"),
    dividendYield.getOrElse("N/A"),
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
    "Beta",
    "RevenueTTM",
    "QuarterlyEarningsGrowthYOY",
    "QuarterlyRevenueGrowthYOY",
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
      ebitda = safeBigDecimal(dto.EBITDA).map(USD.apply),
      evToEbitda = safeBigDecimal(dto.EVToEBITDA).map(USD.apply),
      evToRevenue = safeBigDecimal(dto.EVToRevenue),
      pERatio = safeBigDecimal(dto.PERatio),
      ePS = BigDecimal(dto.EPS.toDouble),
      profitMargin = BigDecimal(dto.ProfitMargin.toDouble),
      beta = BigDecimal(dto.Beta),
      revenueTTM = USD(BigDecimal(dto.RevenueTTM.toDouble)),
      quarterlyRevenueGrowthYoy = safeBigDecimal(dto.QuarterlyRevenueGrowthYOY),
      quarterlyEarningsGrowthYoy = safeBigDecimal(dto.QuarterlyEarningsGrowthYOY),
      analystTargetPrice = USD(BigDecimal(dto.AnalystTargetPrice.toDouble)),
      dividendYield = safeBigDecimal(dto.DividendYield),
    )
  }

  def safeBigDecimal(value: String): Option[BigDecimal] = Try(BigDecimal(value)).toOption
}