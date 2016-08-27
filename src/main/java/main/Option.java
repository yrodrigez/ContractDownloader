package main;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.SimpleDateFormat;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Option {

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public String getContractSymbol() {
    return contractSymbol;
  }

  public void setContractSymbol(String contractSymbol) {
    this.contractSymbol = contractSymbol;
  }

  public double getStrike() {
    return strike;
  }

  public void setStrike(double strike) {
    this.strike = strike;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public double getLastPrice() {
    return lastPrice;
  }

  public void setLastPrice(double lastPrice) {
    this.lastPrice = lastPrice;
  }

  public double getChange() {
    return change;
  }

  public void setChange(double change) {
    this.change = change;
  }

  public double getPercentChange() {
    return percentChange;
  }

  public void setPercentChange(double percentChange) {
    this.percentChange = percentChange;
  }

  public double getVolume() {
    return volume;
  }

  public void setVolume(double volume) {
    this.volume = volume;
  }

  public double getOpenInterest() {
    return openInterest;
  }

  public void setOpenInterest(double openInterest) {
    this.openInterest = openInterest;
  }

  public double getBid() {
    return bid;
  }

  public void setBid(double bid) {
    this.bid = bid;
  }

  public double getAsk() {
    return ask;
  }

  public void setAsk(double ask) {
    this.ask = ask;
  }

  public String getContractSize() {
    return contractSize;
  }

  public void setContractSize(String contractSize) {
    this.contractSize = contractSize;
  }

  public long getExpiration() {
    return expiration;
  }

  public void setExpiration(long expiration) {
    this.expiration = expiration;
  }

  public long getLastTradeDate() {
    return lastTradeDate;
  }

  public void setLastTradeDate(long lastTradeDate) {
    this.lastTradeDate = lastTradeDate;
  }

  public double getImpliedVolatility() {
    return impliedVolatility;
  }

  public void setImpliedVolatility(double impliedVolatility) {
    this.impliedVolatility = impliedVolatility;
  }

  public boolean isInTheMoney() {
    return inTheMoney;
  }

  public void setInTheMoney(boolean inTheMoney) {
    this.inTheMoney = inTheMoney;
  }

  @Override
  public String toString() {
    return "main.Option{" +
      "type=" + type.name() +
      ", contractSymbol='" + contractSymbol + '\'' +
      ", strike=" + strike +
      ", bid=" + bid +
      ", ask=" + ask +
      ", lastTradeDate=" + lastTradeDate +
      ", expiration=" + new SimpleDateFormat("yy-MM-dd").format(new java.util.Date(expiration * 1000L))+
      '}';
  }

  public enum Type { PUT, CALL }
  @JsonIgnore
  public Type type;
  @JsonProperty("contractSymbol")
  private String contractSymbol; // "AAPL160812C00070000",
  @JsonProperty("strike")
  private double strike; // 70.0,
  @JsonProperty("currency")
  private String currency; // "USD",
  @JsonProperty("lastPrice")
  private double lastPrice; // 37.5,
  @JsonProperty("change")
  private double change; // 0.0,
  @JsonProperty("percentChange")
  private double percentChange; // 0.0,
  @JsonProperty("volume")
  private double volume; // 6,
  @JsonProperty("openInterest")
  private double openInterest; // 0,
  @JsonProperty("bid")
  private double bid; // 37.2,
  @JsonProperty("ask")
  private double ask; // 37.8,
  @JsonProperty("contractSize")
  private String contractSize; // "REGULAR",
  @JsonProperty("expiration")
  private long expiration; // 1470960000,
  @JsonProperty("lastTradeDate")
  private long lastTradeDate; // 1470439901,
  @JsonProperty("impliedVolatility")
  private double impliedVolatility; // 1.8398445507812498,
  @JsonProperty("inTheMoney")
  private boolean inTheMoney; // true

}