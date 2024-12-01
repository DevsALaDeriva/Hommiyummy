package com.example.homiyummy.model.menu;

import com.example.homiyummy.model.course.CourseEntity;
import com.example.homiyummy.model.course.CourseResponse;
import lombok.*;

@AllArgsConstructor // CONSTRUCTOR PRINCIPAL USADO PARA CREAR UN USUARIO
@NoArgsConstructor
@ToString
@Getter
@Setter

public class MenuInGetTaskEntity {
    private int id = 0;
    private int date = 0;
    private CourseEntity first_course = new CourseEntity();
    private CourseEntity second_course = new CourseEntity();
    private CourseEntity dessert = new CourseEntity();

}
