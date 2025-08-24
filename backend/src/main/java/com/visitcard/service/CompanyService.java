package com.visitcard.service;

import com.visitcard.dto.CompanyDto;
import com.visitcard.dto.StaffDto;
import com.visitcard.entity.Admin;
import com.visitcard.entity.Company;
import com.visitcard.entity.Staff;
import com.visitcard.repository.CompanyRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ImageService imageService;

    @Transactional
    public Company createCompany(Company company, Admin admin) {
        for (Staff staff : company.getStaffList()) {
            staff.setCompany(company);
        }
        company.setAdmin(admin);
        Company savedCompany = companyRepository.save(company);
        System.out.println("Company saved with ID: " + savedCompany.getId());
        String logoBase64 = company.getLogoUrl();
        if (logoBase64 != null && !logoBase64.isEmpty()) {
            try {
                String savedPath = imageService.saveLogoImage(logoBase64, savedCompany.getId());
                savedCompany.setLogoUrl(savedPath);
                System.out.println("Logo path set: " + savedPath);
            } catch (Exception e) {
                System.out.println("Error saving logo: " + e.getMessage());
                savedCompany.setLogoUrl(null);
            }
        } else {
            System.out.println("No logoWK>System: logoBase64 is null or empty");
        }
        for (Staff staff : savedCompany.getStaffList()) {
            String staffPhotoBase64 = staff.getPhoto();
            if (staffPhotoBase64 != null && !staffPhotoBase64.isEmpty()) {
                try {
                    String savedPath = imageService.saveStaffImage(staffPhotoBase64, savedCompany.getId(), staff.getName());
                    staff.setPhoto(savedPath);
                    System.out.println("Staff photo path set for " + staff.getName() + ": " + savedPath);
                } catch (Exception e) {
                    System.out.println("Error saving staff photo for " + staff.getName() + ": " + e.getMessage());
                    staff.setPhoto(null);
                }
            }
        }
        return companyRepository.save(savedCompany);
    }

    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    @Transactional
    public Company updateCompany(Long id, Company company) {
        Company existing = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        System.out.println("Updating company with ID: " + id);

        existing.setName(company.getName());
        existing.setDescription(company.getDescription());
        String logoBase64 = company.getLogoUrl();
        if (logoBase64 != null && !logoBase64.isEmpty()) {
            try {
                if (isBase64String(logoBase64)) {
                    String savedPath = imageService.saveLogoImage(logoBase64, id);
                    existing.setLogoUrl(savedPath);
                } else {
                    existing.setLogoUrl(logoBase64);
                }
            } catch (Exception e) {
                System.out.println("Error processing logo: " + e.getMessage());
            }
        } else {
            System.out.println("No logo data provided, keeping existing logo");
        }
        existing.getStaffList().clear();
        System.out.println("Cleared existing staff list");
        if (company.getStaffList() != null) {
            for (Staff staff : company.getStaffList()) {
                staff.setCompany(existing);
                String staffPhotoBase64 = staff.getPhoto();
                if (staffPhotoBase64 != null && !staffPhotoBase64.isEmpty()) {
                    try {
                        if (isBase64String(staffPhotoBase64)) {
                            String savedPath = imageService.saveStaffImage(staffPhotoBase64, id, staff.getName());
                            staff.setPhoto(savedPath);
                            System.out.println("Staff photo path set for " + staff.getName() + ": " + savedPath);
                        } else {
                            staff.setPhoto(staffPhotoBase64);
                            System.out.println("Staff photo for " + staff.getName() + " kept as existing path");
                        }
                    } catch (Exception e) {
                        System.out.println("Error saving staff photo for " + staff.getName() + ": " + e.getMessage());
                        staff.setPhoto(null);
                    }
                }
                existing.getStaffList().add(staff);
            }
        }
        System.out.println("Company staff after update: " + existing.getStaffList().size() + " staff members");
        return companyRepository.save(existing);
    }

    private StaffDto toStaffDto(Staff staff) {
        StaffDto dto = new StaffDto();
        dto.setId(staff.getId());
        dto.setName(staff.getName());
        dto.setPhoneNumber(staff.getPhoneNumber());
        dto.setPosition(staff.getPosition());
        if (staff.getPhoto() != null && !staff.getPhoto().isEmpty()) {
            try {
                if(staff.getPhoto() == null){
                    System.out.println("Error: photo is null");
                }
                String base64 = imageService.filePathToBase64(staff.getPhoto());
                dto.setPhotoBase64(base64);
            } catch (Exception e) {
                System.out.println("Error converting staff photo to base64 for " + staff.getName() + ": " + e.getMessage());
                dto.setPhotoBase64(null);
            }
        } else {
            dto.setPhotoBase64(null);
        }
        dto.setOriginalPhoto(staff.getPhoto());
        return dto;
    }

    public CompanyDto getCompanyDtoById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));

        CompanyDto dto = new CompanyDto();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setDescription(company.getDescription());
        if (company.getLogoUrl() != null && !company.getLogoUrl().isEmpty()) {
            try {
                String base64 = imageService.filePathToBase64(company.getLogoUrl());
                dto.setLogoUrl(base64);
                System.out.println("Logo converted to base64 for company ID " + id);
            } catch (Exception e) {
                System.out.println("Error converting logo to base64 for company ID " + id + ": " + e.getMessage());
                dto.setLogoUrl(null);
            }
        } else {
            dto.setLogoUrl(null);
            System.out.println("No logo found for company ID " + id);
        }
        dto.setOriginalLogoUrl(company.getLogoUrl());
        List<StaffDto> staffDtos = new ArrayList<>();
        if (company.getStaffList() != null) {
            for (Staff staff : company.getStaffList()) {
                staffDtos.add(toStaffDto(staff));
            }
        }
        dto.setStaffList(staffDtos);
        return dto;
    }

    public void deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));
        if (company.getLogoUrl() != null && !company.getLogoUrl().isEmpty()) {
            System.out.println("Deleting logo on company delete: " + company.getLogoUrl());
            new File(company.getLogoUrl()).delete();
        }
        for (Staff staff : company.getStaffList()) {
            if (staff.getPhoto() != null && !staff.getPhoto().isEmpty()) {
                System.out.println("Deleting staff photo on company delete: " + staff.getPhoto());
                new File(staff.getPhoto()).delete();
            }
        }
        companyRepository.deleteById(id);
    }

    private boolean isBase64String(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        if (str.contains(",")) {
            str = str.substring(str.indexOf(",") + 1);
        }
        return str.matches("^[A-Za-z0-9+/]*={0,2}$") && str.length() % 4 == 0;
    }
}