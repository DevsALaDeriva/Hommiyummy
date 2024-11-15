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
public class MenuResponse {
    private String uid;
    private int date;
    private ArrayList<String> firstCourse;
    private ArrayList<String> secondCourse;
    private int dessert;
    private float priceWithDessert;
    private float priceNoDessert;
}
