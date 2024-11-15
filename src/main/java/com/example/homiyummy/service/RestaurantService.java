package com.example.homiyummy.service;

import com.example.homiyummy.model.restaurant.RestaurantDTO;
import com.example.homiyummy.model.restaurant.RestaurantEntity;
import com.example.homiyummy.model.restaurant.RestaurantReadResponse;
import com.example.homiyummy.model.restaurant.RestaurantResponse;
import com.example.homiyummy.model.user.UserResponse;
import com.example.homiyummy.repository.RestaurantRepository;
import com.google.firebase.database.*;
import org.springframework.stereotype.Service;

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
        restaurantEntity.setFoodType(restaurantDTO.getFoodType());

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
        restaurantEntity.setFoodType(restaurantDTO.getFoodType());

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

        // Suponiendo que RestaurantRepository hace la consulta a Firebase de forma similar a UserRepository
        DatabaseReference restaurantRef = firebaseDatabase.getReference("restaurants").child(uid);
        restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                future.complete(snapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });

        return future;
    }


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
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
