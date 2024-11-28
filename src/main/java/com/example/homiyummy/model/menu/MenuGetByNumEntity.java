package com.example.homiyummy.model.menu;

import com.example.homiyummy.model.dish.DishEntity;
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
    private int date = 0;
    private DishInOrderEntity firstCourse = new DishInOrderEntity();
    private DishInOrderEntity secondCourse = new DishInOrderEntity();
    private DishInOrderEntity dessert = new DishInOrderEntity();
    private float total = 0;
    private String status = "";
}