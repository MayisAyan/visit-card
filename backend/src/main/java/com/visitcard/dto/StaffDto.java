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
    private String photoBase64;
    private String originalPhoto;
}
