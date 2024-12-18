package com.example.homiyummy.repository;

import com.example.homiyummy.model.dish.*;
import com.example.homiyummy.service.RestaurantService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Repository
public class DishRepository {

    private final  DatabaseReference databaseReference;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantService restaurantService;
    public DishRepository(DatabaseReference databaseReference, RestaurantRepository restaurantRepository, RestaurantService restaurantService){
        this.databaseReference = databaseReference;
        this.restaurantRepository = restaurantRepository;
        this.restaurantService = restaurantService;
    }


    /**
     *  OBTIENE ÚLTIMO ID GRABADO PARA UN PLATO
     * @param uid IDENTIFICADOR ÚNICO DEL RESTAURANTE
     * @param callback DEVUELVE EL ID DEL ÚLTIMO PLATO GUARDADO O 0 SI NO LO HAY
     */
    public void findId(String uid, FindPlatoIdCallback callback){

        DatabaseReference restaurantRef = databaseReference.child("restaurants").child(uid);
        DatabaseReference dishRef = restaurantRef.child("/dishes");
        DatabaseReference idRef = dishRef.child("/counter");

        idRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Integer id = dataSnapshot.getValue(Integer.class);
                    if (id != null) {
                        callback.onSuccess(id);
                    } else {
                        callback.onSuccess(0); // En caso de datos no válidos
                    }
                }
                else{
                    callback.onSuccess(0);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailure(databaseError, 0);
            }
        });
    }


    /**
     *
     * @param dishEntity OJBETO EN QUE RECIBIMOS EL JSON ENTRANTE (FORMATO DishEntity)
     *                   LO CONVERTIMOS EN UN DishSaveEntity (ES LO MISMO, SIN EL UID DEL RESTAURATE)
     * @param callback DEVUELVE EN UN DishResponse EL PLATO RECIÉN GUARDADO Y RECIÉN OBTENIDO
     */
    public void save(DishEntity dishEntity, SavePlatoCallback callback){

        String uid = dishEntity.getUid();
        DatabaseReference restaurantRef = databaseReference.child("restaurants").child(uid);
        DatabaseReference allDishesRef = restaurantRef.child("dishes");
        DatabaseReference counterRef = allDishesRef.child("counter");
        DatabaseReference itemsRef = allDishesRef.child("items");

        DatabaseReference dishRef = itemsRef.child(String.valueOf(dishEntity.getId()));

        DishSaveEntity dishSaveEntity = new DishSaveEntity(
                dishEntity.getId(),
                dishEntity.getName(),
                dishEntity.getIngredients(),
                dishEntity.getAllergens(),
                dishEntity.getImage(),
                dishEntity.getType());

        counterRef.setValue(dishSaveEntity.getId(), (databaseCounterError, databaseCounterReference) -> {
            if(databaseCounterError != null){
                callback.onFailure(new Exception("Error al actualizar el contador: " + databaseCounterError.getMessage()));
                return;
            }

            dishRef.setValue(dishSaveEntity, ((databaseDishError, databaseDishReference) -> {
                if(databaseDishError != null) {
                    callback.onFailure(new Exception("Error al guardar el plato: " + databaseDishError.getMessage()));
                    return;
                }

                dishRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            DishEntity savedDishEntity = dataSnapshot.getValue(DishEntity.class);
                            if(savedDishEntity != null) {
                                DishResponse dishResponse = new DishResponse(
                                        savedDishEntity.getId(),
                                        savedDishEntity.getName(),
                                        savedDishEntity.getIngredients(),
                                        savedDishEntity.getAllergens(),
                                        savedDishEntity.getImage(),
                                        savedDishEntity.getType());

                                callback.onSuccess(dishResponse);
                            }
                            else {
                                DishResponse dishResponse = new DishResponse();
                                dishResponse.setId(0);
                                callback.onSuccess(dishResponse);
                            }
                        }
                        else {
                            callback.onFailure(new Exception("No se encontró el plato guardado en la base de datos"));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        callback.onFailure(new Exception("Error al leer el plato guardado: " + databaseError.getMessage()));
                    }
                });
            }));
        });
    }


    /**
     * ACTUALIZA UN PLATO YA GUARDADO EN LA BASE DE DATOS
     * PARA HACERLO PRIMERO GUARDA LOS VALORES QUE TIENE GUARDADOS ACTUALMENTE PARA QUE LOS MANTENGA SI NO VIENEN EN EL OBJETO
     * @param dishEntity OBJETO QUE HAY QUE GUARDAR
     * @param callback SI EXITO: true, SI NO, false
     */
    public void update(DishEntity dishEntity, UpdateDishCallback callback){
        DatabaseReference dishRef = databaseReference.child("restaurants")
                .child(dishEntity.getUid())
                .child("dishes/items")
                .child(String.valueOf(dishEntity.getId()));

        dishRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    Integer currentId = dataSnapshot.child("id").getValue(Integer.class);
                    String currentName = dataSnapshot.child("name").getValue(String.class);
                    String currentIngredients = dataSnapshot.child("ingredients").getValue(String.class);
                    String currentImage = dataSnapshot.child("image").getValue(String.class);
                    String currentType = dataSnapshot.child("type").getValue(String.class);
                    ArrayList<String> currentAllergens =  (ArrayList<String>) dataSnapshot.child("allergens").getValue();

                    DishUpdateEntity dishEntityToBeSaved = new DishUpdateEntity();

                    dishEntityToBeSaved.setId(dishEntity.getId() != null && dishEntity.getId() != 0 ? dishEntity.getId() : currentId);
                    dishEntityToBeSaved.setName(dishEntity.getName() != null && !dishEntity.getName().isEmpty() ? dishEntity.getName() : currentName);
                    dishEntityToBeSaved.setIngredients(dishEntity.getIngredients() != null && !dishEntity.getIngredients().isEmpty() ? dishEntity.getIngredients() : currentIngredients);
                    dishEntityToBeSaved.setImage(dishEntity.getImage() != null && !dishEntity.getImage().isEmpty() ? dishEntity.getImage() : currentImage);
                    dishEntityToBeSaved.setType(dishEntity.getType() != null && !dishEntity.getType().isEmpty() ? dishEntity.getType() : currentType);
                    dishEntityToBeSaved.setAllergens(dishEntity.getAllergens() != null  ? dishEntity.getAllergens() : currentAllergens);

                    dishRef.setValue(dishEntityToBeSaved, (databaseError, databaseReference1) -> {
                        if(databaseError != null){
                            callback.onFailure(new Exception("Error al guardar el plato" + databaseError.getMessage()));
                            return;
                        }

                        dishRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    callback.onSuccess(true);
                                }
                                else{
                                    callback.onSuccess(false);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                callback.onFailure(new Exception("Error al leer el plato guardado: " + databaseError.getMessage()));
                            }
                        });
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }


    /**
     * DEVUELVE TODOS LOS PLATOS DEL RESTARUANTE QUE SE PASA POR PARÁMETRO
     * 1º COMPRUEBA QUE EL RESTAURANTE EXISTA CON UNA LLAMADA A existsByUid DE RestaurantService
     * 2º CUANDO TERMINA LA PETICIÓN Y TENEMOS RESPUESTA CONTINUAMOS AQUÍ
     *      SI EXISTE EL RESTAURANTE COMPRUEBA SI TIENE PLATOS.
     *      SI LOS TIENE, DEVUELVE UN DishAllResponse VÍA CALLBACK
     * @param uid UID DEL RESTAURANTE
     *
     * @param callback SI TIENE ÉXITO DEVUELVE UN ArrayList CON TODOS LOS PLATOS EN FORMATO DishAllResponse (O VACÍO)
     */
    public void getAll(String uid, FindAllDishesCallback callback) {

        CompletableFuture<Boolean> futureExists = restaurantService.existsByUid(uid);

        futureExists.thenAccept(exists -> {
            if (exists) {

                DatabaseReference dishesRef = databaseReference.child("restaurants")
                        .child(uid)
                        .child("dishes/items");

                dishesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            ArrayList<DishResponse> allDishes = new ArrayList<>();
                            for (DataSnapshot dishSnapshot : dataSnapshot.getChildren()) {
                                DishResponse dish = dishSnapshot.getValue(DishResponse.class);
                                if (dish != null) {
                                    allDishes.add(dish);
                                }
                            }
                            DishAllResponse dishAllResponse = new DishAllResponse();
                            dishAllResponse.setDishes(allDishes);
                            callback.onSuccess(dishAllResponse);
                        } else {
                            DishAllResponse emptyResponse = new DishAllResponse();
                            emptyResponse.setDishes(new ArrayList<>());
                            callback.onSuccess(emptyResponse);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        callback.onFailure(new Exception("Error al leer los platos: " + databaseError.getMessage()));
                    }
                });
            } else {
                DishAllResponse emptyResponse = new DishAllResponse();
                emptyResponse.setDishes(new ArrayList<>());
                callback.onSuccess(emptyResponse);
            }
        }).exceptionally(ex -> {
            callback.onFailure(new Exception("Error al verificar la existencia del restaurante: " + ex.getMessage()));
            return null;
        });
    }


    /**
     * ELIMINA UN PLATO DE LA BASE DE DATOS
     * @param uid UID DEL RESTAURANTE EN AUTHENTICATION
     * @param dishId ID DLE PLATO EN EL NODO DEL RESTAURANTE
     * @return DEVUELVE EN UN CompletableFuture True SI TUVO ÉXITO O false SI NO LO TUVO
     */
    public CompletableFuture<Boolean> delete(String uid, int dishId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        DatabaseReference dishRef = databaseReference.child("restaurants")
                .child(uid)
                .child("dishes/items")
                .child(String.valueOf(dishId));

        dishRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    dishRef.removeValue((databaseError, databaseReference1) -> {
                        if (databaseError != null) {
                            future.completeExceptionally(databaseError.toException());
                        } else {
                            future.complete(true);
                        }
                    });
                } else {
                    future.complete(false); // Objeto no encontrado
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }


    /**
     * RECUPERA DE LA BSE DE DATOS UN OBJETO DishEntity
     * @param uid  UID DEL RESTAURANTE EN AUTHENTICATION
     * @param id   ID DEL PLATO EN EL RESTAURANTE
     * @param callback SI ÉXITO DEVUELVE UN OBJETO DishEntity
     *                 SI NO EXITO DEVUELVE UN DishEntity vacío
     */
    public void get(String uid, int id, OnDishGotCallback callback){

        DatabaseReference restaurantRef = databaseReference.child("restaurants").child(uid);
        DatabaseReference dishesRef = restaurantRef.child("dishes/items");
        dishesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    DishEntity dishEntity = dataSnapshot.child(String.valueOf(id)).getValue(DishEntity.class);
                    callback.onSuccess(dishEntity);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                    callback.onSuccess(new DishEntity());
            }
        });

    }


    public interface SavePlatoCallback{
        void onSuccess(DishResponse dishResponse);
        void onFailure(Exception exception);
    }


    public interface FindPlatoIdCallback{
        void onSuccess(Integer id);
        void onFailure(DatabaseError exception, Integer id);
    }


    public interface UpdateDishCallback{
        void onSuccess(Boolean result);
        void onFailure(Exception exception);
    }


    public interface FindAllDishesCallback{
        void onSuccess(DishAllResponse allDish);
        void onFailure(Exception exception);
    }


    public interface OnDishGotCallback{
        void onSuccess(DishEntity dishEntity);
        void onFailure(Exception exception);
    }


}
