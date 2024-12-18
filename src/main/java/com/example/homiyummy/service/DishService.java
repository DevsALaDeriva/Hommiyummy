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


    /**
     *
     * @param uid UID DEL RESTAURANTE DEL QUE SE VA A BUSCAR EL ID DEL ÚLTIMO PLATO GUARDADO
     * @return DEVUELVE EL ID (NÚMERO) DEL ÚLTIMO PLATO GUARDADO EN ESTE RESTAURANTE
     */
    public int findLastId(String uid){

        CompletableFuture<Integer> futureId = new CompletableFuture<Integer>();

        dishRepository.findId(uid, new DishRepository.FindPlatoIdCallback() {
            @Override
            public void onSuccess(Integer num) {
                futureId.complete(num);
            }

            @Override
            public void onFailure(DatabaseError error, Integer num) {
                futureId.complete(num);
            }
        });
        try {
            return futureId.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return 0;
        }
    }


    /**
     *
     * @param dishDTO OBJETO DISH ENVIADO POR EL FRONTEND EN UN JSON
     * @param newDishId OBJETO DishEntity AL CUAL CONVERTIMOS EL DishDTO ENTRANTE
     * @return DEVUELVE UN OBJETO DishResponse (OBTENIDO DE REALTIME POR EL REPOSITORIO DESPUÉS DE HABERLO GUARDADO COMO DishEntity)
     */
    public DishResponse create(DishDTO dishDTO, int newDishId){

        CompletableFuture<DishResponse> futurePlatoResponse = new CompletableFuture<>();

        DishEntity dishEntity = new DishEntity();
        dishEntity.setId(newDishId);
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


    /**
     *
     * @param dishDTO PLATO ENTRANTE (DishDTO)
     *                SERÁ TRANSFORMADO EN UN DishEntity QUE SE MANDARÁ AL REPOSITORIO
     * @return DEVUELVE True O False (PROCEDENTES DEL REPOSITORIO) O UNA EXCEPCION SI DA ALGÚN ERROR.
     */
    public Boolean updateDish(DishDTO dishDTO){

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        DishEntity dishEntity = new DishEntity();
        dishEntity.setUid(dishDTO.getUid());
        dishEntity.setId(dishDTO.getId());
        dishEntity.setName(dishDTO.getName());
        dishEntity.setIngredients(dishDTO.getIngredients());
        dishEntity.setAllergens(dishDTO.getAllergens());
        dishEntity.setImage(dishDTO.getImage());
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


    /**
     *
     * @param uid UID DEL RESTAURANTE CUYOS PLATOS SE QUIEREN OBTENER
     * @return UN OBJETO DishAllResponse (QUE CONTIENE UN ARRAYLIST DE DishResponse) DENTRO DE UN CompletableFuture
     */
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


    /**
     *
     * @param uid UID DEL RESTAURANTE QUE QUIERE ELIMINAR UN PLATO
     * @param id ID DEL PLATO QUE QUIERE ELIMINAR
     * @return SI ÉXITO - True
     *         SI ERROR - False
     */
    public CompletableFuture<Boolean> deleteDish(String uid, int id){
        return dishRepository.delete(uid, id);
    }


    /**
     * OBTIENE LOS DATOS DEL PLATO CUYO ID SE PASA POR PARÁMETRO
     * @param uid UID DEL RESTAURANTE
     * @param id ID DEL PLATO QUE SE QUIERE OBTENER
     * @return DEVUELVE UN DishResponse DENTRO DE UN CompletableFuture
     */
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
                futureDishResponse.completeExceptionally(exception);
            }
        });
        return futureDishResponse;
    }


}
