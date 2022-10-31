package org.knowm.xchange.binance.perpetualswap.service;

import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.BinanceException;
import org.knowm.xchange.binance.dto.account.BinancePerpetualAccountInformation;
import org.knowm.xchange.binance.perpetualswap.BinancePerpetualAuthenticated;
import org.knowm.xchange.binance.dto.marketdata.BinancePerpetualBalance;
import org.knowm.xchange.binance.dto.marketdata.BinancePositionInfo;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.account.OpenPosition;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.knowm.xchange.binance.BinanceResilience.REQUEST_WEIGHT_RATE_LIMITER;

public class BinancePerpetualAccountServiceRaw extends BinancePerpetualBaseService{
    protected BinancePerpetualAccountServiceRaw(BinanceExchange exchange, BinancePerpetualAuthenticated binance, ResilienceRegistries resilienceRegistries) {
        super(exchange, binance, resilienceRegistries);
    }
    //public final static String SWAP_ASSET = "USDT";

    public BinancePerpetualAccountInformation account() throws BinanceException, IOException {
        return decorateApiCall(
                () -> binance.account(getRecvWindow(), getTimestampFactory(), apiKey, signatureCreator))
                .withRetry(retry("account"))
                .withRateLimiter(rateLimiter(REQUEST_WEIGHT_RATE_LIMITER), 5)
                .call();
    }

    public List<BinancePerpetualBalance> balance() throws IOException {
        return binance.balance(null, getTimestampFactory(),apiKey,signatureCreator);
    }

    public OpenPosition getPositionRisk(CurrencyPair currencyPair, Long recvWindow) throws IOException {
        //BinanceAccountInformation
        List<BinancePositionInfo> list = binance.positionRisk(currencyPair.getParsing(""),recvWindow,getTimestampFactory(), apiKey, signatureCreator);
        if(list.size() == 1){
            BinancePositionInfo binancePositionInfo = list.get(0);

            if(binancePositionInfo.getPositionAmt().compareTo(BigDecimal.ZERO) != 0) {
                OpenPosition.Builder builder = new OpenPosition.Builder();
                builder.currencyPair(currencyPair);
                builder.amount(binancePositionInfo.getPositionAmt().abs());
                builder.margin(BigDecimal.ZERO);
                builder.liquidationPrice(binancePositionInfo.getLiquidationPrice());
                builder.type(binancePositionInfo.getPositionAmt().compareTo(new BigDecimal(0)) > 0 ? OpenPosition.Type.LONG : OpenPosition.Type.SHORT);
                builder.price(binancePositionInfo.getEntryPrice());
                return builder.build();
            }

        }
        return null;
    }

}
