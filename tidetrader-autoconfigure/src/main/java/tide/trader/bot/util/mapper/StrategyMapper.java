package tide.trader.bot.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tide.trader.bot.domain.Strategy;
import tide.trader.bot.dto.strategy.StrategyDTO;

/**
 * Strategy mapper.
 */
@Mapper
public interface StrategyMapper {

    // =================================================================================================================
    // DTO to Domain.

    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "updatedOn", ignore = true)
    Strategy mapToStrategy(StrategyDTO source);

    // =================================================================================================================
    // Domain to DTO.

    StrategyDTO mapToStrategyDTO(Strategy source);

}
