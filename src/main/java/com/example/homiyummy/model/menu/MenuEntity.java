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
public class MenuEntity {

    private String uid;
    private int id;
    private int date;
    private ArrayList<Integer> first_course;
    private ArrayList<Integer> second_course;
    private int dessert;
    private float priceWithDessert;
    private float priceNoDessert;

    public MenuEntity(int id, int date, ArrayList<Integer> firstCourse, ArrayList<Integer> secondCourse, int dessert, float priceWithDessert, float priceNoDessert){
        this.id = id;
        this.date = date;
        this.first_course = firstCourse;
        this.second_course = secondCourse;
        this.dessert = dessert;
        this.priceWithDessert = priceWithDessert;
        this.priceNoDessert = priceNoDessert;
    }
}
