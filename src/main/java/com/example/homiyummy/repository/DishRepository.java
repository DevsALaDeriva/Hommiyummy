package com.example.homiyummy.repository;

import com.example.homiyummy.model.dish.*;
import com.example.homiyummy.service.RestaurantService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
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

    // ----------------------------------------------------------------------------------------------------------------

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
                //System.err.println("Error al obtener el ID: " + databaseError.getMessage());
            }
        });
    }

    // ----------------------------------------------------------------------------------------------------------------

    public void save(DishEntity dishEntity, SavePlatoCallback callback){

        String uid = dishEntity.getUid();                                                                       // UID DEL RESTAURANTE QUE GUARDA EL PLATO
        DatabaseReference restaurantRef = databaseReference.child("restaurants").child(uid);
        DatabaseReference allDishesRef = restaurantRef.child("dishes");                                   // NODO DONDE SE GUARDAN LOS PLATOS DEL RESTAURANTE
        DatabaseReference counterRef = allDishesRef.child("counter");                                     // NODO DONDE ESTÁ EL CONTADOR DE PLATOS DEL RESTAURANTE
        DatabaseReference itemsRef = allDishesRef.child("items");

        DatabaseReference dishRef = itemsRef.child(String.valueOf(dishEntity.getId()));                        // CREAMOS EL NODO DEL NUEVO PLATO CON SU PROPIO ID

        DishSaveEntity dishSaveEntity = new DishSaveEntity(
                dishEntity.getId(),
                dishEntity.getName(),
                dishEntity.getIngredients(),
                dishEntity.getAllergens(),
                dishEntity.getImage(),
                dishEntity.getType());

        counterRef.setValue(dishSaveEntity.getId(), (databaseCounterError, databaseCounterReference) -> {                // AUNQUE NO TIENE QUE VER CON LA FOTO DEL PLATO, APROVECHAMOS PARA ASIGNAR EL NUEVO ID A CONTADOR (PARA QUE LO TENGA DE REFERENCIA EL PRÓXIMO PLATO Q SE GUARDE)
            if(databaseCounterError != null){
                callback.onFailure(new Exception("Error al actualizar el contador: " + databaseCounterError.getMessage()));
                return;
            }

            dishRef.setValue(dishSaveEntity, ((databaseDishError, databaseDishReference) -> {
                if(databaseDishError != null) {
                    callback.onFailure(new Exception("Error al guardar el plato: " + databaseDishError.getMessage()));
                    return;
                }

                dishRef.addListenerForSingleValueEvent(new ValueEventListener() {                                       // FOTO DE ESE PLATO
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
                                callback.onSuccess(dishResponse); // DEVUELVO UN DISRESPONSE VACIO, CON EL ID:0
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

    // -----------------------------------------------------------------------------------------------------------------

    public void update(DishEntity dishEntity, UpdateDishCallback callback){
        //System.out.println("asfasdfasdfasdfasfd");
        DatabaseReference dishRef = databaseReference.child("restaurants")
                .child(dishEntity.getUid())
                .child("dishes/items")
                .child(String.valueOf(dishEntity.getId()));

        dishRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    ArrayList<String> currentAllergens =  (ArrayList<String>) dataSnapshot.child("allergens").getValue();

                    DishUpdateEntity dishEntityToBeSaved = new DishUpdateEntity(); // PLATO A GUARDAR

                    dishEntityToBeSaved.setId(dishEntity.getId());
                    dishEntityToBeSaved.setName(dishEntity.getName());
                    dishEntityToBeSaved.setIngredients(dishEntity.getIngredients());
                    dishEntityToBeSaved.setAllergens(dishEntity.getAllergens());
                    dishEntityToBeSaved.setImage(dishEntity.getImage());
                    dishEntityToBeSaved.setType(dishEntity.getType());

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
                            // TODO -------------
            }
        });
    }

    // ----------------------------------------------------------------------------------------------------------------

    public void getAll(String uid, FindAllDishesCallback callback) {

        CompletableFuture<Boolean> futureExists = restaurantService.existsByUid(uid); // MÉTOD O ASÍNCRONO QUE COMPRUEBA SI EL RESTAURANTE EXISTE

        futureExists.thenAccept(exists -> { // LA MEJOR SOLUCIÓN. ENCADENAR EL RESULTADO DEL FUTURO  -----
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
                            callback.onSuccess(emptyResponse); // SI NO HAY PLATOS OBJETO VACÍO
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
                callback.onSuccess(emptyResponse); // SI EL RESTAURANTE NO EXISTE DEVOLVEMOS UN OBJETO VACÍO
            }
        }).exceptionally(ex -> {
            callback.onFailure(new Exception("Error al verificar la existencia del restaurante: " + ex.getMessage())); // MANEJAMSO CUALQUIER EXCPECIÓN
            return null;
        });
    }

    // ----------------------------------------------------------------------------------------------------------------

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

    // ----------------------------------------------------------------------------------------------------------------

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
                    callback.onSuccess(new DishEntity()); // MANDAMOS UN DISH VACÍO
            }
        });

    }

    // ----------------------------------------------------------------------------------------------------------------

    public interface SavePlatoCallback{
        void onSuccess(DishResponse dishResponse);
        void onFailure(Exception exception);
    }

    // ----------------------------------------------------------------------------------------------------------------

    public interface FindPlatoIdCallback{
        void onSuccess(Integer id);
        void onFailure(DatabaseError exception, Integer id);
    }

    // ----------------------------------------------------------------------------------------------------------------

    public interface UpdateDishCallback{
        void onSuccess(Boolean result);
        void onFailure(Exception exception);
    }

    // ----------------------------------------------------------------------------------------------------------------

    public interface FindAllDishesCallback{
        void onSuccess(DishAllResponse allDish);
        void onFailure(Exception exception);
    }

    // ----------------------------------------------------------------------------------------------------------------

    public interface DeleteDishCallback{
        void onSuccess(DishDeleteResponse dishDeleteResponse);
        void onFailure(Exception exception);
    }

    // ----------------------------------------------------------------------------------------------------------------

    public interface OnDishGotCallback{
        void onSuccess(DishEntity dishEntity);
        void onFailure(Exception exception);
    }

    // ----------------------------------------------------------------------------------------------------------------

}
