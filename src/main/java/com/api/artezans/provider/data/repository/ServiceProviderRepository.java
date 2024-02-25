package com.api.artezans.provider.data.repository;


import com.api.artezans.provider.data.model.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {
    Optional<ServiceProvider> findServiceProviderByUserEmailAddress(String emailAddress);
    boolean existsByUserEmailAddressIgnoreCase(String emailAddress);
}