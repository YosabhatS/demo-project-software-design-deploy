package com.cp.lab08sec1.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cp.lab08sec1.demo.model.TableInfo;
import com.cp.lab08sec1.demo.repository.TableInfoRepository;
import com.cp.lab08sec1.demo.service.ResourceNotFoundException;

@RestController
@RequestMapping("/api/tables")
public class TableInfoController {

    private final TableInfoRepository tableInfoRepository;

    public TableInfoController(TableInfoRepository tableInfoRepository) {
        this.tableInfoRepository = tableInfoRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TableInfo> getTableInfo(@PathVariable Long id) {
        TableInfo table = tableInfoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table ID " + id + " not found"));
        return ResponseEntity.ok(table);
    }
    
    
}
