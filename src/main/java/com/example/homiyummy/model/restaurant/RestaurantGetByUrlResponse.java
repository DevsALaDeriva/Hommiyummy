package com.example.homiyummy.model.restaurant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RestaurantGetByUrlResponse {
    private String uid = "";
    private String name = "";
    private String food_type = "";
    private String address = "";
    private String image = "";
    private String phone = "";
    private String schedule = "";
    private Integer rate = 0;
    // FALTAN UN ARRAY DE RATE
    //reviews: [
    // name (String),
    //review (String),
    //rate (int)
    //],


    // FALTA UN OBJETO menus: [
    //id (int),
    //date (int),
    //first_course: [
    //id (int),
    //name (String),
    //ingredients (String),
    //allergens (Array)
    //]
    //second_course: [
    //id (int),
    //name (String),
    //ingredients (String),
    //allergens (Array)
    //]
    //dessert: [
    //id (int),
    //name (String),
    //ingredients (String),
    //allergens (Array)
    //]
    //priceWithDessert (float),
    //priceNoDessert (float)
    //]
}
