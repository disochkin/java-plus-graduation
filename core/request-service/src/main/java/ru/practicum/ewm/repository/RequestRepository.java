package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.dto.request.RequestStatus;
import ru.practicum.ewm.model.Request;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByRequesterId(Long requesterId);

    List<Request> findByEventId(Long eventId);
//
    List<Request> findByEventIdAndStatus(Long eventId, RequestStatus status);

    Optional<Request> findByRequesterIdAndEventId(Long requesterId, Long eventId);

    @Query("SELECT COUNT(r) FROM Request r WHERE r.eventId = :eventId AND r.status = 'CONFIRMED'")
    Long countConfirmedRequestsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT r.eventId, COUNT(r) FROM Request r " +
            "WHERE r.eventId IN :eventIds AND r.status = 'CONFIRMED' " +
            "GROUP BY r.eventId")
    List<Object[]> countConfirmedRequestsByEventIds(@Param("eventIds") List<Long> eventIds);
//
    List<Request> findByIdInAndEventId(List<Long> ids, Long eventId);
//
//    List<Request> findByEventIdAndRequesterId(Long eventId, Long requesterId);
//
//    List<Request> findAllByIdInOrderByCreated(List<Long> requestIds);
}