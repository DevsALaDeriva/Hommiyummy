package com.example.homiyummy.model.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderGetClientOrdersResponse {
    private String name_restaurant = "";
    private String image_restaurant = "";
    private int date = 0;
    private String num_order = "";
    private float total = 0;
    private String status = "";
}
