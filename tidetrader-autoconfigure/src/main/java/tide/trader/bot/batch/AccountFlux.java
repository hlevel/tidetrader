package tide.trader.bot.batch;

import lombok.RequiredArgsConstructor;
import tide.trader.bot.dto.account.AccountDTO;
import tide.trader.bot.service.UserService;
import tide.trader.bot.util.base.batch.BaseFlux;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Account flux - push {@link AccountDTO}.
 * Two methods override from super class:
 * - getNewValues(): calling user service to retrieve accounts values from exchange.
 * - saveValues(): not implemented as we don't store accounts data in database.
 * To get a deep understanding of how it works, read the documentation of {@link BaseFlux}.
 */
@RequiredArgsConstructor
public class AccountFlux extends BaseFlux<AccountDTO> {

    /** User service. */
    private final UserService userService;

    /** Previous values. */
    private final Map<String, AccountDTO> previousValues = new ConcurrentHashMap<>();
    //private Map<String, AccountDTO> previousValues = new LinkedHashMap<>();


    @Override
    protected final Set<AccountDTO> getNewValues() {
        logger.debug("Retrieving accounts information from exchange");
        /*Set<AccountDTO> newValues = new LinkedHashSet<>();

        // Calling the service and treating results.

        userService.getUser().ifPresent(user -> {
            // For each account, we check if value changed.
            user.getAccounts().forEach((accountId, account) -> {
                logger.debug("Checking account: {}", accountId);
                if (previousValues.containsKey(accountId)) {
                    // If the account is already in the previous values, check if the balances changed.
                    if (!account.equals(previousValues.get(accountId))) {
                        logger.debug("Account {} has changed to: {}", accountId, account);
                        newValues.add(account);
                    }
                } else {
                    // If it's a new account, we add it.
                    logger.debug("New account: {}", account);
                    newValues.add(account);
                }
            });
            previousValues = user.getAccounts();
        });
        return newValues;
*/
/*
        Set<AccountDTO> newValues = new LinkedHashSet<>();
        userService.getAccounts().values().forEach(accountDTO -> {
            logger.debug("Retrieved account from exchange: {}", accountDTO);
            String accountId = accountDTO.getAccountId();
            if(previousValues.containsKey(accountId)) {
                // If the account is already in the previous values, check if the balances changed.
                if (!accountDTO.equals(previousValues.get(accountId))) {
                    logger.debug("Account {} has changed to: {}", accountId, accountDTO);
                    newValues.add(accountDTO);
                }
            } else {
                // If it's a new account, we add it.
                logger.debug("New account: {}", accountDTO);
                previousValues.put(accountId, accountDTO);
                newValues.add(accountDTO);
            }
        });
        return newValues;
        */
        return userService.getAccounts()
                .values()
                .stream()
                .peek(accountDTO -> logger.debug("Retrieved account from exchange: {}", accountDTO))
                // We consider that we have a new value to send to strategies in two cases:
                // - New value (AccountDTO) is already in previous values but balances are different.
                // - New value (AccountDTO) doesn't exist at all in previous values.
                .filter(accountDTO -> !Objects.equals(accountDTO, previousValues.get(accountDTO.getAccountId())))
                .peek(accountDTO -> logger.debug("Updated account: {}", accountDTO))
                // We add or replace the new value in the previous values.
                .peek(accountDTO -> previousValues.put(accountDTO.getAccountId(), accountDTO))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
