package tide.trader.bot.batch;

import lombok.RequiredArgsConstructor;
import tide.trader.bot.domain.Position;
import tide.trader.bot.dto.position.PositionDTO;
import tide.trader.bot.repository.PositionRepository;
import tide.trader.bot.util.base.batch.BaseFlux;
import tide.trader.bot.util.base.Base;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Position flux - push {@link PositionDTO}.
 * Two methods override from super class:
 * - getNewValues(): positions are only created inside cassandre, so we don't need to get new values from outside.
 * - saveValues(): update positions when they are sent to this flux (they are not created in the flux).
 * To get a deep understanding of how it works, read the documentation of {@link BaseFlux}.
 */
@RequiredArgsConstructor
public class PositionFlux extends BaseFlux<PositionDTO> {

    /** Position repository. */
    private final PositionRepository positionRepository;

    @Override
    protected final Set<PositionDTO> saveValues(final Set<PositionDTO> newValues) {
        Set<Position> positions = new LinkedHashSet<>();

        // We save every position sent to the flux.
        newValues.stream().peek(positionDTO -> logger.debug("Checking position in database: {}", positionDTO)).forEach(positionDTO -> {
            final Optional<Position> position = positionRepository.findById(positionDTO.getUid());
            if (position.isPresent()) {
                // If the position is in database (which should be always true), we update it.
                Base.POSITION_MAPPER.updatePosition(positionDTO, position.get());
                positions.add(positionRepository.save(position.get()));
                logger.debug("Updating position in database: {}", positionDTO);
            } else {
                logger.error("Position {} not found in database:", positionDTO.getUid());
            }
        });

        return positions.stream()
                .map(Base.POSITION_MAPPER::mapToPositionDTO)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
