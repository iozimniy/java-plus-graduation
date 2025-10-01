package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.action.UserAction;
import ru.practicum.model.action.UserActionId;

import java.util.List;

public interface UserActionRepository extends JpaRepository<UserAction, UserActionId> {
    @Query("SELECT acts.id.eventId, SUM(acts.weight) FROM UserAction acts WHERE acts.id.eventId IN :ids GROUP BY acts.id.eventId")
    List<Object[]> sumWeightByEventsIds(@Param("ids") List<Long> ids);

    @Query(value = "Select * FROM user_action WHERE user_id = :id ORDER BY updated DESC LIMIT :limit",
            nativeQuery = true)
    List<UserAction> findRecentActionsByUserId(@Param("id") Long id, @Param("limit") int limit);
}
