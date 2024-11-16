package com.example.homiyummy.repository;

import com.example.homiyummy.model.dish.DishEntity;
import com.example.homiyummy.model.dish.DishResponse;
import com.example.homiyummy.model.dish.DishSaveEntity;
import com.example.homiyummy.model.dish.DishUpdateEntity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public class DishRepository {

    private final  DatabaseReference databaseReference;

    public DishRepository(DatabaseReference databaseReference){
        this.databaseReference = databaseReference;
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
                System.err.println("Error al obtener el ID: " + databaseError.getMessage());
            }
        });
    }

    // ----------------------------------------------------------------------------------------------------------------

    public void save(DishEntity dishEntity, SavePlatoCallback callback){

        String uid = dishEntity.getUid();                                                                       // UID DEL RESTAURANTE QUE GUARDA EL PLATO
        DatabaseReference restaurantRef = databaseReference.child("restaurants").child(uid);
        DatabaseReference dishesRef = restaurantRef.child("dishes");                                   // NODO DONDE SE GUARDAN LOS PLATOS DEL RESTAURANTE
        DatabaseReference counterRef = dishesRef.child("counter");                                     // NODO DONDE ESTÁ EL CONTADOR DE PLATOS DEL RESTAURANTE
        DatabaseReference itemsRef = dishesRef.child("items");

        DatabaseReference dishRef = itemsRef.child(String.valueOf(dishEntity.getId()));                        // CREAMOS EL NODO DEL NUEVO PLATO CON SU PROPIO ID

        DishSaveEntity dishSaveEntity = new DishSaveEntity(
                dishEntity.getId(),
                dishEntity.getName(),
                dishEntity.getIngredients(),
                dishEntity.getAllergens(),
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
                                DishResponse dishResponse = new DishResponse(savedDishEntity.getUid(),
                                        savedDishEntity.getId(),
                                        savedDishEntity.getName(),
                                        savedDishEntity.getIngredients(),
                                        savedDishEntity.getAllergens(),
                                        savedDishEntity.getType());
                                callback.onSuccess(dishResponse);
                            }
                            else {
                                callback.onFailure(new Exception("El plato guardado es nulo"));
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
    System.out.println("asfasdfasdfasdfasfd");
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
                    dishEntityToBeSaved.setType(dishEntity.getType());

                    dishEntityToBeSaved.setAllergens(dishEntity.getAllergens() != null  ? dishEntity.getAllergens() : currentAllergens);

                    dishRef.setValue(dishEntityToBeSaved, (databaseError, databaseReference1) -> {
                        if(databaseError != null){


                            // TODO -------------

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
                                // TODO -------------
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

}
