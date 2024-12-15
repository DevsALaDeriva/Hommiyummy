package com.example.homiyummy.controller;

import com.example.homiyummy.model.dish.*;
import com.example.homiyummy.service.DishService;
import com.example.homiyummy.service.RestaurantService;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/dish")
public class DishController {

    FirebaseAuth firebaseAuth;
    DishService dishService;
    RestaurantService restaurantService;

    public DishController(FirebaseAuth firebaseAuth, DishService dishService, RestaurantService restaurantService){
        this.firebaseAuth = firebaseAuth;
        this.dishService = dishService;
        this.restaurantService = restaurantService;
    }

    // ----------------------------------------------------------------------------------------------------------------

    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestBody DishDTO dishDTO){

        String uid = dishDTO.getUid();
        if(!uid.isEmpty()){

            CompletableFuture<Boolean> future = new CompletableFuture<>();

            restaurantService.existsByUid(uid).thenAccept( exists ->
                    future.complete(exists));

            Boolean restaurantExists;
            try {
                restaurantExists = future.get(); // EXISTE O NO EXISTE
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return new ResponseEntity<>("{\"id\": 0}", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if(restaurantExists){

                int lastIdSaved = dishService.findLastId(uid);
                int newDishId = lastIdSaved + 1;

                try{
                    DishResponse dishResponse = dishService.create(dishDTO, newDishId);
                    return new ResponseEntity<>("{\"id\": \"" + dishResponse.getId() + "\"}", HttpStatus.OK);
                }
                catch (RuntimeException e) {
                    e.printStackTrace(); // Log para identificar el error exacto
                    return new ResponseEntity<>("{\"id\": 0}", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            else{
                return new ResponseEntity<>("{\"id\": 0}", HttpStatus.NOT_FOUND);
            }
        }
        else{
            return new ResponseEntity<>("{\"id\": 0}", HttpStatus.NOT_FOUND);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------

    @PostMapping("/update")
    public ResponseEntity<String> update(@RequestBody DishDTO dishDTO) {

        String uid = dishDTO.getUid();

        if(!uid.isEmpty()) {

            CompletableFuture<Boolean> future = new CompletableFuture<>();

            restaurantService.existsByUid(uid).thenAccept(exists ->
                    future.complete(exists));

            Boolean restaurantExists;
            try {
                restaurantExists = future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

            if (restaurantExists) {
                Boolean result = dishService.updateDish(dishDTO);
                return new ResponseEntity<>("{\"change\": \"" + result + "\"}", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("{\"change\": \"" + false + "\"}", HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>("{\"change\": \"" + false + "\"}", HttpStatus.NOT_FOUND);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------


    @PostMapping("/delete")
    public CompletableFuture<ResponseEntity<DishDeleteResponse>> delete(@RequestBody DishRequest dishRequest) {

        String uid = dishRequest.getUid();
        int id = dishRequest.getId();

        if (!uid.isEmpty()) {
            return dishService.deleteDish(uid, id).thenApply( success ->
                    new ResponseEntity<>(new DishDeleteResponse(success), HttpStatus.OK));
        }
        else {
            DishDeleteResponse dishDeleteResponse = new DishDeleteResponse();
            dishDeleteResponse.setDelete(false);
            return CompletableFuture.completedFuture(
                    new ResponseEntity<>(dishDeleteResponse, HttpStatus.NOT_FOUND));
        }
    }

    // ----------------------------------------------------------------------------------------------------------------

    @PostMapping("/getById")
    public CompletableFuture<ResponseEntity<DishResponse>> getById(@RequestBody DishRequest dishRequest) {

        String uid = dishRequest.getUid();
        int id = dishRequest.getId();

        if (!uid.isEmpty()) {
            return dishService.getDish(uid, id).thenApply( success ->
                    new ResponseEntity<>(success, HttpStatus.OK));
        }
        else {
            DishResponse dishResponse = new DishResponse();
            return CompletableFuture.completedFuture(
                    new ResponseEntity<>(dishResponse, HttpStatus.NOT_FOUND));
        }
    }

    // ----------------------------------------------------------------------------------------------------------------

}
