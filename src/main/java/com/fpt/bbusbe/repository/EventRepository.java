package com.fpt.bbusbe.repository;

import com.fpt.bbusbe.model.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    Event findByName(String name);
}
