package com.example.homiyummy.model.course;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseEntity {
    private String name = "";
    private String ingredients = "";
    private String allergens = "";
    private String image = "";
}
