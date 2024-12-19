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

    /**
     * @param dishDTO EL FRONT MANDA UN JSON QUE RECIBIMOS COMO ESTE OBJETO DishDTO
     * @return UN STRING CON FORMATO JSON CON EL ID DEL PLATO CREADO. SI HAY UN ERROR EL NÚMERO QUE SE ASIGNA ES 0
     */

    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestBody DishDTO dishDTO){

        // EXTRAEMOS EL UID DEL USUARIO
        String uid = dishDTO.getUid();

        //
        // SI NO ESTÁ VACÍO
        if(!uid.isEmpty()){

            //CREAMOS UN FUTURO
            CompletableFuture<Boolean> future = new CompletableFuture<>();

            // COMPROBAMOS SI EL UID EXISTE
            restaurantService.existsByUid(uid).thenAccept( exists ->
                    future.complete(exists));

            Boolean restaurantExists;
            try {
                // SI EXISTE COMPLETAMOS EL FUTURO
                restaurantExists = future.get(); // EXISTE O NO EXISTE
            } catch (InterruptedException | ExecutionException e) {
                //SI NO EXISTE ENVIAMOS RESPUESTA AL FRONT CON EL UID = 0
                e.printStackTrace();
                return new ResponseEntity<>("{\"id\": 0}", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if(restaurantExists){
                // OBTENEMOS EL ÚLTIMO ID GUARDADO
                int lastIdSaved = dishService.findLastId(uid);

                // ASIGNAMOS EL ID AL PLATO QUE VAMOS A GUARDAR
                int newDishId = lastIdSaved + 1;

                try{
                    // CREAMOS EL OBJETO "PLATO" CON EL ID MANDÁNDOSELO AL SERVICE
                    DishResponse dishResponse = dishService.create(dishDTO, newDishId);

                    // DEVOLVEMOS AL FRONT LA CONFIRMACIÓN DE QUE SALIÓ BIEN CON EL ID GENERADO
                    return new ResponseEntity<>("{\"id\": \"" + dishResponse.getId() + "\"}", HttpStatus.OK);
                }
                catch (RuntimeException e) {
                    // CON UNA EXCEPCIÓN LA RESPUESTA AL FRONT ES EL ID = 0
                    e.printStackTrace(); // Log para identificar el error exacto
                    return new ResponseEntity<>("{\"id\": 0}", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            else{
                // SI EL RESTAURANTE NO EXISTE EL FRONT  RECIBE UN JSON CON EL ID VACÍO
                return new ResponseEntity<>("{\"id\": 0}", HttpStatus.NOT_FOUND);
            }
        }
        else{
            // SI VIENE VACÍO EL FRONT  RECIBE UN JSON CON EL ID VACÍO
            return new ResponseEntity<>("{\"id\": 0}", HttpStatus.NOT_FOUND);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     *
     * @param dishDTO EL FRONT MANDA UN JSON QUE RECIBIMOS COMO ESTE OBJETO DishDTO
     * @return UN STRING CON FORMATO JSON CON change: true SI  SALE BIEN, O change: false SI ALGO SALE MAL
     */

    @PostMapping("/update")
    public ResponseEntity<String> update(@RequestBody DishDTO dishDTO) {

        // OBTENEMOS UID DEL RESTAURANTE
        String uid = dishDTO.getUid();

        // SI NO ESTÁ VACÍO CONTINUAMOS
        if(!uid.isEmpty()) {

            // CREAMOS UN FUTURO DONDE GUARDAR EL RESULTADO
            CompletableFuture<Boolean> future = new CompletableFuture<>();

            // COMPROBAMOS Q EL RESTAURANTE EXISTE Y SI LO HACE COMPLETAMOS EL FUTURO
            restaurantService.existsByUid(uid).thenAccept(exists ->
                    future.complete(exists));


            Boolean restaurantExists;
            try {
                // EXTRAEMOS EL RESULTADO DEL FUTURO
                restaurantExists = future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

            // SI EL RESTAURANTE EXISTE
            if (restaurantExists) {
                // ENVIAMOS AL SERVICE LA PETICIÓN DE ACTUALIZACIÓN Y GUARDAMOS EL RESULTADO (true SI SALE BIEN, false SI SALE MAL)
                Boolean result = dishService.updateDish(dishDTO);

                // Y LO DEVOLVEMOS AL FRONT EN UN JSON
                return new ResponseEntity<>("{\"change\": \"" + result + "\"}", HttpStatus.OK);
            } else {

                // SI EL RESTAURANTE NO EXISTE MANDAMOS UN JSON CON false
                return new ResponseEntity<>("{\"change\": \"" + false + "\"}", HttpStatus.NOT_FOUND);
            }
        } else {
            // SI EL UID VIENE VACÍO MANDAMOS AL FRONT UN JSON CON false
            return new ResponseEntity<>("{\"change\": \"" + false + "\"}", HttpStatus.NOT_FOUND);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     *
     * @param dishRequest OBJETO EN QUE CONVERTIMOS EL JSON ENTRANTE. CONTIENE:
     *                    - UID DEL RESTAURANTE
     *                    - ID DEL PLATO
     * @return DEVUELVE True o False DEPENDIENDO DEL RESULTADO
     */

    @PostMapping("/delete")
    public CompletableFuture<ResponseEntity<DishDeleteResponse>> delete(@RequestBody DishRequest dishRequest) {

        // EXTRAEMOS EL UID DEL RESTAURANTE Y EL ID DEL PLATO
        String uid = dishRequest.getUid();
        int id = dishRequest.getId();

        // SI NO VIENE VACÍO EL UID
        if (!uid.isEmpty()) {

            // HACEMOS PETICIÓN AL SERVICE PARA QUE EN EL RESTAURANTE CON ESE UID ELIMINE EL PLATO CON ESE ID
            return dishService.deleteDish(uid, id).thenApply( success ->
                    // MANDAMOS AL FRONT LA RESPUESTA UQE ESPERA (true)
                    new ResponseEntity<>(new DishDeleteResponse(success), HttpStatus.OK));
        }
        else {
            DishDeleteResponse dishDeleteResponse = new DishDeleteResponse();
            dishDeleteResponse.setDelete(false);
            // SI SALE MAL ENVIAMOS AL FRONT LA RESPUESTA QUE ESPERA (false)
            return CompletableFuture.completedFuture(
                    new ResponseEntity<>(dishDeleteResponse, HttpStatus.NOT_FOUND));
        }
    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     *
     * @param dishRequest OBJETO EN QUE CONVERTIMOS EL JSON ENTRANTE. CONTIENE:
     *                    - UID DEL RESTAURANTE
     *                    - ID DEL PLATO
     * @return DEVUELVE EL PLATO OBTENIDO EN FORMATO DishResponse CON TODAS SUS PROPIEDADES.
     *                  SI HAY ALGÚN ERROR, LAS PROPIEDADES VIENEN VACÍAS
     */
    @PostMapping("/getById")
    public CompletableFuture<ResponseEntity<DishResponse>> getById(@RequestBody DishRequest dishRequest) {

        // EXTRAEMOS DEL OBJETO ENTRANTE EL UID DEL RESTAURANTE Y EL ID DEL PLATO
        String uid = dishRequest.getUid();
        int id = dishRequest.getId();

        // SI NO ESTÁ VACÍO EL UID
        if (!uid.isEmpty()) {
            // ENVIAMOS LA PETICIÓN AL SERVICIO
            return dishService.getDish(uid, id).thenApply( success ->
                    // SI ES EXITOSA, ENVIAMOS RESPUESTA AL FRONT
                    new ResponseEntity<>(success, HttpStatus.OK));
        }
        else {
            DishResponse dishResponse = new DishResponse();
            // SI EL UID ESTÁ VACÍO ENVIAMOS UNA RESPUESTA CON UN OBJETO VACÍO
            return CompletableFuture.completedFuture(
                    new ResponseEntity<>(dishResponse, HttpStatus.NOT_FOUND));
        }
    }

    // ----------------------------------------------------------------------------------------------------------------

}
