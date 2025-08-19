package com.churchapp.repository;

import com.churchapp.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    
    List<Event> findByNameContainingIgnoreCase(String name);
    
    List<Event> findByStartDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT e FROM Event e WHERE e.startDate >= :currentDate ORDER BY e.startDate ASC")
    List<Event> findUpcomingEvents(@Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT e FROM Event e WHERE e.endDate < :currentDate ORDER BY e.startDate DESC")
    List<Event> findPastEvents(@Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT COUNT(er) FROM EventRegistration er WHERE er.event.id = :eventId")
    Long getRegistrationCountByEventId(@Param("eventId") Integer eventId);
}
