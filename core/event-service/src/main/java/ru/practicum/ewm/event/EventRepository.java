package ru.practicum.ewm.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.ewm.dto.event.EventState;
import ru.practicum.ewm.model.event.Event;

import java.util.Collection;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    Collection<Event> findAllByInitiatorId(Long id);

    Optional<Event> findByIdAndState(Long id, EventState state);

    boolean existsByCategoryId(Long categoryId);

}
