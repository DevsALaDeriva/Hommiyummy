package com.example.homiyummy.model.dish;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuSimpleResponse {
    private int id;
    private int date;
    private List<DishSimpleResponse> first_course;
    private List<DishSimpleResponse> second_course;
    private DishSimpleResponse dessert;
    private float priceWithDessert;
    private float priceNoDessert;
}
