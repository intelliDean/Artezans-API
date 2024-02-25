package com.api.artezans.listings.data.repositories;

import com.api.artezans.listings.data.models.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {
    @Query("""
            select list from Listing list
            where list.serviceProvider.id = :serviceProviderId and list.deleted = false
            """)
    List<Listing> findAllByServiceProvider_Id(Long serviceProviderId);

    @Query(value = "select lst from Listing lst where lst.deleted = false ")
    List<Listing> findAllUndeletedListings();

    @Query("""
            select list from Listing list
            where list.id = :listingId and list.deleted = false
            """)
    Optional<Listing> findActiveListingById(Long listingId);

    @Query("""
            select list from Listing list
            where list.address.id = :addressId and list.deleted = false
            """)
    Optional<Listing> findActiveListingByAddressId(Long addressId);

    List<Listing> findByBusinessNameIgnoreCase(String businessName);

    @Query(value = """
            select lst from Listing lst
            where lower(lst.serviceName) = lower(:serviceName)
            """)
    List<Listing> findByServiceNameIgnoreCase(String serviceName);

    List<Listing> findByAddressStateIgnoreCase(String state);

    @Query(value = """
            SELECT listing FROM Listing listing
            WHERE lower(listing.serviceName) = lower(:serviceName)
            AND (lower(listing.address.streetName) = lower(:location) OR
                 lower(listing.address.suburb) = lower(:location) OR
                 lower(listing.address.state) = lower(:location) OR
                 (:location IS NULL OR :location = ''))
            """)
    List<Listing> findByLocation(String serviceName, String location);

    @Query(value = """
            select lst from Listing lst
            where lst.pricing = :price AND lst.deleted = false
            """)
    List<Listing> findListingByPricing(BigDecimal price);


// TODO: 20-Sep-23 This is meant to work, it is working in pg_Admin but not in Springboot
//    @Query(value = """
//            select lst from listing lst inner join address a on lst.address_id = a.id
//            where address_with_weight @@ to_tsquery(?1)
//            order by ts_rank(address_with_weight, plainto_tsquery(?1)) desc
//            """, nativeQuery = true)
//    List<Listing> findByLocation(String location);

    List<Listing> findByServiceNameIgnoreCaseAndAddressStateIgnoreCase(String serviceName, String state);
}

