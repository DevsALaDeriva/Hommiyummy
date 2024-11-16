package com.example.homiyummy.controller;

import com.example.homiyummy.model.dish.DishDTO;
import com.example.homiyummy.model.dish.DishResponse;
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
    //public ResponseEntity<?> create(@RequestBody PlatoDTO platoDTO){
    public ResponseEntity<String> create(@RequestBody DishDTO dishDTO){

        String uid = dishDTO.getUid();

        if(!uid.isEmpty()){
            CompletableFuture<Boolean> future = new CompletableFuture<>();

            // 1º COMPROBAMOS QUE EL UID EXISTE PARA EVITAR ERRORES VISTOS YA SOBRE ESTO
            restaurantService.existsByUid(uid).thenAccept( exists ->        // EL CODIGO DEBE QUEDARSE ESPERANDO AQUÍ, EN EL thenAccpet HASTA QUE LLEGUE LA RESPUESTA
                    future.complete(exists));

            Boolean restaurantExists;
            try {
                restaurantExists = future.get(); // EXISTE O NO EXISTE

            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Existe el restaurante? -> " + restaurantExists);

            if(restaurantExists){ // SI EXISTE SEGUIMOS
                // 2º AVERIGÜAMOS EL ID DEL ÚLTIMO PLATO GUARDADO
                int lastIdSaved = dishService.findLastId(uid);
                int newDishId = lastIdSaved + 1;
                System.out.println("NEW DISH ID -1--------------> " + newDishId);

                // 3º TRAMITAMOS EL GUARDADO DE DATOS
                try{
                    DishResponse dishResponse = dishService.create(dishDTO, newDishId);
                    System.out.println("NEW DISH ID -2--------------> " + newDishId);
                    return new ResponseEntity<>("{\"id\": \"" + dishResponse.getId() + "\"}", HttpStatus.OK);
                }
                catch (RuntimeException e) {
                    e.printStackTrace(); // Log para identificar el error exacto
                    return new ResponseEntity<>("{\"id\": 0}", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            else{
                return new ResponseEntity<>("{\"id\": 0}", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        else{
            return new ResponseEntity<>("{\"id\": 0}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // ----------------------------------------------------------------------------------------------------------------

    @PostMapping("/update")
    public ResponseEntity<String> update(@RequestBody DishDTO dishDTO) {
        System.out.println("asfasdfasdfasdfasfd");

        String uid = dishDTO.getUid();

        if(!uid.isEmpty()) {

            CompletableFuture<Boolean> future = new CompletableFuture<>();

            // 1º COMPROBAMOS QUE EL UID EXISTE PARA EVITAR ERRORES VISTOS YA SOBRE ESTO
            restaurantService.existsByUid(uid).thenAccept(exists ->        // EL CODIGO DEBE QUEDARSE ESPERANDO AQUÍ, EN EL thenAccpet HASTA QUE LLEGUE LA RESPUESTA
                    future.complete(exists));

            Boolean restaurantExists;
            try {
                restaurantExists = future.get(); // EXISTE O NO EXISTE
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Existe el restaurante? -> " + restaurantExists);

            if (restaurantExists) {
                Boolean result = dishService.updateDish(dishDTO);
                return new ResponseEntity<>("{\"change\": \"" + result + "\"}", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("{\"change\": \"" + false + "\"}", HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<>("{\"change\": \"" + false + "\"}", HttpStatus.OK);
        }
    }


}
