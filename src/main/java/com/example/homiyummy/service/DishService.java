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
            return futureId.get();      // ENVIAMOS EL ID
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace(); // Log para identificar el error exacto
            return 0; // Devuelve 0 en caso de fallo para cumplir con las expectativas del frontend
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
        dishEntity.setAllergens(dishDTO.getAllergens());

        dishRepository.save(dishEntity, new DishRepository.SavePlatoCallback() {
            @Override
            public void onSuccess(DishResponse dishResponse) {
                //System.out.println("-------------1---------------id del plato: " + dishResponse.getId());
                futurePlatoResponse.complete(dishResponse);
            }
            @Override
            public void onFailure(Exception exception) {
                //System.out.println("-------------2---------------");
                futurePlatoResponse.completeExceptionally(exception);
            }
        });
        try {
            //System.out.println("-------------3---------------");
            return futurePlatoResponse.get();
        } catch (InterruptedException | ExecutionException e) {
            // Manejar la interrupción del hilo
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error al crear el plato: " + e.getMessage(), e);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------

    public Boolean updateDish(DishDTO dishDTO){
        //System.out.println("asfasdfasdfasdfasfd");

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



}
