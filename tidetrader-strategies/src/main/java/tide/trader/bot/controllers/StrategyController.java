
package tide.trader.bot.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tide.trader.bot.domain.Strategy;
import tide.trader.bot.dto.position.PositionDTO;
import tide.trader.bot.dto.position.PositionRulesDTO;
import tide.trader.bot.dto.position.PositionStatusDTO;
import tide.trader.bot.dto.util.ColumnsDTO;
import tide.trader.bot.dto.util.CurrencyDTO;
import tide.trader.bot.dto.util.CurrencyPairDTO;
import tide.trader.bot.dto.util.GainDTO;
import tide.trader.bot.common.mvc.BaseController;
import tide.trader.bot.repository.StrategyRepository;
import tide.trader.bot.strategy.BasicTa4jCassandreStrategy;
import tide.trader.bot.strategy.internal.CassandreStrategy;
import tide.trader.bot.util.JacksonUtil;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
@RequestMapping(value = "/strategy")
public class StrategyController extends BaseController {

    /** strategyRepository **/
    private final StrategyRepository strategyRepository;
    /** context **/
    private final ApplicationContext context;

    /** tikcer **/
    @Value("${trading.bot.exchange.rates.ticker}")
    private String ratesTicker;

    @GetMapping("{strategyId}")
    public String index(@PathVariable("strategyId") String strategyId, Model model) {
        strategyRepository.findByStrategyId(strategyId).ifPresent(strategy -> {
            Map<String, BigDecimal> gains = new LinkedHashMap<>();

            CassandreStrategy cassandreStrategy = (CassandreStrategy) context.getBean(strategy.getClassName());
            CurrencyDTO gainCurrency = cassandreStrategy.getRequestedCurrencyPairs().stream().findFirst().get().getQuoteCurrency();

            cassandreStrategy.getPositions()
                    .values().stream()
                    .filter(position -> position.getCurrencyPair().getQuoteCurrency().equals(gainCurrency))
                    .filter(position -> position.getStatus() == PositionStatusDTO.CLOSED)
                    .sorted(Comparator.comparing(p -> p.getOpeningOrder().getTimestamp()))
                    .forEach(position -> {
                        String gainTime = position.getOpeningOrder().getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        GainDTO gain = position.getGain();

                        //total gain
                        if(gains.containsKey(gainTime)) {
                            gains.put(gainTime, gains.get(gainTime).add(gain.getAmount().getValue()));
                        } else {
                            gains.put(gainTime, gains.values().stream().reduce((first, last) -> last).orElse(BigDecimal.ZERO).add(gain.getAmount().getValue()));
                        }
                    });

            model.addAttribute("chart", (cassandreStrategy instanceof BasicTa4jCassandreStrategy ? true : false));
            model.addAttribute("currencyPairs", cassandreStrategy.getRequestedCurrencyPairs().stream().map(CurrencyPairDTO::toString).collect(Collectors.joining(",")));
            model.addAttribute("strategyName", strategy.getName());
            model.addAttribute("strategyClassName", strategy.getClassName());
            model.addAttribute("gainUnit", gainCurrency.toString());
            model.addAttribute("gainTimes", gains.keySet());
            model.addAttribute("gainValues", gains.values().stream().map(BigDecimal::stripTrailingZeros).collect(Collectors.toList()));
            model.addAttribute("gainTotal", gains.values().stream().reduce((first, last) -> last).orElse(BigDecimal.ZERO).stripTrailingZeros() + " " + gainCurrency.toString());
            model.addAttribute("tabTickers", cassandreStrategy.getColumnTickers());
            model.addAttribute("tabBalances", cassandreStrategy.getColumnBalances());
            model.addAttribute("tabOpenedPositions", cassandreStrategy.getColumnPositions(PositionStatusDTO.OPENED));
            model.addAttribute("tabClosedPositions", cassandreStrategy.getColumnPositions(PositionStatusDTO.CLOSED));

        });
        model.addAttribute("ratesTicker", ratesTicker);
        return "strategy";
    }

    @RequestMapping("/tickers/{strategyClassName}")
    @ResponseBody
    public List<Map<String, Object>> tickers(@PathVariable("strategyClassName") String strategyClassName) {
        CassandreStrategy cassandreStrategy = (CassandreStrategy) context.getBean(strategyClassName);
        List<Map<String, Object>> rows = new ArrayList<>();
        ColumnsDTO columnTickers = cassandreStrategy.getColumnTickers();
        columnTickers.datas().forEach(objs -> {
            Map<String, Object> row = new HashMap<>();
            for(int i=0; i < objs.size(); i++) {
                row.put(columnTickers.colNames().get(i), objs.get(i));
            }
            rows.add(row);
        });
        return rows;
    }

    @RequestMapping("/openedPositions/{strategyClassName}")
    @ResponseBody
    public List<Map<String, Object>> openedPositions(@PathVariable("strategyClassName") String strategyClassName) {
        CassandreStrategy cassandreStrategy = (CassandreStrategy) context.getBean(strategyClassName);
        List<Map<String, Object>> rows = new ArrayList<>();
        ColumnsDTO columnPositions = cassandreStrategy.getColumnPositions(PositionStatusDTO.OPENED);
        columnPositions.datas().forEach(objs -> {
            Map<String, Object> row = new HashMap<>();
            for(int i=0; i < objs.size(); i++) {
                row.put(columnPositions.colNames().get(i), objs.get(i));
            }
            rows.add(row);
        });
        return rows;
    }

    @RequestMapping("/chart/{strategyId}/{duration}/{currency}_{quote}")
    public String chart(@PathVariable("strategyId") String strategyId, @PathVariable("duration") String duration, @PathVariable("currency") String currency, @PathVariable("quote") String quote, Model model) {
        strategyRepository.findByStrategyId(strategyId).ifPresent(strategy -> {
            BasicTa4jCassandreStrategy basicTa4jStrategy = (BasicTa4jCassandreStrategy) context.getBean(strategy.getClassName());
            Duration nowDuration = Duration.parse(duration);
            CurrencyPairDTO currencyPair = new CurrencyPairDTO(currency, quote);

            List<List<Double>> rows = new ArrayList<>();
            basicTa4jStrategy.getRequestedDurationMaximumBars()
                    .stream()
                    .filter(bar -> bar.getDuration().equals(nowDuration) && bar.getCurrencyPair().equals(currencyPair))
                    .findAny()
                    .ifPresent(bar -> {
                        basicTa4jStrategy.getSeries(bar).getBarData().stream().forEach(b -> {
                            List<Double> row = new ArrayList<>();
                            row.add(Double.valueOf(Long.toString(b.getEndTime().toInstant().toEpochMilli())));
                            row.add(b.getOpenPrice().doubleValue());
                            row.add(b.getHighPrice().doubleValue());
                            row.add(b.getLowPrice().doubleValue());
                            row.add(b.getClosePrice().doubleValue());
                            rows.add(row);
                        });
                    });

            model.addAttribute("chartTitle", currencyPair.toString() + " - " + duration);
            model.addAttribute("text", currencyPair.toString() + " " + duration + "(" + rows.size() + ")");
            model.addAttribute("currencyPair", currencyPair.toString());
            model.addAttribute("tickers", JacksonUtil.toJSONString(rows));
        });
        return "chart";
    }

    @RequestMapping("/close/{strategyId}/{positionId}/{currency}_{quote}")
    @ResponseBody
    public ResponseEntity<Map<String,String>> close(@PathVariable("strategyId") String strategyId, @PathVariable("positionId") String positionId, @PathVariable("currency") String currency, @PathVariable("quote") String quote) {
        final CurrencyPairDTO currencyPair = new CurrencyPairDTO(currency, quote);
        Map<String, String> result = new HashMap<String, String>();
        result.put("code", "error");
        Optional<Strategy> strategy = strategyRepository.findByStrategyId(strategyId);
        if(strategy.isPresent()) {
            CassandreStrategy cassandreStrategy = (CassandreStrategy) context.getBean(strategy.get().getClassName());
            Optional<PositionDTO> positionDTO = cassandreStrategy.getPositions(currencyPair).values().stream().filter(p -> String.valueOf(p.getPositionId()).equals(positionId)).findFirst();
            if(positionDTO.isPresent()) {
                cassandreStrategy.closePosition(positionDTO.get().getUid(), new CurrencyPairDTO(currency, quote), "Manual exit");
                result.put("code", "ok");
            }
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /*@RequestMapping("/testOrder")
    @ResponseBody
    public ResponseEntity<Map<String,String>> testOrder() {
        Map<String, String> result = new HashMap<String, String>();
        try{
            final CurrencyPairDTO currencyPair = new CurrencyPairDTO("BTC", "USDT");
            result.put("code", "error");
            Optional<Strategy> strategy = strategyRepository.findByStrategyId("3");
            if(strategy.isPresent()) {
                PositionRulesDTO rules = PositionRulesDTO.builder()
                        .stopGainPercentage(200f)
                        .stopLossPercentage(50f)
                        .build();
                CassandreStrategy cassandreStrategy = (CassandreStrategy) context.getBean(strategy.get().getClassName());
                cassandreStrategy.createLongPosition(currencyPair, new BigDecimal("0.001"), rules);
                result.put("code", "ok");
            }
        }catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }*/

}
