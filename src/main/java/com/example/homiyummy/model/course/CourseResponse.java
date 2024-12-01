package com.example.homiyummy.model.course;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {
    private String name = "";
    private String ingredients = "";
    private String allergerns = "";
    private String image = "";
}
