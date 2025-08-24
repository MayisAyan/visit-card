package com.visitcard.controller;

import com.visitcard.entity.Admin;
import com.visitcard.entity.Company;
import com.visitcard.service.AdminService;
import com.visitcard.service.CompanyService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.visitcard.dto.CompanyDto;


import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private  CompanyService companyService;

    @Autowired
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/by-admin/{adminId}")
    public ResponseEntity<List<Company>> getCompaniesByAdmin(@PathVariable Long adminId) {
        List<Company> companies = companyService.getCompaniesByAdminId(adminId);
        return companies.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(companies);
    }

    @PostMapping("/create")
    public ResponseEntity<Company> createCompany(@RequestBody Company company, @RequestParam String adminLogin) {
        try {
            Admin admin = adminService.findByLogin(adminLogin);
            if (admin == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            Company saved = companyService.createCompany(company, admin);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyDto> getCompany(@PathVariable Long id) {
        try {
            CompanyDto dto = companyService.getCompanyDtoById(id);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<Company>> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyDto> updateCompany(@PathVariable Long id, @RequestBody Company company) {
        try {
            Company updated = companyService.updateCompany(id, company);
            System.out.println("Company staff after update:");
            updated.getStaffList().forEach(s ->
                    System.out.println(" - " + s.getName() + " (" + s.getPosition() + ")")
            );
            CompanyDto dto = companyService.getCompanyDtoById(id);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCompany(@PathVariable Long id) {
        try {
            companyService.deleteCompany(id);
            return ResponseEntity.ok("Company deleted");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete company: " + e.getMessage());
        }
    }

}
