package com.visitcard.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class StaffDto {
    private Long id;
    private String name;
    private String phoneNumber;
    private String position;
    private String photoBase64; // Base64 encoded photo for frontend display
    private String photoUrl;    // File path/URL stored in database (optional, for debugging)
}
