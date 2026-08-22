package com.example.hinchmart.repository;

import com.example.hinchmart.entity.Rfq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RfqRepository extends JpaRepository<Rfq, Long> {

    List<Rfq> findByBuyerId(Long buyerId);
}
