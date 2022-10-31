package tide.trader.bot.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import tide.trader.bot.dto.account.BalanceDTO;
import tide.trader.bot.dto.market.TickerDTO;
import tide.trader.bot.common.mvc.BaseController;
import tide.trader.bot.service.MarketService;
import tide.trader.bot.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Controller
@RequestMapping(value = "/")
public class IndexController extends BaseController {

    /** marketService **/
    private final MarketService marketService;
    /** userService **/
    private final UserService userService;

    @Value("${trading.bot.exchange.rates.ticker}")
    private String ratesTicker;

    @GetMapping({ "", "/index"})
    public String index(Model model) {
        model.addAttribute("ratesTicker", ratesTicker);
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
            SecurityContextHolder.getContext().setAuthentication(null);
        }
        return "redirect:login?logout";
    }

    @GetMapping("/ticker24h")
    @ResponseBody
    public Set<TickerDTO> ticker24h() {
        return marketService.getTickersFromCache();
    }

    @GetMapping("/balances")
    public String balances(Model model) {
        Set<BalanceDTO> balances = new HashSet<>();
        userService.getAccountsFromCache().values().stream().forEach(account ->  account.getBalances().stream().filter(balance -> balance.getTotal().compareTo(BigDecimal.ZERO) > 0).forEach(balance -> balances.add(balance)));
        model.addAttribute("balances", balances);
        return "balances";
    }

}
