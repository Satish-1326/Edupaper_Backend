package com.edupaper.dto.document;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitContentResponse {

    private Long unitId;

    private Integer unitNumber;

    private String unitTitle;

    private Integer characterCount;

    private String content;
}