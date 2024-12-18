package com.example.homiyummy.model.menu;

import com.example.homiyummy.model.dish.DishEntity;
import com.example.homiyummy.model.dish.DishGetByEntity;
import com.example.homiyummy.model.dish.DishInOrderEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuGetByNumEntity {
    private int id = 0;
    private int date = 0;
    private DishGetByEntity first_course = new DishGetByEntity();
    private DishGetByEntity second_course = new DishGetByEntity();
    private DishGetByEntity dessert = new DishGetByEntity();
    private float price = 0;
    private String status = "";
}
