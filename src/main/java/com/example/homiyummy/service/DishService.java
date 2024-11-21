package com.example.homiyummy.service;

import com.example.homiyummy.model.dish.*;
import com.example.homiyummy.repository.DishRepository;
import com.google.firebase.database.DatabaseError;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class DishService {

    private DishRepository dishRepository;


    public DishService(DishRepository dishRepository){
        this.dishRepository = dishRepository;
    }

    // ----------------------------------------------------------------------------------------------------------------

    public int findLastId(String uid){

        CompletableFuture<Integer> futureId = new CompletableFuture<Integer>();

        dishRepository.findId(uid, new DishRepository.FindPlatoIdCallback() {
            @Override
            public void onSuccess(Integer num) {
                futureId.complete(num);
            }

            @Override
            public void onFailure(DatabaseError error, Integer num) {
                futureId.complete(num); // SI DA ERROR ENVÍA UN 0, QUE ES LO QUE ESPERA EL FRONTEND
            }
        });
        try {
            return futureId.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ----------------------------------------------------------------------------------------------------------------

    public DishResponse create(DishDTO dishDTO, int newDishId){

        CompletableFuture<DishResponse> futurePlatoResponse = new CompletableFuture<>();

        DishEntity dishEntity = new DishEntity();
        dishEntity.setId(newDishId);                       // AÑADIMOS EL ID AL PLATO
        dishEntity.setUid((dishDTO.getUid()));
        dishEntity.setName(dishDTO.getName());
        dishEntity.setType(dishDTO.getType());
        dishEntity.setIngredients(dishDTO.getIngredients());
        dishEntity.setImage(dishDTO.getImage());
        dishEntity.setAllergens(dishDTO.getAllergens());

        dishRepository.save(dishEntity, new DishRepository.SavePlatoCallback() {
            @Override
            public void onSuccess(DishResponse dishResponse) {
                futurePlatoResponse.complete(dishResponse);
            }
            @Override
            public void onFailure(Exception exception) {
                futurePlatoResponse.completeExceptionally(exception);
            }
        });
        try {
            return futurePlatoResponse.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error al crear el plato: " + e.getMessage(), e);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------

    public Boolean updateDish(DishDTO dishDTO){

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        DishEntity dishEntity = new DishEntity();
        dishEntity.setUid(dishDTO.getUid());
        dishEntity.setId(dishDTO.getId());
        dishEntity.setName(dishDTO.getName());
        dishEntity.setIngredients(dishDTO.getIngredients());
        dishEntity.setAllergens(dishDTO.getAllergens());
        dishEntity.setType(dishDTO.getType());

        dishRepository.update(dishEntity, new DishRepository.UpdateDishCallback() {
            @Override
            public void onSuccess(Boolean result) {
                future.complete(result);
            }

            @Override
            public void onFailure(Exception exception) {
                future.completeExceptionally(exception);
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

    }

    // ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<DishAllResponse> getAll(String uid) {

        CompletableFuture<DishAllResponse> future = new CompletableFuture<>();

        dishRepository.getAll(uid, new DishRepository.FindAllDishesCallback(){

            @Override
            public void onSuccess(DishAllResponse allDishes) {
                future.complete(allDishes);
            }

            @Override
            public void onFailure(Exception exception) {
                future.completeExceptionally(exception);
            }
        });

        return future;
    }

    // ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<Boolean> deleteDish(String uid, int id){
        return dishRepository.delete(uid, id);
    }

    // ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<DishResponse> getDish(String uid, int id){

        CompletableFuture<DishResponse> futureDishResponse = new CompletableFuture<>();

        dishRepository.get(uid, id, new DishRepository.OnDishGotCallback() {
            @Override
            public void onSuccess(DishEntity dishEntity) {
                DishResponse dishResponse = new DishResponse(dishEntity.getId(),
                        dishEntity.getName(),
                        dishEntity.getIngredients(),
                        dishEntity.getAllergens(),
                        dishEntity.getImage(),
                        dishEntity.getType());

                futureDishResponse.complete(dishResponse);
            }

            @Override
            public void onFailure(Exception exception) {
                    // TODO : ------------------------------
            }
        });
        return futureDishResponse;
    }

    // ----------------------------------------------------------------------------------------------------------------



}
