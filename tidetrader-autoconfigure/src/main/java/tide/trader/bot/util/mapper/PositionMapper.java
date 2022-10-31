package tide.trader.bot.util.mapper;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.account.OpenPosition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import tide.trader.bot.domain.Position;
import tide.trader.bot.dto.position.OpenPositionDTO;
import tide.trader.bot.dto.position.PositionDTO;
import tide.trader.bot.dto.position.PositionRulesDTO;
import tide.trader.bot.dto.util.CurrencyPairDTO;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

/**
 * Position mapper.
 */
@Mapper(uses = {CurrencyMapper.class, OrderMapper.class, UtilMapper.class, StrategyMapper.class}, nullValuePropertyMappingStrategy = IGNORE)
public interface PositionMapper {

    // =================================================================================================================
    // DTO to Domain.

    @Mapping(source = "currencyPair.baseCurrencyPrecision", target = "baseCurrencyPrecision")
    @Mapping(source = "currencyPair.quoteCurrencyPrecision", target = "quoteCurrencyPrecision")
    @Mapping(source = "rules.stopGainPercentage", target = "stopGainPercentageRule")
    @Mapping(source = "rules.stopLossPercentage", target = "stopLossPercentageRule")
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "updatedOn", ignore = true)
    Position mapToPosition(PositionDTO source);

    @Mapping(target = "uid", ignore = true)
    @Mapping(target = "strategy", ignore = true)
    @Mapping(target = "positionId", ignore = true)
    @Mapping(target = "stopGainPercentageRule", ignore = true)
    @Mapping(target = "stopLossPercentageRule", ignore = true)
    @Mapping(target = "forceClosing", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "updatedOn", ignore = true)
    @Mapping(target = "baseCurrencyPrecision", ignore = true)
    @Mapping(target = "quoteCurrencyPrecision", ignore = true)
    void updatePosition(PositionDTO source, @MappingTarget Position target);

    // =================================================================================================================
    // Domain to DTO.

    @Mapping(source = "source", target = "currencyPair", qualifiedByName = "mapToPositionDTOCurrencyPair")
    @Mapping(source = "source", target = "rules")
    @Mapping(source = "exitReason", target = "exitReason")
    PositionDTO mapToPositionDTO(Position source);

    @Named("mapToPositionDTOCurrencyPair")
    default CurrencyPairDTO mapToPositionDTOCurrencyPair(Position source) {
        return new CurrencyPairDTO(
                source.getCurrencyPair().split(CurrencyPairDTO.CURRENCY_PAIR_SEPARATOR)[0],
                source.getCurrencyPair().split(CurrencyPairDTO.CURRENCY_PAIR_SEPARATOR)[1],
                source.getBaseCurrencyPrecision(),
                source.getQuoteCurrencyPrecision());
    }

    default PositionRulesDTO mapToPositionRulesDTO(Position source) {
        PositionRulesDTO rules = PositionRulesDTO.builder().build();
        boolean stopGainRuleSet = source.getStopGainPercentageRule() != null;
        boolean stopLossRuleSet = source.getStopLossPercentageRule() != null;
        // Two rules set.
        if (stopGainRuleSet && stopLossRuleSet) {
            rules = PositionRulesDTO.builder()
                    .stopGainPercentage(source.getStopGainPercentageRule())
                    .stopLossPercentage(source.getStopLossPercentageRule())
                    .build();
        }
        // Only a stop gain set.
        if (stopGainRuleSet && !stopLossRuleSet) {
            rules = PositionRulesDTO.builder()
                    .stopGainPercentage(source.getStopGainPercentageRule())
                    .build();
        }
        // Only a stop loss set.
        if (!stopGainRuleSet && stopLossRuleSet) {
            rules = PositionRulesDTO.builder()
                    .stopLossPercentage(source.getStopLossPercentageRule())
                    .build();
        }
        return rules;
    }

    default OpenPosition mapToOpenPosition(OpenPositionDTO source) {
        return new OpenPosition.Builder()
                .currencyPair(new CurrencyPair(source.getCurrencyPair().getBaseCurrency().getSymbol(), source.getCurrencyPair().getQuoteCurrency().getSymbol()))
                .price(source.getPrice())
                .amount(source.getAmount())
                .type(source.getType())
                .liquidationPrice(source.getLiquidationPrice())
                .margin(source.getMargin())
                .build();
    }

}
