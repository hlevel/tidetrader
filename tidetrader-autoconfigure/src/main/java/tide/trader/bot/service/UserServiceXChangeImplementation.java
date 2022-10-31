package tide.trader.bot.service;

import tide.trader.bot.dto.account.AccountDTO;
import tide.trader.bot.dto.account.UserDTO;
import tide.trader.bot.util.base.Base;
import tide.trader.bot.util.base.service.BaseService;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User service - XChange implementation of {@link UserService}.
 */
public class UserServiceXChangeImplementation extends BaseService implements UserService {

    /** XChange service. */
    private final org.knowm.xchange.service.account.AccountService xChangeAccountService;

    /** Cached reply from Exchange. */
    private final Map<String, AccountDTO> cachedReply = new ConcurrentHashMap<>();

    /**
     * Constructor.
     *
     * @param rate                     rate in ms
     * @param newXChangeAccountService xchange account service
     */
    public UserServiceXChangeImplementation(final long rate, final org.knowm.xchange.service.account.AccountService newXChangeAccountService) {
        super(rate);
        this.xChangeAccountService = newXChangeAccountService;
    }

    @Override
    @SuppressWarnings("checkstyle:DesignForExtension")
    public Optional<UserDTO> getUser() {
        try {
            // Consume a token from the token bucket.
            // If a token is not available this method will block until the refill adds one to the bucket.
            bucket.asBlocking().consume(1);

            logger.debug("Retrieving account information");
            final UserDTO user = Base.ACCOUNT_MAPPER.mapToUserIdDTO(xChangeAccountService.getAccountInfo());
            logger.debug("Account information retrieved " + user);
            // Update the cached reply.
            if (user != null && user.getAccounts() != null) {
                user.getAccounts()
                        .values()
                        .stream()
                        .filter(accountDTO -> accountDTO.getAccountId() != null)
                        .forEach(accountDTO -> cachedReply.put(accountDTO.getAccountId(), accountDTO));
            }
            return Optional.ofNullable(user);
        } catch (IOException e) {
            logger.error("Error retrieving account information: {}", e.getMessage());
            return Optional.empty();
        } catch (InterruptedException e) {
            return Optional.empty();
        }
    }

    @Override
    @SuppressWarnings("checkstyle:DesignForExtension")
    public Map<String, AccountDTO> getAccounts() {
        final Optional<UserDTO> user = getUser();
        if (user.isPresent()) {
            return user.get().getAccounts();
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    public final Map<String, AccountDTO> getAccountsFromCache() {
        // If cache is empty, we ask the exchange.
        if (cachedReply.isEmpty()) {
            return getAccounts();
        } else {
            return cachedReply;
        }
    }

}
