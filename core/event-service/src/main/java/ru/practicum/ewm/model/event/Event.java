package ru.practicum.ewm.model.event;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.practicum.ewm.dto.event.EventState;
import ru.practicum.ewm.model.category.Category;

import java.time.LocalDateTime;

import static ru.practicum.ewm.dto.event.EventState.PENDING;

@Entity
@Table(name = "events")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "annotation", length = 2000)
    private String annotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    private Category category;

    @Column(name = "created_on", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdOn;

    @Column(name = "description", length = 7000)
    private String description;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @Column(name = "initiator_id")
    private Long initiatorId;

    @Embedded
    private Location location;

    @Column(name = "paid")
    private Boolean paid;

    @Column(name = "participant_limit")
    private Integer participantLimit;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Column(name = "request_moderation")
    private Boolean requestModeration;

    @Column(name = "rating", nullable = false)
    @Builder.Default
    private Double rating = 0.0;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private EventState state = PENDING;

    @Column(name = "title")
    private String title;
}
