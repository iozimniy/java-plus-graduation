package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.UserAction;
import ru.practicum.model.UserActionId;

import java.util.List;

public interface UserActionRepository extends JpaRepository<UserAction, UserActionId> {
    @Query("SELECT acts.id.eventId, SUM(acts.weight) FROM UserAction acts WHERE acts.id.eventId IN :ids GROUP BY acts.id.eventId")
    List<Object[]> sumWeightByEventsIds(@Param("ids") List<Long> ids);
}
