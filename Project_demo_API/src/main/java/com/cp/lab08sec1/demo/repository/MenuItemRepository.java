package com.cp.lab08sec1.demo.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.cp.lab08sec1.demo.model.MenuItem;

// 💡 JpaRepository สืบทอดมาจาก CrudRepository 
// และเป็นมาตรฐานสำหรับการใช้งาน JPA ใน Spring Boot
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    // JpaRepository มีเมธอดพื้นฐานให้ใช้แล้ว เช่น:
    // - Optional<MenuItem> findById(Long id); // ใช้สำหรับดึง MenuItem (ใน DataServiceClient)
    // - List<MenuItem> findAll();
    // - <S extends MenuItem> S save(S entity);
    
    // หากต้องการค้นหาเพิ่มเติม เช่น by name:
    // Optional<MenuItem> findByName(String name);
}
