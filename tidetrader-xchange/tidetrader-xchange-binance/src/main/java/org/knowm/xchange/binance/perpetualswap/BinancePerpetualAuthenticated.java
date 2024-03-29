package org.knowm.xchange.binance.perpetualswap;

import org.knowm.xchange.binance.dto.BinanceException;
import org.knowm.xchange.binance.dto.account.*;
import org.knowm.xchange.binance.dto.trade.*;
import org.knowm.xchange.binance.dto.marketdata.BinancePerpetualBalance;
import org.knowm.xchange.binance.dto.marketdata.BinancePositionInfo;
import org.knowm.xchange.binance.dto.trade.BinancePerpetualLeverage;
import org.knowm.xchange.binance.dto.trade.BinancePerpetualOrder;
import org.knowm.xchange.binance.dto.trade.Binanceresult;
import si.mazi.rescu.ParamsDigest;
import si.mazi.rescu.SynchronizedValueFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
public interface BinancePerpetualAuthenticated extends BinancePerpetual {

  String SIGNATURE = "signature";
  String X_MBX_APIKEY = "X-MBX-APIKEY";

  @POST
  @Path("fapi/v1/order")
  /**
   * Send in a new order
   *
   * @param symbol
   * @param side
   * @param type
   * @param timeInForce
   * @param quantity
   * @param price optional, must be provided for limit orders only
   * @param newClientOrderId optional, a unique id for the order. Automatically generated if not
   *     sent.
   * @param stopPrice optional, used with stop orders
   * @param icebergQty optional, used with iceberg orders
   * @param recvWindow optional
   * @param timestamp
   * @return
   * @throws IOException
   * @throws BinanceException
   */
  BinancePerpetualOrder newOrder(
      @FormParam("symbol") String symbol,
      @FormParam("side") OrderSide side,
      @FormParam("type") OrderType type,
      @FormParam("timeInForce") TimeInForce timeInForce,
      @FormParam("quantity") BigDecimal quantity,
      @FormParam("price") BigDecimal price,
      @FormParam("newClientOrderId") String newClientOrderId,
      @FormParam("stopPrice") BigDecimal stopPrice,
      @FormParam("recvWindow") Long recvWindow,
      @FormParam("timestamp") SynchronizedValueFactory<Long> timestamp,
      @HeaderParam(X_MBX_APIKEY) String apiKey,
      @QueryParam(SIGNATURE) ParamsDigest signature)
      throws IOException, BinanceException;

  @POST
  @Path("fapi/v1/batchOrders")
  List<BinanceNewOrder> batchOrders(
          @FormParam("batchOrders") String batchOrders,
          @FormParam("recvWindow") Long recvWindow,
          @FormParam("timestamp") SynchronizedValueFactory<Long> timestamp,
          @HeaderParam(X_MBX_APIKEY) String apiKey,
          @QueryParam(SIGNATURE) ParamsDigest signature)
          throws IOException, BinanceException;

  @POST
  @Path("fapi/v1/order/test")
  /**
   * Test new order creation and signature/recvWindow long. Creates and validates a new order but
   * does not send it into the matching engine.
   *
   * @param symbol
   * @param side
   * @param type
   * @param timeInForce
   * @param quantity
   * @param price
   * @param newClientOrderId optional, a unique id for the order. Automatically generated by
   *     default.
   * @param stopPrice optional, used with STOP orders
   * @param icebergQty optional used with icebergOrders
   * @param recvWindow optional
   * @param timestamp
   * @return
   * @throws IOException
   * @throws BinanceException
   */
  Object testNewOrder(
      @FormParam("symbol") String symbol,
      @FormParam("side") OrderSide side,
      @FormParam("type") OrderType type,
      @FormParam("timeInForce") TimeInForce timeInForce,
      @FormParam("quantity") BigDecimal quantity,
      @FormParam("price") BigDecimal price,
      @FormParam("newClientOrderId") String newClientOrderId,
      @FormParam("stopPrice") BigDecimal stopPrice,
      @FormParam("icebergQty") BigDecimal icebergQty,
      @FormParam("recvWindow") Long recvWindow,
      @FormParam("timestamp") SynchronizedValueFactory<Long> timestamp,
      @HeaderParam(X_MBX_APIKEY) String apiKey,
      @QueryParam(SIGNATURE) ParamsDigest signature)
      throws IOException, BinanceException;

  @GET
  @Path("fapi/v1/order")
  /**
   * Check an order's status.<br>
   * Either orderId or origClientOrderId must be sent.
   *
   * @param symbol
   * @param orderId optional
   * @param origClientOrderId optional
   * @param recvWindow optional
   * @param timestamp
   * @param apiKey
   * @param signature
   * @return
   * @throws IOException
   * @throws BinanceException
   */
  BinanceOrder orderStatus(
      @QueryParam("symbol") String symbol,
      @QueryParam("orderId") long orderId,
      @QueryParam("origClientOrderId") String origClientOrderId,
      @QueryParam("recvWindow") Long recvWindow,
      @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
      @HeaderParam(X_MBX_APIKEY) String apiKey,
      @QueryParam(SIGNATURE) ParamsDigest signature)
      throws IOException, BinanceException;

  @DELETE
  @Path("/fapi/v1/order")
  /**
   * Cancel an active order.
   *
   * @param symbol
   * @param orderId optional
   * @param origClientOrderId optional
   * @param recvWindow optional
   * @param timestamp
   * @param apiKey
   * @param signature
   * @return
   * @throws IOException
   * @throws BinanceException
   */
  BinanceCancelledOrder cancelOrder(
      @QueryParam("symbol") String symbol,
      @QueryParam("orderId") Long orderId,
      @QueryParam("origClientOrderId") String origClientOrderId,
      @QueryParam("recvWindow") Long recvWindow,
      @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
      @HeaderParam(X_MBX_APIKEY) String apiKey,
      @QueryParam(SIGNATURE) ParamsDigest signature)
      throws IOException, BinanceException;

  @DELETE
  @Path("fapi/v3/openOrders")
  /**
   * Cancels all active orders on a symbol. This includes OCO orders.
   *
   * @param symbol
   * @param recvWindow optional
   * @param timestamp
   * @return
   * @throws IOException
   * @throws BinanceException
   */
  List<BinanceCancelledOrder> cancelAllOpenOrders(
      @QueryParam("symbol") String symbol,
      @QueryParam("recvWindow") Long recvWindow,
      @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
      @HeaderParam(X_MBX_APIKEY) String apiKey,
      @QueryParam(SIGNATURE) ParamsDigest signature)
      throws IOException, BinanceException;

  @GET
  @Path("fapi/v1/openOrders")
  /**
   * Get open orders on a symbol.
   *
   * @param symbol optional
   * @param recvWindow optional
   * @param timestamp
   * @return
   * @throws IOException
   * @throws BinanceException
   */
  List<BinanceOrder> openOrders(
      @QueryParam("symbol") String symbol,
      @QueryParam("recvWindow") Long recvWindow,
      @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
      @HeaderParam(X_MBX_APIKEY) String apiKey,
      @QueryParam(SIGNATURE) ParamsDigest signature)
      throws IOException, BinanceException;

  @GET
  @Path("fapi/v3/allOrders")
  /**
   * Get all account orders; active, canceled, or filled. <br>
   * If orderId is set, it will get orders >= that orderId. Otherwise most recent orders are
   * returned.
   *
   * @param symbol
   * @param orderId optional
   * @param limit optional
   * @param recvWindow optional
   * @param timestamp
   * @param apiKey
   * @param signature
   * @return
   * @throws IOException
   * @throws BinanceException
   */
  List<BinanceOrder> allOrders(
      @QueryParam("symbol") String symbol,
      @QueryParam("orderId") Long orderId,
      @QueryParam("limit") Integer limit,
      @QueryParam("recvWindow") Long recvWindow,
      @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
      @HeaderParam(X_MBX_APIKEY) String apiKey,
      @QueryParam(SIGNATURE) ParamsDigest signature)
      throws IOException, BinanceException;

  @GET
  @Path("fapi/v2/balance")
  /**
   * Get current account information.
   *
   * @param recvWindow optional
   * @param timestamp
   * @return
   * @throws IOException
   * @throws BinanceException
   */
  List<BinancePerpetualBalance> balance(
          @QueryParam("recvWindow") Long recvWindow,
          @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
          @HeaderParam(X_MBX_APIKEY) String apiKey,
          @QueryParam(SIGNATURE) ParamsDigest signature)
          throws IOException, BinanceException;

  @GET
  @Path("fapi/v2/account")
  /**
   * Get current account information.
   *
   * @param recvWindow optional
   * @param timestamp
   * @return
   * @throws IOException
   * @throws BinanceException
   */
  BinancePerpetualAccountInformation account(
      @QueryParam("recvWindow") Long recvWindow,
      @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
      @HeaderParam(X_MBX_APIKEY) String apiKey,
      @QueryParam(SIGNATURE) ParamsDigest signature)
      throws IOException, BinanceException;

  @GET
  @Path("/fapi/v1/userTrades")
  /**
   * Get trades for a specific account and symbol.
   *
   * @param symbol
   * @param startTime optional
   * @param endTime optional
   * @param limit optional, default 500; max 1000.
   * @param fromId optional, tradeId to fetch from. Default gets most recent trades.
   * @param recvWindow optional
   * @param timestamp
   * @param apiKey
   * @param signature
   * @return
   * @throws IOException
   * @throws BinanceException
   */
  List<BinancePerpetualTrade> myTrades(
      @QueryParam("symbol") String symbol,
      @QueryParam("limit") Integer limit,
      @QueryParam("startTime") Long startTime,
      @QueryParam("endTime") Long endTime,
      @QueryParam("fromId") Long fromId,
      @QueryParam("recvWindow") Long recvWindow,
      @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
      @HeaderParam(X_MBX_APIKEY) String apiKey,
      @QueryParam(SIGNATURE) ParamsDigest signature)
      throws IOException, BinanceException;

  @POST
  @Path("wapi/v3/withdraw.html")
  /**
   * Submit a withdraw request.
   *
   * @param asset
   * @param address
   * @param addressTag optional for Ripple
   * @param amount
   * @param name optional, description of the address
   * @param recvWindow optional
   * @param timestamp
   * @param apiKey
   * @param signature
   * @return
   * @throws IOException
   * @throws BinanceException
   */
  WithdrawRequest withdraw(
      @QueryParam("asset") String asset,
      @QueryParam("address") String address,
      @QueryParam("addressTag") String addressTag,
      @QueryParam("amount") BigDecimal amount,
      @QueryParam("name") String name,
      @QueryParam("recvWindow") Long recvWindow,
      @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
      @HeaderParam(X_MBX_APIKEY) String apiKey,
      @QueryParam(SIGNATURE) ParamsDigest signature)
      throws IOException, BinanceException;

  @GET
  @Path("wapi/v3/depositHistory.html")
  /**
   * Fetch deposit history.
   *
   * @param asset optional
   * @param startTime optional
   * @param endTime optional
   * @param recvWindow optional
   * @param timestamp
   * @param apiKey
   * @param signature
   * @return
   * @throws IOException
   * @throws BinanceException
   */
  DepositList depositHistory(
      @QueryParam("asset") String asset,
      @QueryParam("startTime") Long startTime,
      @QueryParam("endTime") Long endTime,
      @QueryParam("recvWindow") Long recvWindow,
      @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
      @HeaderParam(X_MBX_APIKEY) String apiKey,
      @QueryParam(SIGNATURE) ParamsDigest signature)
      throws IOException, BinanceException;

  @GET
  @Path("wapi/v3/withdrawHistory.html")
  /**
   * Fetch withdraw history.
   *
   * @param asset optional
   * @param startTime optional
   * @param endTime optional
   * @param recvWindow optional
   * @param timestamp
   * @param apiKey
   * @param signature
   * @return
   * @throws IOException
   * @throws BinanceException
   */
  WithdrawList withdrawHistory(
      @QueryParam("asset") String asset,
      @QueryParam("startTime") Long startTime,
      @QueryParam("endTime") Long endTime,
      @QueryParam("recvWindow") Long recvWindow,
      @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
      @HeaderParam(X_MBX_APIKEY) String apiKey,
      @QueryParam(SIGNATURE) ParamsDigest signature)
      throws IOException, BinanceException;

  /**
   * Fetch small amounts of assets exchanged BNB records.
   *
   * @param recvWindow optional
   * @param timestamp
   * @param apiKey
   * @param signature
   * @return
   * @throws IOException
   * @throws BinanceException
   */
  @GET
  @Path("/wapi/v3/userAssetDribbletLog.html")
  AssetDribbletLogResponse userAssetDribbletLog(
      @QueryParam("recvWindow") Long recvWindow,
      @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
      @HeaderParam(X_MBX_APIKEY) String apiKey,
      @QueryParam(SIGNATURE) ParamsDigest signature)
      throws IOException, BinanceException;

  /**
   * Fetch small amounts of assets exchanged BNB records.
   *
   * @param asset optional
   * @param startTime optional
   * @param endTime optional
   * @param recvWindow optional
   * @param timestamp
   * @param apiKey
   * @param signature
   * @return
   * @throws IOException
   * @throws BinanceException
   */
  @GET
  @Path("/sapi/v1/asset/assetDividend")
  AssetDividendResponse assetDividend(
      @QueryParam("asset") String asset,
      @QueryParam("startTime") Long startTime,
      @QueryParam("endTime") Long endTime,
      @QueryParam("recvWindow") Long recvWindow,
      @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
      @HeaderParam(X_MBX_APIKEY) String apiKey,
      @QueryParam(SIGNATURE) ParamsDigest signature)
      throws IOException, BinanceException;

  @GET
  @Path("/wapi/v3/sub-account/transfer/history.html")
  TransferHistoryResponse transferHistory(
      @QueryParam("email") String email,
      @QueryParam("startTime") Long startTime,
      @QueryParam("endTime") Long endTime,
      @QueryParam("page") Integer page,
      @QueryParam("limit") Integer limit,
      @QueryParam("recvWindow") Long recvWindow,
      @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
      @HeaderParam(X_MBX_APIKEY) String apiKey,
      @QueryParam(SIGNATURE) ParamsDigest signature)
      throws IOException, BinanceException;

  @GET
  @Path("/sapi/v1/sub-account/transfer/subUserHistory")
  List<TransferSubUserHistory> transferSubUserHistory(
      @QueryParam("asset") String asset,
      @QueryParam("type") Integer type,
      @QueryParam("startTime") Long startTime,
      @QueryParam("endTime") Long endTime,
      @QueryParam("limit") Integer limit,
      @QueryParam("recvWindow") Long recvWindow,
      @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
      @HeaderParam(X_MBX_APIKEY) String apiKey,
      @QueryParam(SIGNATURE) ParamsDigest signature)
      throws IOException, BinanceException;

  @GET
  @Path("wapi/v3/depositAddress.html")
  /**
   * Fetch deposit address.
   *
   * @param asset
   * @param recvWindow
   * @param timestamp
   * @param apiKey
   * @param signature
   * @return
   * @throws IOException
   * @throws BinanceException
   */
  DepositAddress depositAddress(
      @QueryParam("asset") String asset,
      @QueryParam("recvWindow") Long recvWindow,
      @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
      @HeaderParam(X_MBX_APIKEY) String apiKey,
      @QueryParam(SIGNATURE) ParamsDigest signature)
      throws IOException, BinanceException;

  @GET
  @Path("wapi/v3/assetDetail.html")
  /**
   * Fetch asset details.
   *
   * @param recvWindow
   * @param timestamp
   * @param apiKey
   * @param signature
   * @return
   * @throws IOException
   * @throws BinanceException
   */
  AssetDetailResponse assetDetail(
      @QueryParam("recvWindow") Long recvWindow,
      @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
      @HeaderParam(X_MBX_APIKEY) String apiKey,
      @QueryParam(SIGNATURE) ParamsDigest signature)
      throws IOException, BinanceException;

  /**
   * Returns a listen key for websocket login.
   *
   * @param apiKey the api key
   * @return
   * @throws BinanceException
   * @throws IOException
   */
  @POST
  @Path("/api/v3/userDataStream")
  BinanceListenKey startUserDataStream(@HeaderParam(X_MBX_APIKEY) String apiKey)
      throws IOException, BinanceException;

  /**
   * Keeps the authenticated websocket session alive.
   *
   * @param apiKey the api key
   * @param listenKey the api secret
   * @return
   * @throws BinanceException
   * @throws IOException
   */
  @PUT
  @Path("/api/v3/userDataStream?listenKey={listenKey}")
  Map<?, ?> keepAliveUserDataStream(
      @HeaderParam(X_MBX_APIKEY) String apiKey, @PathParam("listenKey") String listenKey)
      throws IOException, BinanceException;

  /**
   * Closes the websocket authenticated connection.
   *
   * @param apiKey the api key
   * @param listenKey the api secret
   * @return
   * @throws BinanceException
   * @throws IOException
   */
  @DELETE
  @Path("/api/v3/userDataStream?listenKey={listenKey}")
  Map<?, ?> closeUserDataStream(
      @HeaderParam(X_MBX_APIKEY) String apiKey, @PathParam("listenKey") String listenKey)
      throws IOException, BinanceException;


  /**
   * Closes the websocket authenticated connection.
   *
   * @param apiKey the api key
   * symbol	STRING	NO
   * recvWindow	LONG	NO
   * timestamp	LONG	YES
   */
  @GET
  @Path("fapi/v2/positionRisk")
  List<BinancePositionInfo> positionRisk(
          @QueryParam("symbol") String symbol,
          @QueryParam("recvWindow") Long recvWindow,
          @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
          @HeaderParam(X_MBX_APIKEY) String apiKey,
          @QueryParam(SIGNATURE) ParamsDigest signature)
          throws IOException, BinanceException;

  @GET
  @Path("/fapi/v1/positionSide/dual")
  /**
   * 查询持仓模式(USER_DATA)
   */
  Boolean getPositionSideDual(@QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
                              @QueryParam("recvWindow") Long recvWindow,
                              @HeaderParam(X_MBX_APIKEY) String apiKey,
                              @QueryParam(SIGNATURE) ParamsDigest signature)
          throws IOException, BinanceException;

  @POST
  @Path("/fapi/v1/positionSide/dual")
  /**
   * 更改持仓模式(TRADE)
   */
  HashMap setPositionSideDual(@QueryParam("dualSidePosition") String dualSidePosition,
                              @QueryParam("recvWindow") Long recvWindow,
                              @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
                              @HeaderParam(X_MBX_APIKEY) String apiKey,
                              @QueryParam(SIGNATURE) ParamsDigest signature)
          throws IOException, BinanceException;

  /**
   * 变换逐全仓模式(TRADE)
   */
  @POST
  @Path("/fapi/v1/marginType")
  Binanceresult setMarginType(@FormParam("symbol") String symbol,
                              @FormParam("marginType") MarginType marginType,
                              @FormParam("recvWindow") Long recvWindow,
                              @FormParam("timestamp") SynchronizedValueFactory<Long> timestamp,
                              @HeaderParam(X_MBX_APIKEY) String apiKey,
                              @QueryParam(SIGNATURE) ParamsDigest signature)
          throws IOException, BinanceException;

  /**
   * 调整开仓杠杆(TRADE)
   */
  @POST
  @Path("/fapi/v1/leverage")
  BinancePerpetualLeverage setLeverage(@FormParam("symbol") String symbol,
                                       @FormParam("leverage") Integer leverage,
                                       @FormParam("recvWindow") Long recvWindow,
                                       @FormParam("timestamp") SynchronizedValueFactory<Long> timestamp,
                                       @HeaderParam(X_MBX_APIKEY) String apiKey,
                                       @QueryParam(SIGNATURE) ParamsDigest signature)
          throws IOException, BinanceException;


}
