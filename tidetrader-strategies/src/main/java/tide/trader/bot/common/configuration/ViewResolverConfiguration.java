package tide.trader.bot.common.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import tide.trader.bot.dto.strategy.StrategyDTO;
import tide.trader.bot.strategy.internal.CassandreStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Configuration
public class ViewResolverConfiguration {

    /** thymeleaf **/
    private final ThymeleafViewResolver thymeleafViewResolver;
    /** context **/
    private final ApplicationContext context;

    @Bean
    public void globalResolver(){
        List<StrategyDTO> strategyList = context.getBeansWithAnnotation(tide.trader.bot.strategy.CassandreStrategy.class)
                .values()
                .stream()
                .map(o -> (CassandreStrategy) o)
                .map(strategy -> strategy.getConfiguration().getStrategyDTO())
                .collect(Collectors.toList());

        Map<String, List<StrategyDTO>> map = new HashMap<>();
        map.put("strategys", strategyList);
        thymeleafViewResolver.setStaticVariables(map);
    }

}
