package com.example.homiyummy.model.menu;

import com.example.homiyummy.model.course.CourseResponse;
import lombok.*;

@AllArgsConstructor // CONSTRUCTOR PRINCIPAL USADO PARA CREAR UN USUARIO
@NoArgsConstructor
@ToString
@Getter
@Setter

public class MenuInGetTasksResponse {
    private int id = 0;
    private int date = 0;
    private CourseResponse first_course = new CourseResponse();
    private CourseResponse second_course = new CourseResponse();
    private CourseResponse dessert = new CourseResponse();

}
