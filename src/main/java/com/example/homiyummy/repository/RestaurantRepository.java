package com.example.homiyummy.repository;

import com.example.homiyummy.model.dish.DishEntity;
import com.example.homiyummy.model.dish.DishGetByEntity;
import com.example.homiyummy.model.menu.MenuEntity;
import com.example.homiyummy.model.menu.MenuGetAllMenusEntity;
import com.example.homiyummy.model.menu.MenuGetByUrlEntity;
import com.example.homiyummy.model.restaurant.*;
import com.example.homiyummy.model.reviews.ReviewsEntity;
import com.google.firebase.database.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class RestaurantRepository {

    private final FirebaseDatabase firebaseDatabase;
    private final  DatabaseReference databaseReference;

    public RestaurantRepository(FirebaseDatabase firebaseDatabase, DatabaseReference databaseReference) {
        this.firebaseDatabase = firebaseDatabase;
        this.databaseReference = databaseReference;
    }


    /**
     * CONVERTIMOS EL OBJETO RestaurantENtity EN UN MAP, QUE SERÁ LO QUE GUARDAMOS
     * @param restaurantEntity OBJETO A GUARDAR
     * @param callback SI EXITO -> DEVUELVE UN OBJETO RestaurantResponse
     */
    public void saveRestaurant(RestaurantEntity restaurantEntity, getSaveRestaurantCallback callback) {

        // CREAMOS UN MAP CON LAS PROPIEDADES DEL RESTAURANTE QUE GUARDAREMOS EN REALTIIME
        Map<String, Object> restaurantEntityToSave = new HashMap<>();

        // ASIGNAMOS VALOR A LAS PROPIEDADES EXTRAYÉNDOLAS DEL OBJETO ENTRANTE
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
        restaurantEntityToSave.put("location", restaurantEntity.getLocation());
        restaurantEntityToSave.put("uid", restaurantEntity.getUid());

        // OBTENEMOS LA REFERENCIA EN BASE DE DATOS DEL RESTAURANTE
        DatabaseReference restaurantRef = firebaseDatabase.getReference("restaurants").child(restaurantEntity.getUid());

        // GUARDAMOS EL RESTAURANTE (EL MAP) EN REALTIME
        restaurantRef.setValue(restaurantEntityToSave, ((databaseError, databaseReference) -> {

            // SI NO HAY ERROR
            if(databaseError == null) {
                // ACCEDEMOS AL LUGAR DONDE HEMOS GUARDADO EL RESTAURANTE
                restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // GUARDAMOS EN UN OBJETO RestaurantResponse EL RESTAURANTE EXTRAYENDOLO DE BBDD
                        RestaurantResponse restaurantResponse = dataSnapshot.getValue(RestaurantResponse.class);
                        // AÑADIMOS EL UID AL OBJETO
                        restaurantResponse.setUid(restaurantRef.getKey());
                        // LO ENVIAMOS AL SERVICE
                        callback.onRestaurantGot(restaurantResponse);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // ENVIAMOS UNA EXCEPTICION AL SERVICE
                        callback.onFailure(databaseError.toException());                    }
                });
            }
            else{
                // SI HA HABIDO UN ERROR MANDAMOS UNA EXCEPTION AL SERVICE
                callback.onFailure(databaseError.toException());
            }
        }));
    }

    // INTERFAZ PARA GESTIONAR EL CALLBACK
    public interface getSaveRestaurantCallback{
        void onRestaurantGot(RestaurantResponse restaurantResponse);
        void onFailure(Exception exception);

    }


    /**
     *   ACTUALIZAD UN RESTAURANTE EN LA BASE DE DATOS
     *   USAMOS UN MAPA PQ QUEREMOS CONSERVAR PROPIEDADES QUE NO SE INCLUYEN EN EL OBJETO QUE LLEGA
     *   ESTO CONSERVA EL VALOR DE LOS NODOS COMPLEJOS PQ 1º TOMAMOS SU VALOR Y 2º LO GUARDAMOS EN EL MAPA
     * @param restaurantEntity OBJETO CON LOS DATOS DEL RESTAURANTE
     * @param callback DEVUELVE true SI TIENE EXITO
     */
    public void updateRestaurantData(RestaurantEntity restaurantEntity, GetUpdateRestaurantCallback callback) {

        // OBTENEMOS LA REFERENCIA EN BASE DE DATOS DEL RESTAURANTE
        DatabaseReference restaurantRef = firebaseDatabase.getReference("restaurants").child(restaurantEntity.getUid());

        // ACCEDEMOS AL RESTAURANTE
        restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // SI EXISTE...
                if(dataSnapshot.exists()){
                    // CREAMOS UN MAP
                    Map<String, Object> updates = new HashMap<>();
                    // RECUPERAMOS LOS DATOS GUARDADOS SI LOS HUBIERA DE ESTAS PROPIEDADES
                    // SI HAY PLATOS...
                    if (dataSnapshot.child("dishes").exists()) {
                        updates.put("dishes", dataSnapshot.child("dishes").getValue());
                    }
                    // SI HAY MENUS...
                    if (dataSnapshot.child("menus").exists()) {
                        updates.put("menus", dataSnapshot.child("menus").getValue());
                    }
                    // SI HAY PEDIDOS...
                    if (dataSnapshot.child("orders").exists()) {
                        updates.put("orders", dataSnapshot.child("orders").getValue());
                    }

                    // SOLO METEMOS EN EL MAPA LAS PROPIEDADES QUE CONTIENEN ALGO EN EL OBJETO restaurantEntity QUE LLEGA
                    if(restaurantEntity.getEmail() != null && !restaurantEntity.getEmail().isEmpty())
                        updates.put("email", restaurantEntity.getEmail());
                    if (restaurantEntity.getName() != null && !restaurantEntity.getName().isEmpty())
                        updates.put("name", restaurantEntity.getName());
                    if (restaurantEntity.getDescription_mini() != null && !restaurantEntity.getDescription_mini().isEmpty())
                        updates.put("description_mini", restaurantEntity.getDescription_mini());
                    if (restaurantEntity.getDescription() != null && !restaurantEntity.getDescription().isEmpty())
                        updates.put("description", restaurantEntity.getDescription());
                    if (restaurantEntity.getUrl() != null && !restaurantEntity.getUrl().isEmpty())
                        updates.put("url", restaurantEntity.getUrl());
                    if (restaurantEntity.getAddress() != null && !restaurantEntity.getAddress().isEmpty())
                        updates.put("address", restaurantEntity.getAddress());
                    if (restaurantEntity.getCity() != null && !restaurantEntity.getCity().isEmpty())
                        updates.put("city", restaurantEntity.getCity());
                    if (restaurantEntity.getPhone() != null && !restaurantEntity.getPhone().isEmpty())
                        updates.put("phone", restaurantEntity.getPhone());
                    if (restaurantEntity.getSchedule() != null && !restaurantEntity.getSchedule().isEmpty())
                        updates.put("schedule", restaurantEntity.getSchedule());
                    if (restaurantEntity.getImage() != null && !restaurantEntity.getImage().isEmpty())
                        updates.put("image", restaurantEntity.getImage());
                    if (restaurantEntity.getFood_type() != null && !restaurantEntity.getFood_type().isEmpty())
                        updates.put("food_type", restaurantEntity.getFood_type());
                    if (restaurantEntity.getLocation() != null)
                        updates.put("location", restaurantEntity.getLocation());

                    // ACTUALIZAMOS PROPIEDADES
                    restaurantRef.updateChildren(updates, (databaseError, databaseReference) -> {
                        if (databaseError == null) {
                            // SI NO HAY ERROR MANDAMOS true AL SERVICE
                            callback.onSuccess(true);
                        } else {
                            // SI HAY ERROR MANDAMOS EXCEPTION
                            callback.onFailure(databaseError.toException());
                        }
                    });
                } else {
                    // SI NO EXISTE MANDAMOS EXCEPTION
                    callback.onFailure(new Exception("Restaurante no encontrado."));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }

    // INTERFAZ PARA GESTIONAR EL CALLBACK QUE IMPLEMENTARÁ EL SERVICE
    public interface GetUpdateRestaurantCallback{
        void onSuccess(Boolean confirmation);
        void onFailure(Exception exception);
    }


    // COMPRUEBA SI EXISTE UN UID EN BASE DE DATOS Y RESPONDE AL SERVICE
    public void exists(String uid, ExistsRestaurantCallback callback) {

        // ACCEDEMOS A LA REFERENCIA DEL RESTAURANTE CON EL UID RECIBIDO
        DatabaseReference restaurantRef = firebaseDatabase.getReference("restaurants").child(uid);

        // VAMOS A ESA REFERENCIA
        restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    // SI EXISTE MANDAMOS true AL SERVICE
                    callback.onSuccess(true);
                }
                else{
                    // SI NO EXISTE, MANDAMOS false
                    callback.onSuccess(false);
                }
            }

            // POR UN ERROR DE ACCESO O CUALQUIER OTRO MANDAMO UN false
            @Override
            public void onCancelled(DatabaseError error) {
                callback.onSuccess(false);
            }
        });
    }


    // OBTENEMOS EL RESTAURANTE (SI EXISTE) DE REALTIME PARA EL UID APORTADO
    public void findByUid(String uid, FindRestaurantCallback callback) {

        // ACCEDEMOS A LA REFERENCIA DEL RESTAURANTE CON EL UID RECIBIDO
        DatabaseReference restaurantRef = databaseReference.child("restaurants").child(uid);

        // VAMOS A ESA REFERENCIA
        restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){ // SI EXISTE
                    // EXTRAEMOS EL RESTAURANTE OBTENIÉNDOLO DE REALTIME
                    RestaurantReadResponse restaurantReadResponse = dataSnapshot.getValue(RestaurantReadResponse.class);

                    // SI CONTIENE DATOS
                    if(!restaurantReadResponse.getName().isEmpty() && !restaurantReadResponse.getEmail().isEmpty()){
                        // LO MANDAMOS AL SERVICE
                        callback.onSuccess(restaurantReadResponse);
                    }
                    // SI NO CONTIENE DATOS
                    else{
                        // MANDAMOS AL SERVICE UN OBJETO VACÍO
                        RestaurantReadResponse emptyResponse = new RestaurantReadResponse();
                        callback.onFailure(emptyResponse);
                    }
                }
                else{ // SI NO EXISTE
                    // MANDAMOS AL SERVICE UN OBJETO VACÍO
                    RestaurantReadResponse emptyResponse = new RestaurantReadResponse();
                    callback.onFailure(emptyResponse);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // PARA CUALQUIER ERROR DE CONEXIÓN, ETC..
                // MANDAMOS AL SERVICE UN OBJETO VACÍO
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


    /**
     *
     * @param callback SI EXITO -> DEVUELVE TODOS LOS RESTAURANTES QUE HAY EN FORMATO RestaurantEntity
     *                 SI ERROR -> DEVUELVE UNA EXCEPCIÓN QUE VERÁ EL SERVICIO
     */
    public void getAllRestaurantList(OnRestaurantListCallback callback){

        // GURADAMOS LA REFERENCIA DE TODOS LOS RESTAURANTE
        DatabaseReference allRestaurantsRef = databaseReference.child("restaurants");

        // ACCEDEMOS A ESA REFERENCIA
        allRestaurantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // SI EXISTE
                if(dataSnapshot.exists()){

                    // CREO UN ARRAY DONDE GUARDAR LOS RESTAURANTES QUE HAYA
                    ArrayList<RestaurantEntity> restaurantList = new ArrayList<>();

                    // GUARDAMOS EL Nº DE RESTAURANTES QUE HAY
                    int totalRestaurants = (int)dataSnapshot.getChildrenCount();
                    // CREAMOS UN CONTADOR
                    int[] contados = {0};

                    // BUCLE PARA RECORRER TODOS LOS RESTAURANTES
                    for(DataSnapshot restaurantSnapshot: dataSnapshot.getChildren()){
                        String uid = "";
                        // SI EXISTE ESTE RESTAURATNE
                        if(restaurantSnapshot.exists()){
                            // SE OBTIENEN TODOS SUS DATOS
                            uid = restaurantSnapshot.getKey();
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
                            Float average_price = restaurantSnapshot.child("average_price").getValue(Float.class);
                            RestaurantLocation location = restaurantSnapshot.child("location").getValue(RestaurantLocation.class);

                            // SE OBTIENE LA NOTA MEDIA
                            int average_rate = 0;
                            DataSnapshot ordersSnapshot = restaurantSnapshot.child("orders/items");
                            if(ordersSnapshot.exists()){
                                int sumOfRates = 0;
                                int numberOfReviews = 0;
                                for(DataSnapshot order : ordersSnapshot.getChildren()){
                                    if(order.child("reviews/rate").exists()){
                                        int orderRate = order.child("reviews/rate").getValue(Integer.class);
                                        sumOfRates += orderRate;
                                        numberOfReviews++;
                                    }
                                    average_rate = Math.round((float)sumOfRates / numberOfReviews);
                                }

                            }

                            // SE OBTIENEN SUS PLATOS
                            ArrayList<DishEntity> dishes = new ArrayList<>();
                            DataSnapshot dishesSnapshot = restaurantSnapshot.child("dishes/items");
                            if(dishesSnapshot.exists()) {

                                for(DataSnapshot dishSnapSoht : dishesSnapshot.getChildren()){
                                    int id = dishSnapSoht.child("id").getValue(Integer.class);
                                    String dishName = dishSnapSoht.child("name").getValue(String.class);
                                    String ingredients = dishSnapSoht.child("ingredients").getValue(String.class);

                                    DataSnapshot allergensSnapshot = dishSnapSoht.child("allergens");
                                    ArrayList<String> allergens = new ArrayList<>();

                                    if(allergensSnapshot.exists()){
                                        for(DataSnapshot allergen : allergensSnapshot.getChildren()){
                                            allergens.add(allergen.getValue().toString());
                                        }
                                    }

                                    String dishImage = dishSnapSoht.child("image").getValue(String.class);
                                    String dishType = dishSnapSoht.child("type").getValue(String.class);

                                    DishEntity dishEntity = new DishEntity(id, dishName, ingredients, allergens, dishImage, dishType);

                                    dishes.add(dishEntity);
                                }
                            }

                            // OBTENEMOS LOS MENÚS
                            ArrayList<MenuEntity> menus = new ArrayList<>();
                            DataSnapshot menusSnapshot = restaurantSnapshot.child("menus/items");
                            if(menusSnapshot.exists()){
                                for(DataSnapshot singleMenuSnapshot : menusSnapshot.getChildren()){
                                    int date = singleMenuSnapshot.child("date").getValue(Integer.class);
                                    int dessert = singleMenuSnapshot.child("dessert").getValue(Integer.class);
                                    int id = singleMenuSnapshot.child("id").getValue(Integer.class);
                                    float priceWithDessert = singleMenuSnapshot.child("priceWithDessert").getValue(Float.class);
                                    float priceNoDessert = singleMenuSnapshot.child("priceNoDessert").getValue(Float.class);
                                    ArrayList<Integer> firstCourses = new ArrayList<>();
                                    DataSnapshot firstCoursesSnapshot = singleMenuSnapshot.child("first_course");
                                    if(firstCoursesSnapshot.exists()){
                                        for(DataSnapshot first : firstCoursesSnapshot.getChildren()){
                                            firstCourses.add(Integer.parseInt(first.getValue().toString()));
                                        }
                                    }
                                    ArrayList<Integer> secondCourses = new ArrayList<>();
                                    DataSnapshot secondCoursesSnapshot = singleMenuSnapshot.child("second_course");
                                    if(secondCoursesSnapshot.exists()){
                                        for(DataSnapshot second : secondCoursesSnapshot.getChildren()){
                                            secondCourses.add(Integer.parseInt(second.getValue().toString()));
                                        }
                                    }
                                    MenuEntity menuEntity = new MenuEntity(id, date, firstCourses, secondCourses, dessert, priceWithDessert, priceNoDessert);
                                    menus.add(menuEntity);
                                }
                            }

                            // CON TODOS ESOS DATOS CREAMOS UN OBJETO RestaurantEntity

                            RestaurantEntity restaurantEntity = new RestaurantEntity(
                                    uid, email, name, description_mini, description, url, address, city, phone,
                                    schedule, image, food_type, dishes, average_rate, average_price, location, menus);

                            // LO AÑADIMOS AL ARRAYLIST
                            restaurantList.add(restaurantEntity);
                        }
                        // SUMAMOS 1 A contados
                        contados[0]++;

                        // CUANDO HAYAMOS RECORRIDO TODOS LOS RESTAURANTES
                        if (contados[0] == totalRestaurants) {
                            // MANDAMOS EL ARRAY CON TODOS AL SERVICE
                            callback.onSearchingSuccess(restaurantList);
                        }
                    }
                }
                else{
                    // SI NO HAY RESTAURANTES MANDAMOS UN ARRAY VACÍO
                    callback.onSearchingSuccess(new ArrayList<>());
                }
            }

            // ANTE CUALQUIER ERROR MANDAMOS UNA EXCEPCIÓN
            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onSearchingFailure(new Exception());
            }
        });
    }


    public interface OnRestaurantListCallback{
        void onSearchingSuccess(ArrayList<RestaurantEntity> restaurants);
        void onSearchingFailure(Exception exception);
    }


    /**
     * RECUPERA TODOS LOS DATOS DE UN RESTAURANTE APORTÁNDOLE ÚNICAMENTE SU URL
     * @param url URL DEL RESTAURANTE UQE SE BUSCA
     * @param callback SI SUCCESS -> DEVUELVE UN OBJETO RestaurantGetByUrlEntity
     *                 SI ERROR -> DEVUELVE UNA EXCEPCIÓN CON UN MENSAJE
     */
    public void getByUrl(String url, OnRestByUrlGot callback ){

        // ACCEDEMOS A LA REFERENCIA GLOBAL DE LA BASE DE DATOS (NODO SUPERIOR)
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // SI EXISTE
                if(dataSnapshot.exists()){

                    // GUARAMOS UNA "FOTO" DEL NODO DONDE ESTÁN LOS RESTAURANTES
                    DataSnapshot allRestaurantsSnapshot = dataSnapshot.child("restaurants");

                    // SI ESE NODOO EXISTE
                    if(allRestaurantsSnapshot.exists()){

                        // GUARDAMOS EL NÚMERO TOTAL DE RESTAURANTES Q HAY
                        int restaurantsQuantity = (int)allRestaurantsSnapshot.getChildrenCount();
                        // CREAMOS UN CONTADOR
                        int contador = 0;

                        // RECORREMOS TODOS LOS RESTAURANTS
                        for(DataSnapshot singleRestaurantSnapshot : allRestaurantsSnapshot.getChildren()){

                            // SNAPSHOT DEL RESTAURANTE ACTUAL
                            String urlSnapshot = singleRestaurantSnapshot.child("url").getValue(String.class);

                            // SI EXISTE
                            if(urlSnapshot.equals(url)){
                                // OBTENEMOS TODOS SUS DATOS
                                String uid = singleRestaurantSnapshot.getKey();
                                String name = singleRestaurantSnapshot.child("name").getValue(String.class);
                                String foodType = singleRestaurantSnapshot.child("food_type").getValue(String.class);
                                String address = singleRestaurantSnapshot.child("address").getValue(String.class);
                                String image = singleRestaurantSnapshot.child("image").getValue(String.class);
                                String phone = singleRestaurantSnapshot.child("phone").getValue(String.class);
                                String schedule = singleRestaurantSnapshot.child("schedule").getValue(String.class);
                                String description = singleRestaurantSnapshot.child("description").getValue(String.class);
                                String city = singleRestaurantSnapshot.child("city").getValue(String.class);

                                // OBTENEMOS LA NOTA MEDIA
                                int average_rate = 0;

                                // CREAMOS ARRAY DONDE METER LAS VALORACIONES SI LAS HAY
                                ArrayList<ReviewsEntity> reviewsEntityList = new ArrayList<>();
                                // SNAPSHOT DONDES ESTÁN LAS VALORACIONES SI LAS HUBIERA
                                DataSnapshot ordersSnapshot = singleRestaurantSnapshot.child("orders/items");

                                // SI HAY PEDIDOS
                                if(ordersSnapshot.exists()){
                                    // VARIABLES PARA GESTIONAR LAS VALORACOINES
                                    int sumOfRates = 0;
                                    int numberOfReviews = 0;

                                    // RECORREMOS TODOS LOS PEDIDOS QUR TIENE
                                    for(DataSnapshot order : ordersSnapshot.getChildren()){

                                        // SI TIENE VALORACIONES EL PEDIDO ENTRAMOS
                                        if(order.child("reviews").exists()){
                                            // GUARDAMOS TODOS LOS DATOS DE LA VALORACIÓN
                                            String customerUID = order.child("uidCustomer").getValue(String.class);
                                            String revName = dataSnapshot.child("users").child(customerUID).child("name").getValue(String.class);
                                            String revSurname = dataSnapshot.child("users").child(customerUID).child("surname").getValue(String.class);
                                            String revText = order.child("reviews/review").getValue(String.class);
                                            int orderRate = order.child("reviews/rate").getValue(Integer.class);

                                            // UNIMOS NOMBRE Y APELLIDO
                                            String fullName = revName + " " + revSurname;

                                            // CREAMOS LA VALORACIÓN
                                            ReviewsEntity revEntity = new ReviewsEntity(fullName, revText, orderRate);

                                            // LA AÑADIMOS AL ARRAYLIST
                                            reviewsEntityList.add(revEntity);

                                            numberOfReviews++;
                                            sumOfRates += orderRate;
                                        }

                                    }
                                    // REDONDEAMOS LA VALORACIÓN
                                    average_rate = Math.round((float)sumOfRates / numberOfReviews);
                                }

                                // SNAPSHOT DONDE ESTÁN LOS MENUS
                                DataSnapshot menusSnapshot = singleRestaurantSnapshot.child("menus/items");

                                // SI HAY MENUS ENTRAMOS
                                if(menusSnapshot.exists()){

                                    // CREAMOS UN ARRAY DONDE METER CADA MENU
                                    ArrayList<MenuGetByUrlEntity> menuEntityList = new ArrayList<>();

                                    // RECORREMOS CADA MENU
                                    for(DataSnapshot menu : menusSnapshot.getChildren()){
                                        // OBTENEMOS TOODS SUS DATOS
                                        int date = menu.child("date").getValue(Integer.class);
                                        int id = menu.child("id").getValue(Integer.class);
                                        float priceWithDessert = menu.child("priceWithDessert").getValue(Float.class);
                                        float priceNoDessert = menu.child("priceNoDessert").getValue(Float.class);

                                        // OBTENEMOS EL POSTRE
                                        int dessertID = menu.child("dessert").getValue(Integer.class);
                                        String dessertName = singleRestaurantSnapshot.child("dishes/items")
                                                .child(String.valueOf(dessertID)).child("name").getValue(String.class);
                                        String dessertIngs = singleRestaurantSnapshot.child("dishes/items")
                                                .child(String.valueOf(dessertID)).child("ingredients").getValue(String.class);
                                        String dessertImg = singleRestaurantSnapshot.child("dishes/items")
                                                .child(String.valueOf(dessertID)).child("image").getValue(String.class);

                                        ArrayList<String> dessertAllergens = new ArrayList<>();
                                        DataSnapshot dAllergens = singleRestaurantSnapshot.child("dishes/items")
                                                .child(String.valueOf(dessertID)).child("allergens");
                                        for(DataSnapshot allergen: dAllergens.getChildren()){
                                            dessertAllergens.add(allergen.getValue(String.class));
                                        }
                                        // CREAMOS EL POSTRE OBTENIDO
                                        DishGetByEntity dessertEntity = new DishGetByEntity(dessertID, dessertName, dessertIngs, dessertAllergens, dessertImg);


                                        // CREAMOS UN ARRAY PARA METER LOS PRIMEROS
                                        ArrayList<DishGetByEntity> firstCourses = new ArrayList<>();

                                        // LUGAR DONDE ESTÁN LOS PRIMEROS
                                        DataSnapshot firstCoursesSnapshot = menu.child("first_course");

                                        // SI EXISTEN ENTRAMOS
                                        if(firstCoursesSnapshot.exists()){
                                            // RECORREMOS CADA PRIMERO
                                            for(DataSnapshot first : firstCoursesSnapshot.getChildren()){
                                                // GUARDAMOS TODAS SUS PROPIEDADES
                                                int firstID = first.getValue(Integer.class);

                                                String firstName = singleRestaurantSnapshot.child("dishes/items")
                                                        .child(String.valueOf(firstID)).child("name").getValue(String.class);
                                                String firstIngs = singleRestaurantSnapshot.child("dishes/items")
                                                        .child(String.valueOf(firstID)).child("ingredients").getValue(String.class);
                                                String firstImg = singleRestaurantSnapshot.child("dishes/items")
                                                        .child(String.valueOf(firstID)).child("image").getValue(String.class);

                                                ArrayList<String> firstAllergens = new ArrayList<>();
                                                DataSnapshot fAllergens = singleRestaurantSnapshot.child("dishes/items")
                                                        .child(String.valueOf(firstID)).child("allergens");
                                                for(DataSnapshot allergen: fAllergens.getChildren()){
                                                    firstAllergens.add(allergen.getValue(String.class));
                                                }
                                                // GUARDAMOS EN EL ARRAY EL PLATO OBTENIDO
                                                firstCourses.add(new DishGetByEntity(firstID, firstName, firstIngs,firstAllergens, firstImg));
                                            }
                                        }
                                        // CREAMOS ARRAY PARA GUARDAR LOS SEGUNDOS
                                        ArrayList<DishGetByEntity> secondCourses = new ArrayList<>();
                                        // SNAP DONDES ESTÁN LOS SEGUNDOS EN REALTIME
                                        DataSnapshot secondCoursesSnapshot = menu.child("second_course");

                                        // SI EXISTEN ENTRAMOS
                                        if(secondCoursesSnapshot.exists()){
                                            // RECORREMOS CADA PLATO
                                            for(DataSnapshot second : secondCoursesSnapshot.getChildren()){
                                                // OBTENEMOS TODAS SUS PROPIEDADES
                                                int secondID = second.getValue(Integer.class);

                                                String secondIName = singleRestaurantSnapshot.child("dishes/items")
                                                        .child(String.valueOf(secondID)).child("name").getValue(String.class);
                                                String secondIngs = singleRestaurantSnapshot.child("dishes/items")
                                                        .child(String.valueOf(secondID)).child("ingredients").getValue(String.class);
                                                String secondImg = singleRestaurantSnapshot.child("dishes/items")
                                                        .child(String.valueOf(secondID)).child("image").getValue(String.class);

                                                ArrayList<String> secondIAllergens = new ArrayList<>();

                                                DataSnapshot secAllergens = singleRestaurantSnapshot.child("dishes/items")
                                                        .child(String.valueOf(secondID)).child("allergens");
                                                for(DataSnapshot allergen: secAllergens.getChildren()){
                                                    secondIAllergens.add(allergen.getValue(String.class));

                                                }
                                                // AÑADIMOS AL ARRAY EL PLATO SEGUNDO OBTENIDOS
                                                secondCourses.add(new DishGetByEntity(secondID, secondIName, secondIngs,secondIAllergens, secondImg));
                                            }
                                        }
                                        // CREAMOS EL MENÚ CON LOS PLATOS OBTENIDOS Y TODAS SUS PROPIEDADES
                                        MenuGetByUrlEntity menuEntity = new MenuGetByUrlEntity(id, date, firstCourses, secondCourses, dessertEntity, priceWithDessert, priceNoDessert);
                                        // AÑADIMOS EL MENU AL ARRAY
                                        menuEntityList.add(menuEntity);
                                    }

                                    // CREAMOS EL OBJETO QUE MANDAREMOS AL SERVICE

                                    RestaurantGetByUrlEntity entity = new RestaurantGetByUrlEntity(uid, name, foodType,
                                            address, image, phone, schedule, average_rate, description, city, reviewsEntityList, menuEntityList);

                                    // LO MANDAMOS
                                    callback.onSearchingSuccess(entity);
                                }
                            }
                            else{
                                // SUMA 1 SI NO COINCIDE EL RESTAURANTE ACTUAL CON LA URL APORTADA
                                contador++;
                            }
                            if(contador == restaurantsQuantity) {
                                // SI LLEGA AQUÍ ES PQ HA RECORRIDO TODOS LOS RESTAURANTES Y NO HAY NINGUNO CON LA URL APORTADA
                                callback.onSearchingFailure(new Exception("No existe un restaurante con esa URL"));
                            }
                        }
                    }
                }
                else{
                    // NO HAY RESTAURANTES
                    callback.onSearchingFailure(new Exception("No hay restaurantes registrados"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // HAY UN ERROR DE CONEXIÓN
                callback.onSearchingFailure(new Exception("Error de conexión a la base de datos"));
            }
        });

    }


    public interface OnRestByUrlGot{
        void onSearchingSuccess(RestaurantGetByUrlEntity restaurantGetByUrlEntity);
        void onSearchingFailure(Exception exception);
    }


    /**
     * OBTIENE DE LA BBDD LOS MENUS QUE UN RESTUARANTE TIENE ALMACENADOS
     * @param uid NÚMERO DE USUARIO DEL RESTAURANTE
     * @param callback SI ÉXITO DEVUELVE UN ARRAY CON LOS MENÚS EN FORMATO MenuGetAllMenusEntity
     *                 SI HAY UN FALLO ENVÍA UNA EXCEPCIÓN PARA QUE LA VEA EL SERVICIO
     */
    public void getMenus(String uid, OnMenusGot callback ) {

        // REFERENCIA EN REALTIME DEL RESTAURANTE CUYA UID SE APORTA
        DatabaseReference restRef = databaseReference.child("restaurants").child(uid);

        // ACCEDEMOS A ESE RESTAURANTE
        restRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // SI EXISTE ENTRAMOS
                if(dataSnapshot.exists()){

                    // SNAP DONDE ESTÁN LOS MENÚS SI LOS HUBIERA
                    DataSnapshot menusSnapshot = dataSnapshot.child("menus/items");

                    // SI TIENE CONTENIDO LOS MENÚS ENTRA
                    if(menusSnapshot.exists()){

                        // CREAMOS ARRAY DONDE GUARDAR LOS MENUS
                        ArrayList<MenuGetAllMenusEntity> menuEntityList = new ArrayList<>();

                        // RECORREMOS UNO A UNO
                        for(DataSnapshot menu : menusSnapshot.getChildren()){
                            // OBTENEMOS LAS PROOPIEDADES DE CADA UNO
                            int id = menu.child("id").getValue(Integer.class);
                            int date = menu.child("date").getValue(Integer.class);
                            float priceWithDessert = menu.child("priceWithDessert").getValue(Float.class);
                            float priceNoDessert = menu.child("priceNoDessert").getValue(Float.class);

                            // OBTENEMOS EL POSTRE
                            int dessertID = menu.child("dessert").getValue(Integer.class);
                            String dessertName = dataSnapshot.child("dishes/items")
                                    .child(String.valueOf(dessertID)).child("name").getValue(String.class);
                            String dessertIngs = dataSnapshot.child("dishes/items")
                                    .child(String.valueOf(dessertID)).child("ingredients").getValue(String.class);
                            String dessertImg = dataSnapshot.child("dishes/items")
                                    .child(String.valueOf(dessertID)).child("image").getValue(String.class);

                            ArrayList<String> dessertAllergens = new ArrayList<>();
                            DataSnapshot dAllergens = dataSnapshot.child("dishes/items")
                                    .child(String.valueOf(dessertID)).child("allergens");
                            for(DataSnapshot allergen: dAllergens.getChildren()){
                                dessertAllergens.add(allergen.getValue(String.class));
                            }
                            // CREAMOS EL POSTRE
                            DishGetByEntity dessertEntity = new DishGetByEntity(dessertID, dessertName, dessertIngs, dessertAllergens, dessertImg);

                            // ARRAY PARA GUARDAR LOS PRIMEROS
                            ArrayList<DishGetByEntity> firstCourses = new ArrayList<>();
                            // LUGAR DONDE ESTÁN LOS PRIMEROS
                            DataSnapshot firstCoursesSnapshot = menu.child("first_course");

                            // SI EXISTEN ENTRA
                            if(firstCoursesSnapshot.exists()){
                                // RECORREMOS PLATO A PLATO
                                for(DataSnapshot first : firstCoursesSnapshot.getChildren()){
                                    // OBTENEMOS TODAS SUS PROPIEDADES
                                    int firstID = first.getValue(Integer.class);

                                    String firstName = dataSnapshot.child("dishes/items")
                                            .child(String.valueOf(firstID)).child("name").getValue(String.class);
                                    String firstIngs = dataSnapshot.child("dishes/items")
                                            .child(String.valueOf(firstID)).child("ingredients").getValue(String.class);
                                    String firstImg = dataSnapshot.child("dishes/items")
                                            .child(String.valueOf(firstID)).child("image").getValue(String.class);

                                    ArrayList<String> firstAllergens = new ArrayList<>();
                                    DataSnapshot fAllergens = dataSnapshot.child("dishes/items")
                                            .child(String.valueOf(firstID)).child("allergens");
                                    for(DataSnapshot allergen: fAllergens.getChildren()){
                                        firstAllergens.add(allergen.getValue(String.class));
                                    }
                                    // CREAMOS EL PLATO PRIMERO Y LO AÑADIMOS AL ARRAYLIST
                                    firstCourses.add(new DishGetByEntity(firstID, firstName, firstIngs,firstAllergens, firstImg));
                                }
                            }
                            // ARRAY PARA GUARDAR LOS SEGUNDOS
                            ArrayList<DishGetByEntity> secondCourses = new ArrayList<>();
                            // SNAP DONDE ESTÁN LSO SEGUNDOS
                            DataSnapshot secondCoursesSnapshot = menu.child("second_course");
                            // SI EXISTEN
                            if(secondCoursesSnapshot.exists()){
                                // RECORREMOS PLATO A PLATO
                                for(DataSnapshot second : secondCoursesSnapshot.getChildren()){
                                    // OBTENEMOS TODAS SUS PROPIEDADES
                                    int secondID = second.getValue(Integer.class);

                                    String secondIName = dataSnapshot.child("dishes/items")
                                            .child(String.valueOf(secondID)).child("name").getValue(String.class);
                                    String secondIngs = dataSnapshot.child("dishes/items")
                                            .child(String.valueOf(secondID)).child("ingredients").getValue(String.class);
                                    String secondImg = dataSnapshot.child("dishes/items")
                                            .child(String.valueOf(secondID)).child("image").getValue(String.class);

                                    ArrayList<String> secondIAllergens = new ArrayList<>();

                                    DataSnapshot secAllergens = dataSnapshot.child("dishes/items")
                                            .child(String.valueOf(secondID)).child("allergens");
                                    for(DataSnapshot allergen: secAllergens.getChildren()){
                                        secondIAllergens.add(allergen.getValue(String.class));
                                    }
                                    // AÑADIMOS EL SEGUNDO AL ARRAYLIST DE LOS SEGUNDOS
                                    secondCourses.add(new DishGetByEntity(secondID, secondIName, secondIngs,secondIAllergens, secondImg));
                                }
                            }
                            // GUARAMOS EL MENÚ EN UNA VARIABLE
                            MenuGetAllMenusEntity menuEntity = new MenuGetAllMenusEntity(id, date, firstCourses, secondCourses, dessertEntity, priceWithDessert, priceNoDessert);
                            // AÑADIMOS EL MENÚA AL ARRAY
                            menuEntityList.add(menuEntity);
                        }

                        // CREAMOS EL RESTAURANTE QUE VAMOS A MANDAR CON EL OBJETO CREADO
                        RestaurantGetAllMenusEntity entity = new RestaurantGetAllMenusEntity(menuEntityList);
                        // Y  LO MANDAMOS AL SERVICE
                        callback.onSearchingSuccess(entity);
                    }
                }
                else{
                    // SI NO EXISTE MANDAMOS UNA EXCEPCIOÓN
                    callback.onSearchingFailure(new Exception("No existe el restaurante"));
                }
            }

            // SI HAY UN ERROR MANDAMOS UNA EXCEPCIÓN
            @Override
            public void onCancelled(DatabaseError databaseError) {
                    callback.onSearchingFailure(new Exception("Error de conexión a la base de datos"));
            }
        });

    }


    public interface OnMenusGot{
        void onSearchingSuccess(RestaurantGetAllMenusEntity restaurantGetAllMenusEntity);
        void onSearchingFailure(Exception exception);
    }


}

