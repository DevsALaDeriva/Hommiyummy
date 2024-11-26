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
public class MenuSoldDTO {

    private int id = 0;
    private int first_course = 0;
    private int second_course = 0;
    private int dessert = 0;
    private float price = 0;
    private String status = "";

}
