package com.edupaper.dto.topic;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicResponse {

    private Long id;

    private String name;

    private String description;

    private Long unitId;
}