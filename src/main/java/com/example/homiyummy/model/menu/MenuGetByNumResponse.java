package com.example.homiyummy.model.menu;

import com.example.homiyummy.model.dish.DishGetByResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuGetByNumResponse {


    private int id = 0;
    private int date = 0;
    private DishGetByResponse first_course = new DishGetByResponse();
    private DishGetByResponse second_course = new DishGetByResponse();
    private Object dessert = new DishGetByResponse(); // Usamos Object para permitir flexibilidad
    private float price = 0;
    private String status = "";

    public MenuGetByNumResponse(int id, int date, DishGetByResponse first_course, DishGetByResponse second_course, DishGetByResponse dessert, float price, String status) {
        this.id = id;
        this.date = date;
        this.first_course = first_course;
        this.second_course = second_course;
        this.dessert = dessert;
        this.price = price;
        this.status = status;
    }

    public MenuGetByNumResponse(int id, int date, DishGetByResponse first_course, DishGetByResponse second_course, String dessert, float price, String status) {
        this.id = id;
        this.date = date;
        this.first_course = first_course;
        this.second_course = second_course;
        this.dessert = dessert;
        this.price = price;
        this.status = status;
    }
}
