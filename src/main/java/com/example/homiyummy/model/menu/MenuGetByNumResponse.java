package com.example.homiyummy.model.menu;

import com.example.homiyummy.model.dish.DishGetByEntity;
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
    private DishGetByResponse dessert = new DishGetByResponse();
    private float price = 0;
    private String status = "";
}
