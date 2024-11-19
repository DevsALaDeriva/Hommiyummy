package com.example.homiyummy.repository;

import com.example.homiyummy.model.dish.DishEntity;
import com.example.homiyummy.model.restaurant.RestaurantEntity;
import com.example.homiyummy.model.restaurant.RestaurantReadResponse;
import com.example.homiyummy.model.restaurant.RestaurantResponse;
import com.google.firebase.database.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


@Repository
public class RestaurantRepository {

    private final FirebaseDatabase firebaseDatabase;
    private final  DatabaseReference databaseReference;

    public RestaurantRepository(FirebaseDatabase firebaseDatabase, DatabaseReference databaseReference) {
        this.firebaseDatabase = firebaseDatabase;
        this.databaseReference = databaseReference;
    }

    // ----------------------------------------------------------------------------------------------------------------

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
        restaurantEntityToSave.put("food_type", restaurantEntity.getFood_type());
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
                    String currentFoodType = dataSnapshot.child("food_type").getValue(String.class);

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
                    restaurantEntityToBeSaved.setFood_type(restaurantEntity.getFood_type() != null && !restaurantEntity.getFood_type().isEmpty() ? restaurantEntity.getFood_type() : currentFoodType);

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
                                            || !rr.getImage().isEmpty() || !rr.getFood_type().isEmpty()){
                                        //System.out.println("Restaurante actualizado.");
                                        callback.onSuccess(true);
                                    }
                                    else{
                                        callback.onSuccess(false);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    //System.out.println("No ha sido posible confirmar la actualización de los datos del restaurante.");
                                    callback.onFailure(databaseError.toException());
                                }
                            });
                        }
                        else{
                            //System.out.println("No ha sido posible la actualización del restaurante.");
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
    public void exists(String uid, ExistsRestaurantCallback callback) {

       // CompletableFuture<Boolean> future = new CompletableFuture<>();
        DatabaseReference restaurantRef = firebaseDatabase.getReference("restaurants").child(uid);
        restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    // Completa el future con `true` si existe, `false` si no existe
                    callback.onSuccess(true);
                }
                else{
                    callback.onSuccess(false);
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onSuccess(false);
                //future.completeExceptionally(error.toException());
            }
        });

        //return future;
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

    public interface ExistsRestaurantCallback{
        void onSuccess(Boolean response);
        void onFailure(Exception exception);
    }

// ----------------------------------------------------------------------------------------------------------------

    public void getAllRestaurantList(OnRestaurantListCallback callback){

        DatabaseReference allRestaurantsRef = databaseReference.child("restaurants");

        allRestaurantsRef.addListenerForSingleValueEvent(new ValueEventListener() {                                     // UBICACIÓN DEL NODO "restaurants"
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    ArrayList<RestaurantEntity> restaurantList = new ArrayList<>();                                     // ARRAY DONDE GUARDAREMOS TODOS LOS RESTAURANTES
                    int totalRestaurants = (int)dataSnapshot.getChildrenCount();
                    int[] contados = {0};
                    //AtomicInteger contadorAtomic = new AtomicInteger(0);                                      //USAMOS AtomicInteger PARA MANEJAR EL CONTADOR DE FORMA SEGURA EN UN ENTORNO ASÍNCRONO
                    for(DataSnapshot restaurantSnapshot: dataSnapshot.getChildren()){                                   // RECORRO EL SNAPSHOT DEL NODO "RESTAURANTS"
                        String uid = restaurantSnapshot.getKey();                                                       // OBTENEMOS EL UID DEL RESTAURANTE ( QUE ES SU KEY)
                            //System.out.println(restaurantSnapshot.getKey());
                        DatabaseReference eachRestaurantRef = allRestaurantsRef.child(uid);                             // OBTENEMOS LA REFERENCIA DE CADA RESTAURANTE.
                        eachRestaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {                     // APUNTAMOS A LA REF DEL RESTAURANTE
                            @Override
                            public void onDataChange(DataSnapshot restaurantSnapshot) {

                                if(restaurantSnapshot.exists()){

                                    String uid = restaurantSnapshot.getKey();                                           // GUARDAMOS EL VALOR DE CADA PROPIEDAD DEL RESTAURANTE
                                    String email = restaurantSnapshot.child("email").getValue(String.class);
                                    String name = restaurantSnapshot.child("name").getValue(String.class);
                                    String description_mini = restaurantSnapshot.child("description_mini").getValue(String.class);
                                    String description = restaurantSnapshot.child("description").getValue(String.class);
                                    String url = restaurantSnapshot.child("url").getValue(String.class);
                                    String address = restaurantSnapshot.child("address").getValue(String.class);
                                    String city = restaurantSnapshot.child("city").getValue(String.class);
                                    String phone = restaurantSnapshot.child("phone").getValue(String.class);
                                    String schedule = restaurantSnapshot.child("schedule").getValue(String.class);
                                    String image = restaurantSnapshot.child("image").getValue(String.class);
                                    String food_type = restaurantSnapshot.child("food_type").getValue(String.class);

                                    // FALTAN LOS PLATOS
                                    //ArrayList<DishResponse> dishes = new ArrayList<>();
                                    ArrayList<DishEntity> dishes = new ArrayList<>();
                                    DataSnapshot dishesSnapshot = restaurantSnapshot.child("dishes/items");        // COMO YA TENEMOS EL DATASNAPSHOT PRINCIPAL, DESDE EL PODEMOS ACCEDER A "HIJOS" SIN TENER QUE VOLVER A HACER UNA PETICIÓN A LA BBDD

                                    if(dishesSnapshot.exists()) {
                                        for(DataSnapshot dishSnapSoht : dishesSnapshot.getChildren()){
                                            int id = dishSnapSoht.child("id").getValue(Integer.class);
                                            String dishName = dishSnapSoht.child("name").getValue(String.class);
                                            String ingredients = dishSnapSoht.child("ingredients").getValue(String.class);

                                            DataSnapshot allergensSnapshot = dishSnapSoht.child("allergens");      // CREO OTRA IMAGEN PARTIENDO DE LA QUE YA TENÍA SIN TENER QUE HACER OTRA PETICIÓN A BBDD
                                            ArrayList<String> allergens = new ArrayList<>();

                                            if(allergensSnapshot.exists()){
                                                for(DataSnapshot allergen : allergensSnapshot.getChildren()){
                                                    allergens.add(allergen.getValue().toString());
                                                }
                                            }

                                            String dishImage = dishSnapSoht.child("image").getValue(String.class);
                                            String dishType = dishSnapSoht.child("type").getValue(String.class);
                                            // 1º CREAMOS EL DISH ENTITY PQ LO EXTRAEMOS DE LA BBDD
                                            DishEntity dishEntity = new DishEntity(id, dishName, ingredients, allergens, dishImage, dishType);

                                            // 2º PASAMOS EL ENTITY A RESPONSE
//                                            DishResponse dishResponse = new DishResponse(); // CREAMOS UN DISH RESPONSE PARA GUARDAR EL ENTITY
//                                            dishResponse.setUid(dishEntity.getUid());
//                                            dishResponse.setId(dishEntity.getId());
//                                            dishResponse.setName(dishEntity.getName());
//                                            dishResponse.setIngredients(dishEntity.getIngredients());
//                                            dishResponse.setAllergens(dishEntity.getAllergens());
//                                            dishResponse.setImage(dishEntity.getImage());
//                                            dishResponse.setType(dishEntity.getType());

                                            //dishes.add(dishResponse); // AÑADIMOS EL RESPONSE AL ARRAY
                                            dishes.add(dishEntity); // AÑADIMOS EL RESPONSE AL ARRAY
                                        }
                                    }

                                    RestaurantEntity restaurantEntity = new RestaurantEntity(
                                            uid, email, name, description_mini, description, url, address,
                                            city, phone, schedule, image, food_type, dishes);

//                                    RestaurantResponse restaurantResponse = new RestaurantResponse();
//                                    restaurantResponse.setUid(restaurantEntity.getUid());
//                                    restaurantResponse.setEmail(restaurantEntity.getEmail());
//                                    restaurantResponse.setName(restaurantEntity.getName());
//                                    restaurantResponse.setDescription_mini(restaurantEntity.getDescription_mini());
//                                    restaurantResponse.setDescription(restaurantEntity.getDescription());
//                                    restaurantResponse.setUrl(restaurantEntity.getUrl());
//                                    restaurantResponse.setAddress(restaurantEntity.getAddress());
//                                    restaurantResponse.setCity(restaurantEntity.getCity());
//                                    restaurantResponse.setPhone(restaurantEntity.getPhone());
//                                    restaurantResponse.setSchedule(restaurantEntity.getSchedule());
//                                    restaurantResponse.setImage(restaurantEntity.getImage());
//                                    restaurantResponse.setFood_type(restaurantEntity.getFood_type());
//                                    restaurantResponse.setDishes(restaurantEntity.getDishes());
//                                    restaurantList.add(restaurantResponse);
                                      restaurantList.add(restaurantEntity);                                             // AÑADIMOS EL RESTAURANTE AL ARRAY
                                }

                                // Incrementar contador y verificar
                                //int currentCount = contadorAtomic.incrementAndGet(); // AUMENTO EN 1 LOS RESTAURANTES PROCESADOS Y GUARDAMOS RESULTADO
                                //System.out.println("Restaurantes procesados: " + currentCount + " de " + totalRestaurants);

                                contados[0]++;
                                //System.out.println("Restaurantes procesados: " + contados[0]++ + " de " + totalRestaurants);
                                //contadorAtomic.incrementAndGet();
                                //System.out.println("2 Total restaurantes: " + totalRestaurants);
                                //System.out.println("2 Total contados: " + contadorAtomic.get());
                                //System.out.println(contadorAtomic.get() == totalRestaurants);

                                if (contados[0] == totalRestaurants) {                                                // SOLO SEGUIMOS CUANDO HEMOS AÑADIDO TODOS
                                    //System.out.println("Todos los restaurantes procesados. Llamando al callback."); // ---- Añadido para depuración
                                    //RestaurantAllResponse restaurantAllResponse = new RestaurantAllResponse(); // CREO EL OBJETO RESPUESTA QUE EL FRONTEND ENVIARÁ
                                    //restaurantAllResponse.setRestaurantResponses(restaurantList);
                                    //callback.onSearchingSuccess(restaurantAllResponse);
                                    callback.onSearchingSuccess(restaurantList);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // TODO --------> FALTA
                                System.out.println("-----------2-----------");
                            }
                        });
                    }
                }
                else{
                    System.out.println("-----------3-----------");
                    callback.onSearchingSuccess(new ArrayList<>());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("-----------4-----------");
                callback.onSearchingFailure(new Exception());
            }
        });
    }

    // ----------------------------------------------------------------------------------------------------------------

    public interface OnRestaurantListCallback{
        void onSearchingSuccess(ArrayList<RestaurantEntity> restaurants);
        void onSearchingFailure(Exception exception);
    }

    // ----------------------------------------------------------------------------------------------------------------

    // ----------------------------------------------------------------------------------------------------------------



}

