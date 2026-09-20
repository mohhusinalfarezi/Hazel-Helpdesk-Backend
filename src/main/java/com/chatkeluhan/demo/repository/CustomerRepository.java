package com.chatkeluhan.demo.repository;

import com.chatkeluhan.demo.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    // Spring Data JPA otomatis membuatkan fungsi pencarian berdasarkan nomor HP
    Customer findByContactNumber(String contactNumber);
}