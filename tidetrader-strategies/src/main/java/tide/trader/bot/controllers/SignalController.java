package tide.trader.bot.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tide.trader.bot.domain.Signal;
import tide.trader.bot.domain.Strategy;
import tide.trader.bot.dto.TvInstructDTO;
import tide.trader.bot.dto.trade.OrderTypeDTO;
import tide.trader.bot.dto.trade.SignalDTO;
import tide.trader.bot.dto.trade.SignalStatusDTO;
import tide.trader.bot.dto.trade.SideDTO;
import tide.trader.bot.dto.util.CurrencyAmountDTO;
import tide.trader.bot.dto.util.CurrencyPairDTO;
import tide.trader.bot.common.mvc.BaseController;
import tide.trader.bot.repository.SignalRepository;
import tide.trader.bot.repository.StrategyRepository;

import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/signal")
public class SignalController extends BaseController {

    /** strategyRepository **/
    private final StrategyRepository strategyRepository;
    /** signalRepository **/
    private final SignalRepository signalRepository;

    @PostMapping(value = "/tv/{currency}/{quote}", produces = { "application/json;charset=UTF-8" })
    @ResponseBody
    public ResponseEntity<?> tv(@PathVariable String currency, @PathVariable String quote, @RequestBody TvInstructDTO instruct) {
        CurrencyPairDTO currencyPair = new CurrencyPairDTO(currency, quote);
        OrderTypeDTO type = instruct.getType().equalsIgnoreCase("sell") ? OrderTypeDTO.ASK : OrderTypeDTO.BID;
        SideDTO side = SideDTO.valueOf(instruct.getSide().toUpperCase());
        if(currencyPair == null || side == null || side == null) {
            return new ResponseEntity<>("The required parameter is null and cannot be executed", HttpStatus.BAD_REQUEST);
        }

        //Determine whether the policy exists
        Optional<Strategy> strategyGet = strategyRepository.findByName(instruct.getName());
        if(strategyGet.isEmpty()) {
            return new ResponseEntity<>("Strategy does not exist", HttpStatus.BAD_REQUEST);
        }

        //Judge whether the signal is received repeatedly
        Optional<Signal> signalGet = signalRepository.findFirstByStrategyStrategyIdAndCurrencyPairAndStatusAndTypeOrderByCreatedOnAsc(strategyGet.get().getStrategyId(), currencyPair.toString(), SignalStatusDTO.ACTIVE, side);
        if(signalGet.isPresent()) {
            return new ResponseEntity<>("Repeating signal", HttpStatus.BAD_REQUEST);
        }

        //Save signal results
        SignalDTO signal = SignalDTO.builder()
                .strategy(STRATEGY_MAPPER.mapToStrategyDTO(strategyGet.get()))
                .currencyPair(currencyPair)
                .price(CurrencyAmountDTO.builder().currency(currencyPair.getBaseCurrency()).value(instruct.getPrice()).build())
                .type(type)
                .side(side)
                .status(SignalStatusDTO.ACTIVE)
                .build();
        signalRepository.save(TRADE_MAPPER.mapToSignal(signal));
        return new ResponseEntity<>("Successfully received signal", HttpStatus.OK);
    }

}
