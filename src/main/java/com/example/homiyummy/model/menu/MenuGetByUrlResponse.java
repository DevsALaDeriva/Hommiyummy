package com.example.homiyummy.model.menu;

import com.example.homiyummy.model.dish.DishGetByUrlEntity;
import com.example.homiyummy.model.dish.DishInOrderEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuGetByUrlResponse {
    private int id = 0;
    private int date = 0;
    private ArrayList<DishGetByUrlEntity> first_course = new ArrayList<>();
    private ArrayList<DishGetByUrlEntity> second_course = new ArrayList<>();
    private DishInOrderEntity dessert = new DishInOrderEntity();
    private float priceWithDessert = 0;
    private float priceNoDessert = 0;

}
