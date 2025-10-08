package com.cp.lab08sec1.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cp.lab08sec1.demo.model.TableInfo;

public interface TableInfoRepository extends JpaRepository<TableInfo, Long> {
    Optional<TableInfo> findByTableNumber(int tableNumber);
}
