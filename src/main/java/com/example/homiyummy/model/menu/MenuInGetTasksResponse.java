package com.example.homiyummy.model.menu;

//import com.example.homiyummy.model.course.CourseResponse;
import com.example.homiyummy.model.dish.DishGetDayTaskResponse;
import lombok.*;

@AllArgsConstructor // CONSTRUCTOR PRINCIPAL USADO PARA CREAR UN USUARIO
@NoArgsConstructor
@ToString
@Getter
@Setter

public class MenuInGetTasksResponse {
    private int id = 0;
    private int date = 0;
    private DishGetDayTaskResponse first_course = new DishGetDayTaskResponse();
    private DishGetDayTaskResponse second_course = new DishGetDayTaskResponse();
    private DishGetDayTaskResponse dessert = new DishGetDayTaskResponse();

}
