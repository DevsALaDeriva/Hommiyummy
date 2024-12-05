package com.example.homiyummy.repository;

import com.example.homiyummy.model.dish.DishEntity;
import com.example.homiyummy.model.dish.DishGetByEntity;
import com.example.homiyummy.model.dish.DishInOrderEntity;
import com.example.homiyummy.model.menu.MenuEntity;
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
        restaurantEntityToSave.put("location", restaurantEntity.getLocation());

        DatabaseReference restaurantRef = firebaseDatabase.getReference("restaurants").child(restaurantEntity.getUid());

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

        restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    Map<String, Object> updates = new HashMap<>();
                    // USAMOS UN MAPA PQ QUEREMOS CONSERVAR PROPIEDADES QUE NO SE INCLUYEN EN EL OBJETO QUE LLEGA
                    // ESTO CONSERVA EL VALOR DE LOS NODOS COMPLEJOS. 1º COGEMOS SU VALOR 2º LO GUARDAMOS EN EL MAPA
                    if (dataSnapshot.child("dishes").exists()) {
                        updates.put("dishes", dataSnapshot.child("dishes").getValue());
                    }
                    if (dataSnapshot.child("menus").exists()) {
                        updates.put("menus", dataSnapshot.child("menus").getValue());
                    }
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

                    // Solo actualizamos las propiedades específicas
                    restaurantRef.updateChildren(updates, (databaseError, databaseReference) -> {
                        if (databaseError == null) {
                            callback.onSuccess(true);
                        } else {
                            callback.onFailure(databaseError.toException());
                        }
                    });
                } else {
                    callback.onFailure(new Exception("Restaurante no encontrado."));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
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
                        RestaurantReadResponse emptyResponse = new RestaurantReadResponse();
                        callback.onFailure(emptyResponse);
                    }
                }
                else{
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

    // ----------------------------------------------------------------------------------------------------------------

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
                    for(DataSnapshot restaurantSnapshot: dataSnapshot.getChildren()){                                   // RECORRO EL SNAPSHOT DEL NODO "RESTAURANTS"
                        String uid = restaurantSnapshot.getKey();                                                       // OBTENEMOS EL UID DEL RESTAURANTE ( QUE ES SU KEY)
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
                                    Integer rate = restaurantSnapshot.child("rate").getValue(Integer.class);
                                    Float average_price = restaurantSnapshot.child("average_price").getValue(Float.class);
                                    RestaurantLocation location = restaurantSnapshot.child("location").getValue(RestaurantLocation.class);

                                    rate = rate != null ? rate : 0;
                                    average_price = average_price != null ? average_price : 0;


                                    // PREPARANDO LAS REVIEWS-----------------------
                                    //----------------------------------------------
//                                    ArrayList<ReviewsEntity> reviewsList = new ArrayList<>();
//                                    DataSnapshot reviewsSnapshot = restaurantSnapshot.child("reviews");
//
//                                    if(reviewsSnapshot.exists()){
//                                        for(DataSnapshot singleReviewSnapshot : reviewsSnapshot.getChildren()){
//                                            String reviewName = singleReviewSnapshot.child("name").getValue(String.class);
//                                            String reviewText = singleReviewSnapshot.child("review").getValue(String.class);
//                                            Integer reviewRate = singleReviewSnapshot.child("rate").getValue(Integer.class);
//
//                                            ReviewsEntity reviewsEntity = new ReviewsEntity(reviewName, reviewText, reviewRate);
//
//                                            reviewsList.add(reviewsEntity);
//                                        }
//                                    }
                                    //----------------------------------------------


                                    // AHORA LOS PLATOS
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

                                            dishes.add(dishEntity);
                                        }
                                    }

                                    // AHORA LOS MENÚS
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

                                    RestaurantEntity restaurantEntity = new RestaurantEntity(
                                            uid, email, name, description_mini, description, url, address, city, phone,
                                            schedule, image, food_type, dishes, rate, average_price, location, menus);

                                      restaurantList.add(restaurantEntity);                                             // AÑADIMOS EL RESTAURANTE AL ARRAY
                                }

                                contados[0]++;

                                if (contados[0] == totalRestaurants) {                                                // SOLO SEGUIMOS CUANDO HEMOS AÑADIDO TODOS


                                    callback.onSearchingSuccess(restaurantList);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // TODO --------> FALTA
                            }
                        });
                    }
                }
                else{
                    callback.onSearchingSuccess(new ArrayList<>());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
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

//    public void getByUrl(String url, OnRestByUrlGot callback ){
//
//        DatabaseReference restaurantsRef = databaseReference.child("restaurants");
//
//        restaurantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists()){
//
//                    for(DataSnapshot singleRestaurantSnapshot : dataSnapshot.getChildren()){
//
//                        String urlSnapshot = singleRestaurantSnapshot.child("url").getValue(String.class);
//
//                        if(urlSnapshot.equals(url)){
//                            // 1º OBTENEMOS CAMPOS EN FORMATO STRING E INT DEL RESTAURANTE
//                            String uid = singleRestaurantSnapshot.getKey();
//                            String name = singleRestaurantSnapshot.child("name").getValue(String.class);
//                            String foodType = singleRestaurantSnapshot.child("food_type").getValue(String.class);
//                            String address = singleRestaurantSnapshot.child("address").getValue(String.class);
//                            String image = singleRestaurantSnapshot.child("image").getValue(String.class);
//                            String phone = singleRestaurantSnapshot.child("phone").getValue(String.class);
//                            String schedule = singleRestaurantSnapshot.child("schedule").getValue(String.class);
//                            Integer rate = singleRestaurantSnapshot.child("rate").getValue(Integer.class);
//                            rate = rate != null ? rate : 0;
//                            String description = singleRestaurantSnapshot.child("description").getValue(String.class);
//                            String city = singleRestaurantSnapshot.child("city").getValue(String.class);
//
//                            // 2º OBTENEMOS EL CAMPO REVIEWS QUE ES DEL TIPO ARRAYLIST
//                            ArrayList<ReviewsEntity> reviewsEntityList = new ArrayList<>();
//                            DataSnapshot reviewsSnapshot = singleRestaurantSnapshot.child("reviews");
//                            if(reviewsSnapshot.exists()){
//                                for(DataSnapshot rev : reviewsSnapshot.getChildren()){
//                                    String revName = rev.child("name").getValue(String.class);
//                                    String revText = rev.child("review").getValue(String.class);
//                                    int revRate = rev.child("rate").getValue(Integer.class);
//
//                                    ReviewsEntity revEntity = new ReviewsEntity(revName, revText, revRate);
//
//                                    reviewsEntityList.add(revEntity);
//                                }
//                            }
//
//                            // 3º OBTENEMOS EL CAMPO MENUS QUE ES DEL TIPO ARRAYLIST
//                            ArrayList<MenuEntity> menuEntityList = new ArrayList<>();
//                            DataSnapshot menusSnapshot = singleRestaurantSnapshot.child("menus/items");
//                            if(menusSnapshot.exists()){
//                                for(DataSnapshot menu : menusSnapshot.getChildren()){
//                                    int date = menu.child("date").getValue(Integer.class);
//
//
//
//                                    int dessert = menu.child("dessert").getValue(Integer.class);
//
//
//
//                                    int id = menu.child("id").getValue(Integer.class);
//                                    float priceWithDessert = menu.child("priceWithDessert").getValue(Float.class);
//                                    float priceNoDessert = menu.child("priceNoDessert").getValue(Float.class);
//
//
//
//
//
//                                    ArrayList<Integer> firstCourses = new ArrayList<>();
//                                    DataSnapshot firstCoursesSnapshot = menu.child("first_course");
//                                    if(firstCoursesSnapshot.exists()){
//                                        for(DataSnapshot first : firstCoursesSnapshot.getChildren()){
//                                            firstCourses.add(Integer.parseInt(first.getValue().toString()));
//                                        }
//                                    }
//                                    ArrayList<Integer> secondCourses = new ArrayList<>();
//                                    DataSnapshot secondCoursesSnapshot = menu.child("second_course");
//                                    if(secondCoursesSnapshot.exists()){
//                                        for(DataSnapshot second : secondCoursesSnapshot.getChildren()){
//                                            secondCourses.add(Integer.parseInt(second.getValue().toString()));
//                                        }
//                                    }
//                                    MenuEntity menuEntity = new MenuEntity(id, date, firstCourses, secondCourses, dessert, priceWithDessert, priceNoDessert);
//                                    menuEntityList.add(menuEntity);
//                                }
//                            }
//
//                            RestaurantGetByUrlEntity entity = new RestaurantGetByUrlEntity(uid, name, foodType,
//                                    address, image, phone, schedule, rate, description, city, reviewsEntityList, menuEntityList);
//
//                            callback.onSearchingSuccess(entity);
//                        }
//                    }
//                }
//                else{
//                    // TOD O ----- SI EL RESTAURANTE NO EXISTE
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                // TOD O ------- SI HAY UN ERROR
//            }
//        });
//
//    }



    public void getByUrl(String url, OnRestByUrlGot callback ){

        DatabaseReference restaurantsRef = databaseReference.child("restaurants");

        restaurantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    int restaurantsQuantity = (int)dataSnapshot.getChildrenCount();
                    int contador = 0;

                    for(DataSnapshot singleRestaurantSnapshot : dataSnapshot.getChildren()){

                        String urlSnapshot = singleRestaurantSnapshot.child("url").getValue(String.class);

                        if(urlSnapshot.equals(url)){

                            // 1º OBTENEMOS CAMPOS EN FORMATO STRING E INT DEL RESTAURANTE
                            String uid = singleRestaurantSnapshot.getKey();
                            String name = singleRestaurantSnapshot.child("name").getValue(String.class);
                            String foodType = singleRestaurantSnapshot.child("food_type").getValue(String.class);
                            String address = singleRestaurantSnapshot.child("address").getValue(String.class);
                            String image = singleRestaurantSnapshot.child("image").getValue(String.class);
                            String phone = singleRestaurantSnapshot.child("phone").getValue(String.class);
                            String schedule = singleRestaurantSnapshot.child("schedule").getValue(String.class);
                            Integer rate = singleRestaurantSnapshot.child("rate").getValue(Integer.class);
                            rate = rate != null ? rate : 0;
                            String description = singleRestaurantSnapshot.child("description").getValue(String.class);
                            String city = singleRestaurantSnapshot.child("city").getValue(String.class);

                            // 2º OBTENEMOS EL CAMPO REVIEWS QUE ES DEL TIPO ARRAYLIST
                            ArrayList<ReviewsEntity> reviewsEntityList = new ArrayList<>();
                            DataSnapshot reviewsSnapshot = singleRestaurantSnapshot.child("reviews");

                            if(reviewsSnapshot.exists()){
                                for(DataSnapshot rev : reviewsSnapshot.getChildren()){
                                    String revName = rev.child("name").getValue(String.class);
                                    String revText = rev.child("review").getValue(String.class);
                                    int revRate = rev.child("rate").getValue(Integer.class);

                                    ReviewsEntity revEntity = new ReviewsEntity(revName, revText, revRate);

                                    reviewsEntityList.add(revEntity);
                                }
                            }

                            DataSnapshot menusSnapshot = singleRestaurantSnapshot.child("menus/items");

                            if(menusSnapshot.exists()){
                                // 3º OBTENEMOS EL CAMPO MENUS QUE ES DEL TIPO ARRAYLIST
                                ArrayList<MenuGetByUrlEntity> menuEntityList = new ArrayList<>();

                                for(DataSnapshot menu : menusSnapshot.getChildren()){
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

                                    String dessertAllergens = "";
                                    DataSnapshot dAllergens = singleRestaurantSnapshot.child("dishes/items")
                                            .child(String.valueOf(dessertID)).child("allergens");
                                    for(DataSnapshot allergen: dAllergens.getChildren()){
                                        dessertAllergens += allergen.getValue(String.class) + ", ";
                                    }
                                    if(dessertAllergens.length() > 4){
                                        dessertAllergens = dessertAllergens.substring(0,dessertAllergens.length()-2);
                                    }

                                    DishInOrderEntity dessertEntity = new DishInOrderEntity(dessertName, dessertIngs, dessertAllergens, dessertImg);


                                    // OBTENEMOS LOS PRIMEROS
                                    ArrayList<DishGetByEntity> firstCourses = new ArrayList<>();
                                    DataSnapshot firstCoursesSnapshot = menu.child("first_course");

                                    if(firstCoursesSnapshot.exists()){
                                        for(DataSnapshot first : firstCoursesSnapshot.getChildren()){

                                            int firstID = first.getValue(Integer.class);

                                            String firstName = singleRestaurantSnapshot.child("dishes/items")
                                                    .child(String.valueOf(firstID)).child("name").getValue(String.class);
                                            String firstIngs = singleRestaurantSnapshot.child("dishes/items")
                                                    .child(String.valueOf(firstID)).child("ingredients").getValue(String.class);
                                            String firstImg = singleRestaurantSnapshot.child("dishes/items")
                                                    .child(String.valueOf(firstID)).child("image").getValue(String.class);

                                            String firstAllergens = "";
                                            DataSnapshot fAllergens = singleRestaurantSnapshot.child("dishes/items")
                                                    .child(String.valueOf(firstID)).child("allergens");
                                            for(DataSnapshot allergen: fAllergens.getChildren()){
                                                firstAllergens += allergen.getValue(String.class) + ", ";
                                            }
                                            if(firstAllergens.length() > 4) {
                                                firstAllergens = firstAllergens.substring(0, firstAllergens.length() - 2);
                                            }
                                            firstCourses.add(new DishGetByEntity(firstID, firstName, firstIngs,firstAllergens, firstImg));
                                        }
                                    }

                                    // OBTENEMOS LOS SEGUNDOS
                                    ArrayList<DishGetByEntity> secondCourses = new ArrayList<>();
                                    DataSnapshot secondCoursesSnapshot = menu.child("second_course");
                                    if(secondCoursesSnapshot.exists()){

                                        for(DataSnapshot second : secondCoursesSnapshot.getChildren()){

                                            int secondID = second.getValue(Integer.class);

                                            String secondIName = singleRestaurantSnapshot.child("dishes/items")
                                                    .child(String.valueOf(secondID)).child("name").getValue(String.class);
                                            String secondIngs = singleRestaurantSnapshot.child("dishes/items")
                                                    .child(String.valueOf(secondID)).child("ingredients").getValue(String.class);
                                            String secondImg = singleRestaurantSnapshot.child("dishes/items")
                                                    .child(String.valueOf(secondID)).child("image").getValue(String.class);

                                            String secondIAllergens = "";

                                            DataSnapshot secAllergens = singleRestaurantSnapshot.child("dishes/items")
                                                    .child(String.valueOf(secondID)).child("allergens");
                                            for(DataSnapshot allergen: secAllergens.getChildren()){
                                                secondIAllergens += allergen.getValue(String.class) + ", ";

                                            }
                                            if(secondIAllergens.length() > 4){
                                                secondIAllergens = secondIAllergens.substring(0,secondIAllergens.length()-2);
                                            }

                                            secondCourses.add(new DishGetByEntity(secondID, secondIName, secondIngs,secondIAllergens, secondImg));

                                        }
                                    }

                                    MenuGetByUrlEntity menuEntity = new MenuGetByUrlEntity(id, date, firstCourses, secondCourses, dessertEntity, priceWithDessert, priceNoDessert);
                                    menuEntityList.add(menuEntity);
                                }

                                RestaurantGetByUrlEntity entity = new RestaurantGetByUrlEntity(uid, name, foodType,
                                        address, image, phone, schedule, rate, description, city, reviewsEntityList, menuEntityList);
                                callback.onSearchingSuccess(entity);
                            }

                        }
                        else{
                            contador++;
                        }
                        if(contador == restaurantsQuantity) {
                            callback.onSearchingFailure(new Exception("No existe un restaurante con esa URL"));
                        }

                    }
                }
                else{
                    callback.onSearchingFailure(new Exception("No hay restaurantes registrados"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onSearchingFailure(new Exception("Error de conexión a la base de datos"));
            }
        });

    }

    // ----------------------------------------------------------------------------------------------------------------

    public interface OnRestByUrlGot{
        void onSearchingSuccess(RestaurantGetByUrlEntity restaurantGetByUrlEntity);
        void onSearchingFailure(Exception exception);
    }

    // ----------------------------------------------------------------------------------------------------------------

}

