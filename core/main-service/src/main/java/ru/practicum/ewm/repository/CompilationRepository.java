package ru.practicum.ewm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.compilation.Compilation;

import java.util.List;
import java.util.Set;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    Page<Compilation> findByPinned(Boolean pinned, Pageable pageable);

    @Query("select distinct c from Compilation c join fetch c.events where c.id in :ids")
    List<Compilation> findAllWithEvents(@Param("ids") Set<Long> ids);

}