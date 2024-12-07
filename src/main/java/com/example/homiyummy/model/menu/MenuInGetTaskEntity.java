package com.example.homiyummy.model.menu;

//import com.example.homiyummy.model.course.CourseEntity;
import com.example.homiyummy.model.dish.DishGetDayTaskEntity;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter

public class MenuInGetTaskEntity {
    private int id = 0;
    private int date = 0;
    private DishGetDayTaskEntity first_course = new DishGetDayTaskEntity();
    private DishGetDayTaskEntity second_course = new DishGetDayTaskEntity();
    private DishGetDayTaskEntity dessert = new DishGetDayTaskEntity();
    private String status = "";
}
