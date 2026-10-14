package com.smartwater.dto;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApartmentDto {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private Integer totalUnits;
    private OffsetDateTime createdAt;
}
