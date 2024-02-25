package com.api.artezans.users.repository;

import com.api.artezans.users.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByEmailAddressIgnoreCase(String emailAddress);

    boolean existsByEmailAddress(String email);
}
