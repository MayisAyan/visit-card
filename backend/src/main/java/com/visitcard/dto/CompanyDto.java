package com.visitcard.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CompanyDto {
    private Long id;
    private String name;
    private String logoUrl;      // Base64 encoded logo for frontend display
    private String description;
    private List<StaffDto> staffList;
    private String logoFilePath; // File path stored in database (optional, for debugging)
}
