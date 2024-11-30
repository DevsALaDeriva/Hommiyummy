
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
public class MenuDTO {

    private String uid;
    private int id;
    private int date;
    private ArrayList<Integer> first_course;
    private ArrayList<Integer> second_course;
    private int dessert;
    private float priceWithDessert;
    private float priceNoDessert;
    
}

