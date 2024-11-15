package com.example.homiyummy.repository;

import com.example.homiyummy.model.restaurant.RestaurantEntity;
import com.example.homiyummy.model.restaurant.RestaurantReadResponse;
import com.example.homiyummy.model.restaurant.RestaurantResponse;
import com.example.homiyummy.model.user.UserResponse;
import com.google.firebase.database.*;
import org.springframework.stereotype.Repository;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Repository
public class RestaurantRepository {

    private final FirebaseDatabase firebaseDatabase;
    private final  DatabaseReference databaseReference;

    public RestaurantRepository(FirebaseDatabase firebaseDatabase, DatabaseReference databaseReference) {
        this.firebaseDatabase = firebaseDatabase;
        this.databaseReference = databaseReference;
    }

    public void saveRestaurant(RestaurantEntity restaurantEntity, getSaveRestaurantCallback callback) {

        Map<String, Object> restaurantEntityToSave = new HashMap<>(); // GUARDAMOS TOD-O MENOS EL ID

        restaurantEntityToSave.put("email", restaurantEntity.getEmail());
        restaurantEntityToSave.put("name", restaurantEntity.getName());
        restaurantEntityToSave.put("description_mini", restaurantEntity.getDescription_mini());
        restaurantEntityToSave.put("description", restaurantEntity.getDescription());
        restaurantEntityToSave.put("url", restaurantEntity.getUrl());
        restaurantEntityToSave.put("address", restaurantEntity.getAddress());
        restaurantEntityToSave.put("city", restaurantEntity.getCity());
        restaurantEntityToSave.put("phone", restaurantEntity.getPhone());
        restaurantEntityToSave.put("image", restaurantEntity.getImage());
        restaurantEntityToSave.put("foodType", restaurantEntity.getFoodType());
        restaurantEntityToSave.put("schedule", restaurantEntity.getSchedule());

        DatabaseReference restaurantRef = firebaseDatabase.getReference("restaurants").child(restaurantEntity.getUid());

       // System.out.println("UID RestauranteEntity: " + restaurantEntity.getUid());

        restaurantRef.setValue(restaurantEntityToSave, ((databaseError, databaseReference) -> {
            if(databaseError == null) {
                restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        RestaurantResponse restaurantResponse = dataSnapshot.getValue(RestaurantResponse.class);
                        restaurantResponse.setUid(restaurantRef.getKey()); // AÑADO EL UID (Q ES EL NODO) AL UserResponse
                        callback.onRestaurantGot(restaurantResponse);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        callback.onFailure(databaseError.toException());                    }
                });
            }
            else{
                callback.onFailure(databaseError.toException());
            }
        }));
    }

// ----------------------------------------------------------------------------------------------------------------

    // INTERFAZ PARA MANEJAR LA DEVOLUCIÓN DEL RestaurantResponse DESDE EL REPOSITORIO
    public interface getSaveRestaurantCallback{
        void onRestaurantGot(RestaurantResponse restaurantResponse);
        void onFailure(Exception exception);

    }

// ----------------------------------------------------------------------------------------------------------------


    public void updateRestaurantData(RestaurantEntity restaurantEntity, GetUpdateRestaurantCallback callback) {

        DatabaseReference restaurantRef = firebaseDatabase.getReference("restaurants").child(restaurantEntity.getUid());

        RestaurantResponse restaurantResponse = new RestaurantResponse();

        restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    // GUARDAMOS LOS DATOS ACTUALES GUARDADOS EN BASE DE DATOS
                    String currentEmail = dataSnapshot.child("email").getValue(String.class);
                    String currentName = dataSnapshot.child("name").getValue(String.class);
                    String currentDescriptionMini = dataSnapshot.child("description_mini").getValue(String.class);
                    String currentDescription = dataSnapshot.child("description").getValue(String.class);
                    String currentUrl = dataSnapshot.child("url").getValue(String.class);
                    String currentAddress = dataSnapshot.child("address").getValue(String.class);
                    String currentCity = dataSnapshot.child("city").getValue(String.class);
                    String currentPhone = dataSnapshot.child("phone").getValue(String.class);
                    String currentSchedule = dataSnapshot.child("schedule").getValue(String.class);
                    String currentImage = dataSnapshot.child("image").getValue(String.class);
                    String currentFoodType = dataSnapshot.child("foodType").getValue(String.class);

                    RestaurantEntity restaurantEntityToBeSaved = new RestaurantEntity();

                    restaurantEntityToBeSaved.setName(restaurantEntity.getName() != null && !restaurantEntity.getName().isEmpty() ? restaurantEntity.getName() : currentName);
                    restaurantEntityToBeSaved.setDescription_mini(restaurantEntity.getDescription_mini() != null && !restaurantEntity.getDescription_mini().isEmpty() ? restaurantEntity.getDescription_mini() : currentDescriptionMini);
                    restaurantEntityToBeSaved.setDescription(restaurantEntity.getDescription() != null && !restaurantEntity.getDescription().isEmpty() ? restaurantEntity.getDescription() : currentDescription);
                    restaurantEntityToBeSaved.setUrl(restaurantEntity.getUrl() != null && !restaurantEntity.getUrl().isEmpty() ? restaurantEntity.getUrl() : currentUrl);
                    restaurantEntityToBeSaved.setAddress(restaurantEntity.getAddress() != null && !restaurantEntity.getAddress().isEmpty() ? restaurantEntity.getAddress() : currentAddress);
                    restaurantEntityToBeSaved.setCity(restaurantEntity.getCity() != null && !restaurantEntity.getCity().isEmpty() ? restaurantEntity.getCity() : currentCity);
                    restaurantEntityToBeSaved.setPhone(restaurantEntity.getPhone() != null && !restaurantEntity.getPhone().isEmpty() ? restaurantEntity.getPhone() : currentPhone);
                    restaurantEntityToBeSaved.setSchedule(restaurantEntity.getSchedule() != null && !restaurantEntity.getSchedule().isEmpty() ? restaurantEntity.getSchedule() : currentSchedule);
                    restaurantEntityToBeSaved.setImage(restaurantEntity.getImage() != null && !restaurantEntity.getImage().isEmpty() ? restaurantEntity.getImage() : currentImage);
                    restaurantEntityToBeSaved.setFoodType(restaurantEntity.getFoodType() != null && !restaurantEntity.getFoodType().isEmpty() ? restaurantEntity.getFoodType() : currentFoodType);

                    // EL VALOR PARA EMAIL SE MANTIENE EL QUE HABÍA GUARDADO
                    restaurantEntityToBeSaved.setEmail(currentEmail);

                    restaurantRef.setValue(restaurantEntityToBeSaved, ((databaseError, databaseReference) -> {
                        if(databaseError == null) {
                            restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    RestaurantResponse rr = dataSnapshot.getValue(RestaurantResponse.class);

                                    if(!rr.getName().isEmpty() || !rr.getAddress().isEmpty() || !rr.getCity().isEmpty()
                                            || !rr.getPhone().isEmpty() || !rr.getSchedule().isEmpty()
                                            || !rr.getImage().isEmpty() || !rr.getFoodType().isEmpty()){
                                        System.out.println("Restaurante actualizado.");
                                        callback.onSuccess(true);
                                    }
                                    else{
                                        callback.onSuccess(false);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    System.out.println("No ha sido posible confirmar la actualización de los datos del restaurante.");
                                    callback.onFailure(databaseError.toException());
                                }
                            });
                        }
                        else{
                            System.out.println("No ha sido posible la actualización del restaurante.");
                            callback.onFailure(databaseError.toException());
                        }
                    }));


                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }

// ----------------------------------------------------------------------------------------------------------------

    public interface GetUpdateRestaurantCallback{
        void onSuccess(Boolean confirmation);
        void onFailure(Exception exception);
    }

// ----------------------------------------------------------------------------------------------------------------

    // USADO PARA VER SI UN RESTAURANTE EXISTE
    public CompletableFuture<Boolean> existsByUid(String uid) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        DatabaseReference restaurantRef = firebaseDatabase.getReference("restaurants").child(uid);
        restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // Completa el future con `true` si existe, `false` si no existe
                future.complete(snapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });

        return future;
    }
// ----------------------------------------------------------------------------------------------------------------

    public void findByUid(String uid, FindRestaurantCallback callback) {

        DatabaseReference restaurantRef = databaseReference.child("restaurants").child(uid);
        restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    RestaurantReadResponse restaurantReadResponse = dataSnapshot.getValue(RestaurantReadResponse.class);
                    if(!restaurantReadResponse.getName().isEmpty() && !restaurantReadResponse.getEmail().isEmpty()){
                        callback.onSuccess(restaurantReadResponse);
                    }
                    else{
                        //System.out.println("-----------1-----------");
                        RestaurantReadResponse emptyResponse = new RestaurantReadResponse();
                        callback.onFailure(emptyResponse);
                    }
                }
                else{
                    //System.out.println("-----------2-----------");
                    RestaurantReadResponse emptyResponse = new RestaurantReadResponse();
                    callback.onFailure(emptyResponse);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { // FALTA MANEJAR EL ERROR
                RestaurantReadResponse emptyResponse = new RestaurantReadResponse();
                callback.onFailure(emptyResponse);
            }
        });
    }


    public interface FindRestaurantCallback{
        void onSuccess(RestaurantReadResponse response);
        void onFailure(RestaurantReadResponse response);
    }


}

