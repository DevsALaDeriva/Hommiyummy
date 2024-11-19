package com.example.homiyummy.model.restaurant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
/**
 * CLASE CREADA PARA ENVIAR AL FRONTEND TODOS LOS RESTAURANTES CON TODAS SUS PROPIEDADES
 */
public class RestaurantGetAllResponse {
    private ArrayList<RestaurantResponse> restaurantResponses;
}
