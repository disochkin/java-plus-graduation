package ru.practicum.ewm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.model.Comment;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByEventId(Long eventId, Pageable pageable);

    Long countByEventId(Long eventId);

    @Query("SELECT c.eventId, COUNT(c) FROM Comment c " +
            "WHERE c.eventId IN :ids " +
            "GROUP BY c.eventId")
    List<Object[]> countByEventIdIn(Collection<Long> ids);
}
