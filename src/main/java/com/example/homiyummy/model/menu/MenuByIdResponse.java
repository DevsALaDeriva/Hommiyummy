package com.example.homiyummy.model.menu;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class MenuByIdResponse {
    private int id;
    private int date;
    private ArrayList<Integer> firstCourse;
    private ArrayList<Integer> secondCourse;
    private int dessert;
    private float priceWithDessert;
    private float priceNoDessert;
}
