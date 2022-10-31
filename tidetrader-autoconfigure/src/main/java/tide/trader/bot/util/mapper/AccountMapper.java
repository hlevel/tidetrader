package tide.trader.bot.util.mapper;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tide.trader.bot.dto.account.AccountDTO;
import tide.trader.bot.dto.account.BalanceDTO;
import tide.trader.bot.dto.account.UserDTO;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Account mapper.
 */
@Mapper(uses = {CurrencyMapper.class})
public interface AccountMapper {

    final String DEFAULT_ACCOUNT_ID = "default_0";
    // =================================================================================================================
    // XChange to DTO.

    //@Mapping(source = "username", target = "id")
    //@Mapping(source = "wallets", target = "accounts")
    default UserDTO mapToUserDTO1(AccountInfo source) {
        /*
        String id = Optional.ofNullable(source.getUsername()).orElse(DEFAULT_ACCOUNT_ID);
        Map<String, AccountDTO> accounts = new LinkedHashMap<>();
        source.getWallets().forEach((name, wallet) -> {

            Set<BalanceDTO> balances = new HashSet<>();
            wallet.getBalances().forEach(((currency, balance) -> {
                BalanceDTO.builder().currency().total().available(balance.getAvailable()).frozen().loaned().borrowed().withdrawing().depositing().shorted().build();
            }));
            accounts.put(name, AccountDTO.builder().accountId(wallet.getId()).name(wallet.getName()).balances(null).features(null).openPositions(null).build());
        });

        return UserDTO.builder().id(id).accounts(accounts).build();
        */
        return null;
    }

    @Mapping(source = "username", target = "id")
    @Mapping(source = "wallets", target = "accounts")
    UserDTO mapToUserDTO(AccountInfo source);

    default UserDTO mapToUserIdDTO(AccountInfo source) {
        UserDTO userDTO = this.mapToUserDTO(source);
        String userId = userDTO.getId();
        if(userId == null || userId.equals("")) {
            userId = DEFAULT_ACCOUNT_ID;
        }
        Map<String, AccountDTO> accounts = new LinkedHashMap<>();
        userDTO.getAccounts().forEach((accountId, account) -> {
            if(accountId == null || accountId.equals("")) {
                accountId = DEFAULT_ACCOUNT_ID;
            }
            accounts.put(accountId, AccountDTO.builder().accountId(accountId).name(accountId).features(account.getFeatures()).balances(account.getBalances()).openPositions(account.getOpenPositions()).build());
        });
        return UserDTO.builder().id(userId).accounts(accounts).timestamp(userDTO.getTimestamp()).build();
    }


    @Mapping(source = "id", target = "accountId")
    @Mapping(target = "feature", ignore = true)
    @Mapping(target = "balances", source = "balances")
    @Mapping(target = "balance", ignore = true)
    AccountDTO mapToWalletDTO(Wallet source);

    default Set<BalanceDTO> mapToBalanceDTO(Map<Currency, Balance> source) {
        return source.values()
                .stream()
                .map(this::mapToBalanceDTO)
                .collect(Collectors.toSet());
    }

    @Mapping(source = "currency", target = "currency")
    BalanceDTO mapToBalanceDTO(Balance source);

}
