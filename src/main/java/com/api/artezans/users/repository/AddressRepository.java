package com.api.artezans.users.repository;


import com.api.artezans.users.models.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {

        @Query(value = """
            select adr from Address adr
            where address_with_weight @@ to_tsquery(?1)
            order by ts_rank(address_with_weight, plainto_tsquery(?1)) desc
            """, nativeQuery = true)
        List<Address> findAddressesByLocation(String location);
}
