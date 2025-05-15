package com.fpt.bbusbe.repository;

import com.fpt.bbusbe.model.entity.User;
import com.fpt.bbusbe.model.entity.UserHasRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface UserHasRoleRepository extends JpaRepository<UserHasRole, UUID> {
}
