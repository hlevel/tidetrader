package org.knowm.xchange.binance.perpetualswap.service;

import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.account.BinancePerpetualAccountInformation;
import org.knowm.xchange.binance.perpetualswap.BinancePerpetualAuthenticated;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.OpenPosition;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.service.account.AccountService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class BinancePerpetualAccountService extends BinancePerpetualAccountServiceRaw implements AccountService {
    public BinancePerpetualAccountService(BinanceExchange exchange, BinancePerpetualAuthenticated binance, ResilienceRegistries resilienceRegistries) {
        super(exchange, binance, resilienceRegistries);
    }

    @Override
    public AccountInfo getAccountInfo() throws IOException {
        BinancePerpetualAccountInformation acc = account();

        List<Balance> balances = acc.getBalancePerpetuals()
                .stream()
                .filter(perpetual -> acc.getBalancePositions().stream().filter(position -> position.getSymbol().endsWith(perpetual.getCurrency().getCurrencyCode())).findFirst().isPresent())
                .map(bp -> new Balance(bp.getCurrency(), bp.getAvailableBalance(), bp.getAvailableBalance(), BigDecimal.ZERO)).collect(Collectors.toList());

        Set<OpenPosition> balancePositions = balances.stream()
                .flatMap(balance -> acc.getBalancePositions()
                        .stream()
                        .filter(position -> position.getPositionAmt().compareTo(BigDecimal.ZERO) != 0)
                        .filter(position -> position.getSymbol().endsWith(balance.getCurrency().getCurrencyCode()))
                        .map(position -> {
                            String currencyCode = position.getSymbol().substring(0, position.getSymbol().lastIndexOf(balance.getCurrency().getCurrencyCode()));
                            return new OpenPosition(new CurrencyPair(Currency.getInstance(currencyCode), balance.getCurrency()), position.getPositionAmt().compareTo(BigDecimal.ZERO) > 0 ? OpenPosition.Type.LONG : OpenPosition.Type.SHORT, position.getPositionAmt().abs(), position.getEntryPrice(), BigDecimal.ZERO, position.getMaintMargin());
                        })).collect(Collectors.toSet());

        Wallet wallet = new Wallet("perpetual", "perpetual", balances, Collections.emptySet(), BigDecimal.ZERO, BigDecimal.ZERO, balancePositions);
        return new AccountInfo(new Date(acc.getUpdateTime()), wallet);
    }

    @Override
    public OpenPosition getOpenPosition(CurrencyPair currencyPair, Object... args) throws IOException {
        return getPositionRisk(currencyPair, null);
    }
}
