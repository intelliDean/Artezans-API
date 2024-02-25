package com.api.artezans.customer.data.repository;

import com.api.artezans.customer.data.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findCustomerByUserEmailAddress(String email);
    @Query(value = """
            select cust
            from customer cust inner join users u on cust.user_id = u.id
            where user_with_weight @@ to_tsquery(?1)
            order by ts_rank(user_with_weight, plainto_tsquery(?1)) desc
            """, nativeQuery = true)
    List<Customer> findCustomerByKeyword(String keyword);
}