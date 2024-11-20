package com.example.homiyummy.service;

import com.example.homiyummy.model.dish.DishEntity;
import com.example.homiyummy.model.dish.DishResponse;
import com.example.homiyummy.model.restaurant.*;
import com.example.homiyummy.repository.RestaurantRepository;
import com.google.firebase.database.FirebaseDatabase;
import org.checkerframework.checker.units.qual.A;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final FirebaseDatabase firebaseDatabase;

    public RestaurantService(RestaurantRepository restaurantRepository, FirebaseDatabase firebaseDatabase) {
        this.restaurantRepository = restaurantRepository;
        this.firebaseDatabase = firebaseDatabase;
    }


    // ----------------------------------------------------------------------------------------------------------------

    public RestaurantResponse createRestaurant(RestaurantDTO restaurantDTO) {

        RestaurantEntity restaurantEntity = new RestaurantEntity();

        restaurantEntity.setUid(restaurantDTO.getUid());
        restaurantEntity.setEmail(restaurantDTO.getEmail());
        restaurantEntity.setName(restaurantDTO.getName());
        restaurantEntity.setDescription_mini(restaurantDTO.getDescription_mini());
        restaurantEntity.setDescription(restaurantDTO.getDescription());
        restaurantEntity.setUrl(restaurantDTO.getUrl());
        restaurantEntity.setAddress(restaurantDTO.getAddress());
        restaurantEntity.setCity(restaurantDTO.getCity());
        restaurantEntity.setPhone(restaurantDTO.getPhone());
        restaurantEntity.setSchedule(restaurantDTO.getSchedule());
        restaurantEntity.setImage(restaurantDTO.getImage());
        restaurantEntity.setFood_type(restaurantDTO.getFood_type());
        //System.out.println(restaurantDTO.getLocation().getLat());
        //System.out.println(restaurantDTO.getLocation().getLng());
        restaurantEntity.setLocation(restaurantDTO.getLocation());

        CompletableFuture<RestaurantResponse> future = new CompletableFuture<>();

        restaurantRepository.saveRestaurant(restaurantEntity, new RestaurantRepository.getSaveRestaurantCallback() {
            @Override
            public void onRestaurantGot(RestaurantResponse restaurantResponse) {
                future.complete(restaurantResponse);
            }

            @Override
            public void onFailure(Exception exception) {
                future.completeExceptionally(exception);
            }
        });

        try {
            return future.get(); // DEVUELVE UN RestaurantResponse DESPUÉS DE HABERLO SACADO DEL future. Y ESPERA AHÍ HASTA QUE EL GET LO OBTIENE
        }  catch (Exception e) {
            throw new RuntimeException("Error al guardar el restaurante en Firebase", e);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------

    public Boolean updateRestaurant(RestaurantDTO restaurantDTO) {

        CompletableFuture<Boolean> futureResponse = new CompletableFuture<>();

        RestaurantEntity restaurantEntity = new RestaurantEntity();

        restaurantEntity.setUid(restaurantDTO.getUid());
        restaurantEntity.setName(restaurantDTO.getName());
        restaurantEntity.setDescription_mini(restaurantDTO.getDescription_mini());
        restaurantEntity.setDescription(restaurantDTO.getDescription());
        restaurantEntity.setUrl(restaurantDTO.getUrl());
        restaurantEntity.setAddress(restaurantDTO.getAddress());
        restaurantEntity.setCity(restaurantDTO.getCity());
        restaurantEntity.setPhone(restaurantDTO.getPhone());
        restaurantEntity.setSchedule(restaurantDTO.getSchedule());
        restaurantEntity.setImage(restaurantDTO.getImage());
        restaurantEntity.setFood_type(restaurantDTO.getFood_type());
        restaurantEntity.setLocation(restaurantDTO.getLocation());

        restaurantRepository.updateRestaurantData(restaurantEntity, new RestaurantRepository.GetUpdateRestaurantCallback() {
            @Override
            public void onSuccess(Boolean confirmation) {
                futureResponse.complete(confirmation);
            }

            @Override
            public void onFailure(Exception exception) {
                futureResponse.completeExceptionally(exception);
            }
        });

        try {
            return futureResponse.get();
        }
        catch (Exception e){
            throw new RuntimeException("Error al obtener confirmación de la actualización del restaurante", e);
        }

    }

    // ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<Boolean> existsByUid(String uid) {

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        restaurantRepository.exists(uid, new RestaurantRepository.ExistsRestaurantCallback() {
            @Override
            public void onSuccess(Boolean response) {
                future.complete(response);
            }

            @Override
            public void onFailure(Exception exception) {
                future.completeExceptionally(exception);
            }
        });
        return future;
    }

    // ----------------------------------------------------------------------------------------------------------------

    public RestaurantReadResponse findByUid(String uid) {

        CompletableFuture<RestaurantReadResponse> future = new CompletableFuture<>();

        restaurantRepository.findByUid(uid, new RestaurantRepository.FindRestaurantCallback() {
            @Override
            public void onSuccess(RestaurantReadResponse response) {
                future.complete(response);
            }

            @Override
            public void onFailure(RestaurantReadResponse response) {
                future.complete(response);
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<Map<String,ArrayList<String>>> getFoodTypes() {
        CompletableFuture<Map<String,ArrayList<String>>> futureTypes = new CompletableFuture<>();
        Map<String, ArrayList<String>> objeto = new HashMap<>();
        restaurantRepository.getAllFoodTypes(new RestaurantRepository.OnTypesGot() {
            @Override
            public void onTypesSuccess(ArrayList<String> types) {
                objeto.put("food_type", types);
                futureTypes.complete(objeto);
            }

            @Override
            public void onTypesFailure(Exception exception) {
                //futureTypes.complete(exception);
                // TODO -------> PREGUNTAR QUÉ QUIERE EL FRONTEND SI DA ERROR
            }
        });
        return futureTypes;
    }

    // ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<Map<String, ArrayList<RestaurantGetAllFormatResponse>>> getAllRestaurants(){

        CompletableFuture<Map<String, ArrayList<RestaurantGetAllFormatResponse>>> futureAllRestaurants = new CompletableFuture<>();

        restaurantRepository.getAllRestaurantList(new RestaurantRepository.OnRestaurantListCallback() {
            @Override
            public void onSearchingSuccess(ArrayList<RestaurantEntity> restaurants) {

                ArrayList<RestaurantGetAllFormatResponse> restListResponse = new ArrayList<>();

                for(RestaurantEntity restaurant : restaurants){

                    RestaurantGetAllFormatResponse restaurantGetAllFormatResponse = new RestaurantGetAllFormatResponse();

                    restaurantGetAllFormatResponse.setUid(restaurant.getUid());
                    restaurantGetAllFormatResponse.setName(restaurant.getName());
                    restaurantGetAllFormatResponse.setDescription_mini(restaurant.getDescription_mini());
                    restaurantGetAllFormatResponse.setUrl(restaurant.getUrl());
                    restaurantGetAllFormatResponse.setAddress(restaurant.getAddress());
                    restaurantGetAllFormatResponse.setPhone(restaurant.getPhone());
                    restaurantGetAllFormatResponse.setSchedule(restaurant.getSchedule());
                    restaurantGetAllFormatResponse.setFood_type(restaurant.getFood_type());
                    restaurantGetAllFormatResponse.setImage(restaurant.getImage());
                    restaurantGetAllFormatResponse.setRate(restaurant.getRate());
                    restaurantGetAllFormatResponse.setAverage_price(restaurant.getAverage_price());
                    restaurantGetAllFormatResponse.setLocation(restaurant.getLocation());

                    restListResponse.add(restaurantGetAllFormatResponse);

                }

                Map<String, ArrayList<RestaurantGetAllFormatResponse>> dataResponse = new HashMap<>();
                dataResponse.put("restaurants", restListResponse);

                futureAllRestaurants.complete(dataResponse);
            }

            @Override
            public void onSearchingFailure(Exception exception) {
                futureAllRestaurants.completeExceptionally(exception);
            }
        });
        return futureAllRestaurants;
    }

}
