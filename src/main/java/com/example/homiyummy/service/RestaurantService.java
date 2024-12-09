package com.example.homiyummy.service;

import com.example.homiyummy.model.menu.*;
import com.example.homiyummy.model.restaurant.*;
import com.example.homiyummy.model.reviews.ReviewsEntity;
import com.example.homiyummy.model.reviews.ReviewsResponse;
import com.example.homiyummy.repository.RestaurantRepository;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final FirebaseDatabase firebaseDatabase;

    public RestaurantService(RestaurantRepository restaurantRepository, FirebaseDatabase firebaseDatabase) {
        this.restaurantRepository = restaurantRepository;
        this.firebaseDatabase = firebaseDatabase;
    }


    // ----------------------------------------------------------------------------------------------------------------

    public RestaurantResponse createRestaurant(RestaurantDTO restaurantDTO) {

        RestaurantEntity restaurantEntity = new RestaurantEntity();

        restaurantEntity.setUid(restaurantDTO.getUid());
        restaurantEntity.setEmail(restaurantDTO.getEmail());
        restaurantEntity.setName(restaurantDTO.getName());
        restaurantEntity.setDescription_mini(restaurantDTO.getDescription_mini());
        restaurantEntity.setDescription(restaurantDTO.getDescription());
        restaurantEntity.setUrl(restaurantDTO.getUrl());
        restaurantEntity.setAddress(restaurantDTO.getAddress());
        restaurantEntity.setCity(restaurantDTO.getCity());
        restaurantEntity.setPhone(restaurantDTO.getPhone());
        restaurantEntity.setSchedule(restaurantDTO.getSchedule());
        restaurantEntity.setImage(restaurantDTO.getImage());
        restaurantEntity.setFood_type(restaurantDTO.getFood_type());
        //System.out.println(restaurantDTO.getLocation().getLat());
        //System.out.println(restaurantDTO.getLocation().getLng());
        restaurantEntity.setLocation(restaurantDTO.getLocation());

        CompletableFuture<RestaurantResponse> future = new CompletableFuture<>();

        restaurantRepository.saveRestaurant(restaurantEntity, new RestaurantRepository.getSaveRestaurantCallback() {
            @Override
            public void onRestaurantGot(RestaurantResponse restaurantResponse) {
                future.complete(restaurantResponse);
            }

            @Override
            public void onFailure(Exception exception) {
                future.completeExceptionally(exception);
            }
        });

        try {
            return future.get(); // DEVUELVE UN RestaurantResponse DESPUÉS DE HABERLO SACADO DEL future. Y ESPERA AHÍ HASTA QUE EL GET LO OBTIENE
        }  catch (Exception e) {
            throw new RuntimeException("Error al guardar el restaurante en Firebase", e);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------

    public Boolean updateRestaurant(RestaurantDTO restaurantDTO) {

        CompletableFuture<Boolean> futureResponse = new CompletableFuture<>();

        RestaurantEntity restaurantEntity = new RestaurantEntity();

        restaurantEntity.setUid(restaurantDTO.getUid());
        restaurantEntity.setName(restaurantDTO.getName());
        restaurantEntity.setDescription_mini(restaurantDTO.getDescription_mini());
        restaurantEntity.setDescription(restaurantDTO.getDescription());
        restaurantEntity.setUrl(restaurantDTO.getUrl());
        restaurantEntity.setAddress(restaurantDTO.getAddress());
        restaurantEntity.setCity(restaurantDTO.getCity());
        restaurantEntity.setPhone(restaurantDTO.getPhone());
        restaurantEntity.setSchedule(restaurantDTO.getSchedule());
        restaurantEntity.setImage(restaurantDTO.getImage());
        restaurantEntity.setFood_type(restaurantDTO.getFood_type());
        restaurantEntity.setLocation(restaurantDTO.getLocation());

        restaurantRepository.updateRestaurantData(restaurantEntity, new RestaurantRepository.GetUpdateRestaurantCallback() {
            @Override
            public void onSuccess(Boolean confirmation) {
                futureResponse.complete(confirmation);
            }

            @Override
            public void onFailure(Exception exception) {
                futureResponse.completeExceptionally(exception);
            }
        });

        try {
            return futureResponse.get();
        }
        catch (Exception e){
            throw new RuntimeException("Error al obtener confirmación de la actualización del restaurante", e);
        }

    }

    // ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<Boolean> existsByUid(String uid) {

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        restaurantRepository.exists(uid, new RestaurantRepository.ExistsRestaurantCallback() {
            @Override
            public void onSuccess(Boolean response) {
                future.complete(response);
            }

            @Override
            public void onFailure(Exception exception) {
                future.completeExceptionally(exception);
            }
        });
        return future;
    }

    // ----------------------------------------------------------------------------------------------------------------

    public RestaurantReadResponse findByUid(String uid) {

        CompletableFuture<RestaurantReadResponse> future = new CompletableFuture<>();

        restaurantRepository.findByUid(uid, new RestaurantRepository.FindRestaurantCallback() {
            @Override
            public void onSuccess(RestaurantReadResponse response) {
                future.complete(response);
            }

            @Override
            public void onFailure(RestaurantReadResponse response) {
                future.complete(response);
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<Map<String, ArrayList<RestaurantGetAllFormatResponse>>> getAll(){

        CompletableFuture<Map<String, ArrayList<RestaurantGetAllFormatResponse>>> futureAllRestaurants = new CompletableFuture<>();

        restaurantRepository.getAllRestaurantList(new RestaurantRepository.OnRestaurantListCallback() {
            @Override
            public void onSearchingSuccess(ArrayList<RestaurantEntity> restaurants) {

                ArrayList<RestaurantGetAllFormatResponse> restListResponse = new ArrayList<>();

                for(RestaurantEntity restaurant : restaurants){

                    RestaurantGetAllFormatResponse restaurantGetAllFormatResponse = new RestaurantGetAllFormatResponse();

                    restaurantGetAllFormatResponse.setName(restaurant.getName());
                    restaurantGetAllFormatResponse.setDescription_mini(restaurant.getDescription_mini());
                    restaurantGetAllFormatResponse.setUrl(restaurant.getUrl());
                    restaurantGetAllFormatResponse.setAddress(restaurant.getAddress());
                    restaurantGetAllFormatResponse.setPhone(restaurant.getPhone());
                    restaurantGetAllFormatResponse.setSchedule(restaurant.getSchedule());
                    restaurantGetAllFormatResponse.setFood_type(restaurant.getFood_type());
                    restaurantGetAllFormatResponse.setImage(restaurant.getImage());
                    restaurantGetAllFormatResponse.setRate(restaurant.getAverage_rate());
                    restaurantGetAllFormatResponse.setAverage_price(restaurant.getAverage_price());
                    restaurantGetAllFormatResponse.setLocation(restaurant.getLocation());

                    restListResponse.add(restaurantGetAllFormatResponse);
                }

                Map<String, ArrayList<RestaurantGetAllFormatResponse>> dataResponse = new HashMap<>();
                dataResponse.put("restaurants", restListResponse);

                futureAllRestaurants.complete(dataResponse);
            }

            @Override
            public void onSearchingFailure(Exception exception) {
                futureAllRestaurants.completeExceptionally(exception);
            }
        });
        return futureAllRestaurants;
    }

    // ----------------------------------------------------------------------------------------------------------------

//    public CompletableFuture<FeaturedRestaurantResponse> getTheOneFeaturedRestaurant(){
//
//        CompletableFuture<FeaturedRestaurantResponse> futureList = new CompletableFuture<>();
//
//        restaurantRepository.getAllRestaurantList(new RestaurantRepository.OnRestaurantListCallback() {
//            @Override
//            public void onSearchingSuccess(ArrayList<RestaurantEntity> allRestaurantsInApp) {
//                //System.out.println("Size en el service "  + allRestaurantsInApp.size());
//                //ArrayList<RestaurantResponse> allRests = restaurants.getRestaurantResponses();
//
//                // TODO----------->: AHORA FUNCIONANDO CON MENUS. SUSTITUIR PLATOS POR MENÚS CUANDO ESTÉN CON LA CONDICIÓN DE MÍNIMO 7
//
//                ArrayList<RestaurantEntity> restaurantsWithSevenMenus = new ArrayList<>();
//
//                for(RestaurantEntity restaurant : allRestaurantsInApp){
//                    System.out.println("El restaurante " + restaurant.getName() +" tiene: " + restaurant.getMenus().size()+ " platos" );
//                    if(restaurant.getMenus().size() >= 7) {
//                        //System.out.println("Este restaurante tiene: " + restaurant.getDishes().size()+ " platos" );
//                        restaurantsWithSevenMenus.add(restaurant);
//                    }
//                }
//                //System.out.println("Tamaño array Restaurantes " + restaurantsWithSevenMenus.size());
//                FeaturedRestaurantResponse featuredRestaurantResponse = new FeaturedRestaurantResponse(); // OBJETO QUE VOY A MANDAR AL FRONTTEND
//
//                if(restaurantsWithSevenMenus.isEmpty()){
//                    //System.out.println("-----X-----");
//                    futureList.complete(featuredRestaurantResponse);                                      // MANDO UN OBJETO VACÍO
//                }
//
//                int quantity = restaurantsWithSevenMenus.size();                                          // TAMAÑO DEL ARRAY CON RESTAURANTES Q CUMPLEN EL REQUISITO DE LOS MENÚS
//                int random = (int) (Math.random() * quantity) + 1;
//
//                RestaurantEntity chosenRestaurantEntity = restaurantsWithSevenMenus.get(random - 1);      // RESTAURANTE ELEGIDO PARA FEATURED
//
//                featuredRestaurantResponse.setUid(chosenRestaurantEntity.getUid());
//                featuredRestaurantResponse.setName(chosenRestaurantEntity.getName());
//                featuredRestaurantResponse.setDescription(chosenRestaurantEntity.getDescription());
//                featuredRestaurantResponse.setUrl(chosenRestaurantEntity.getUrl());
//                featuredRestaurantResponse.setImage(chosenRestaurantEntity.getImage());
//                featuredRestaurantResponse.setFood_type(chosenRestaurantEntity.getFood_type());
//
//                futureList.complete(featuredRestaurantResponse);
//            }
//
//            @Override
//            public void onSearchingFailure(Exception exception) {
//                // TODO -------> PREGUNTAR QUÉ QUIERE EL FRONTEND SI DA ERROR
//            }
//        });
//
//        //getAllFeaturedRestaurants();
//
//        return futureList;
//    }

    // ----------------------------------------------------------------------------------------------------------------

//    public CompletableFuture<ArrayList<RestaurantWithSevenDaysMenusResponse>> getRestaurantsWithNextSevenDaysMenus(){
//
//        CompletableFuture<ArrayList<RestaurantWithSevenDaysMenusResponse>> futureList = new CompletableFuture<>();
//
//        restaurantRepository.getAllRestaurantList(new RestaurantRepository.OnRestaurantListCallback() {
//            @Override
//            public void onSearchingSuccess(ArrayList<RestaurantEntity> allRestaurantsInApp) {
//
//                ArrayList<RestaurantEntity> restaurantsWithNextSevenDaysMenusEntity = new ArrayList<>(); // PARA METER LOS RESTAURANTES QUE LLEGUEN DEL REPOSITORIO Y CUMPLAN LA CONDICIÓN
//
//                System.out.println("Nº de restaurantes: " + allRestaurantsInApp.size());
//
//                for(RestaurantEntity restaurant : allRestaurantsInApp){
//                    // TODO----------->: CREAR CONDICIÓN MENÚ LOS PRÓXIMOS 7 DÍAS
//                    if(!restaurant.getMenus().isEmpty()) {
//                        restaurantsWithNextSevenDaysMenusEntity.add(restaurant);
//                    }
//                }
//
//                //System.out.println("Nº de restaurantes con más de 6 menús: " + restaurantsWithSevenDaysMenusEntity.size());
//
//                if(restaurantsWithNextSevenDaysMenusEntity.isEmpty()){
//                    System.out.println("-----X-----");
//                    futureList.complete(new ArrayList<RestaurantWithSevenDaysMenusResponse>());                                      // MANDO UN OBJETO VACÍO
//                }
//                else {
//                    ArrayList<RestaurantWithSevenDaysMenusResponse> restaurantsWithNextSevenDaysMenusResponse = new ArrayList<>();
//
//                    for(RestaurantEntity re : restaurantsWithNextSevenDaysMenusEntity){
//
//                        RestaurantWithSevenDaysMenusResponse restResponseToBeAdded = new RestaurantWithSevenDaysMenusResponse();
//
//                        restResponseToBeAdded.setName(re.getName());
//                        restResponseToBeAdded.setDescription_mini(re.getDescription_mini());
//                        restResponseToBeAdded.setUrl(re.getUrl());
//                        restResponseToBeAdded.setAddress(re.getAddress());
//                        restResponseToBeAdded.setPhone(re.getPhone());
//                        restResponseToBeAdded.setSchedule(re.getSchedule());
//                        restResponseToBeAdded.setFood_type(re.getFood_type());
//                        restResponseToBeAdded.setImage(re.getImage());
//                        restResponseToBeAdded.setRate(re.getRate());
//                        restResponseToBeAdded.setAverage_price(re.getAverage_price());
//                        restResponseToBeAdded.setLocation(re.getLocation());
//
////                        ArrayList<DishResponse> dishResponses = new ArrayList<>();
////
////                        for(DishEntity de : re.getDishes()){
////
////                            DishResponse dr = new DishResponse();
////
////                            dr.setId(de.getId());
////                            dr.setName(de.getName());
////                            dr.setIngredients(de.getIngredients());
////                            dr.setAllergens(de.getAllergens());
////                            dr.setImage(de.getImage());
////                            dr.setType(de.getType());
////
////                            dishResponses.add(dr);
////                        }
//                        restaurantsWithNextSevenDaysMenusResponse.add(restResponseToBeAdded);
//                    }
//                    //System.out.println(restaurantsWithAtLeastSevenMenusResponse.size());
//                    //for(RestaurantGetAllFormatResponse r : restaurantsWithAtLeastSevenMenusResponse){
//                        //System.out.println("Name: " + r.getName());
//                    //}
//                    futureList.complete(restaurantsWithNextSevenDaysMenusResponse);
//                }
//            }
//
//            @Override
//            public void onSearchingFailure(Exception exception) {
//                // TODO -------> PREGUNTAR QUÉ QUIERE EL FRONTEND SI DA ERROR
//            }
//        });
//
//        return futureList;
//    }

    // ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<ArrayList<RestaurantWithMenusResponse>> getAllRestaurantWithMenus(){

        CompletableFuture<ArrayList<RestaurantWithMenusResponse>> futureList = new CompletableFuture<>();

        restaurantRepository.getAllRestaurantList(new RestaurantRepository.OnRestaurantListCallback() {
            @Override
            public void onSearchingSuccess(ArrayList<RestaurantEntity> allRestaurantsInApp) {

                ArrayList<RestaurantEntity> restaurantsWithMenus = new ArrayList<>(); // PARA METER LOS RESTAURANTES QUE LLEGUEN DEL REPOSITORIO Y CUMPLAN LA CONDICIÓN

                for(RestaurantEntity restaurant : allRestaurantsInApp){
                    if(!restaurant.getMenus().isEmpty()) {
                        restaurantsWithMenus.add(restaurant);
                    }
                }

                if(restaurantsWithMenus.isEmpty()){
                    futureList.complete(new ArrayList<RestaurantWithMenusResponse>());                                      // MANDO UN OBJETO VACÍO
                }
                else {

                    // TRANSFORMO LOS ENTITY EN RESPONSE
                    ArrayList<RestaurantWithMenusResponse> restsWithMenuResponse = new ArrayList<>();

                    for(RestaurantEntity re : restaurantsWithMenus){

                        RestaurantWithMenusResponse restToBeAddedResponse = new RestaurantWithMenusResponse();

                        restToBeAddedResponse.setName(re.getName());
                        restToBeAddedResponse.setDescription_mini(re.getDescription_mini());
                        restToBeAddedResponse.setUrl(re.getUrl());
                        restToBeAddedResponse.setAddress(re.getAddress());
                        restToBeAddedResponse.setCity(re.getCity());
                        restToBeAddedResponse.setPhone(re.getPhone());
                        restToBeAddedResponse.setSchedule(re.getSchedule());
                        restToBeAddedResponse.setFood_type(re.getFood_type());
                        restToBeAddedResponse.setDescription(re.getDescription());
                        restToBeAddedResponse.setImage(re.getImage());
                        restToBeAddedResponse.setAverage_rate(re.getAverage_rate());
                        //restToBeAddedResponse.setAverage_price(re.getAverage_price());
                        restToBeAddedResponse.setLocation(re.getLocation());

                        ArrayList<MenuReadResponse> menus = new ArrayList<>();
                        for(MenuEntity menu : re.getMenus()){
                            int id = menu.getId();
                            int date = menu.getDate();
                            ArrayList<Integer> first_course = new ArrayList<>();
                            for(Integer first : menu.getFirst_course()){
                                first_course.add(first);
                            }

                            ArrayList<Integer> second_course = new ArrayList<>();
                            for(Integer second : menu.getSecond_course()){
                                second_course.add(second);
                            }
                            int dessert = menu.getDessert();

                            float priceWithDessert = menu.getPriceWithDessert();
                            float priceWithNoDessert = menu.getPriceNoDessert();

                            menus.add(new MenuReadResponse(id, date, first_course, second_course ,dessert, priceWithDessert, priceWithNoDessert));
                        }

                        restToBeAddedResponse.setMenus(menus);
                        restsWithMenuResponse.add(restToBeAddedResponse);
                    }

                    futureList.complete(restsWithMenuResponse);
                }
            }

            @Override
            public void onSearchingFailure(Exception exception) {
                // TODO -------> PREGUNTAR QUÉ QUIERE EL FRONTEND SI DA ERROR
            }
        });

        return futureList;
    }

    // ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<RestaurantGetByUrlResponse> getRestaurantByUrl(String url){

        CompletableFuture<RestaurantGetByUrlResponse> futureResponse = new CompletableFuture<>();

        restaurantRepository.getByUrl(url, new RestaurantRepository.OnRestByUrlGot() {
            @Override
            public void onSearchingSuccess(RestaurantGetByUrlEntity restaurantGetByUrlEntity) {
                String uid = restaurantGetByUrlEntity.getUid();
                String name = restaurantGetByUrlEntity.getName();
                String food_type = restaurantGetByUrlEntity.getFood_type();
                String address = restaurantGetByUrlEntity.getAddress();
                String image = restaurantGetByUrlEntity.getImage();
                String phone = restaurantGetByUrlEntity.getPhone();
                String schedule = restaurantGetByUrlEntity.getSchedule();
                Integer rate = restaurantGetByUrlEntity.getRate();
                String description = restaurantGetByUrlEntity.getDescription();
                String city = restaurantGetByUrlEntity.getCity();

                ArrayList<ReviewsResponse> reviews = new ArrayList<>();
                for(ReviewsEntity reviewsEntity : restaurantGetByUrlEntity.getReviews()){
                    ReviewsResponse revResponse = new ReviewsResponse();
                    revResponse.setName(reviewsEntity.getName());
                    revResponse.setReview(reviewsEntity.getReview());
                    revResponse.setRate(reviewsEntity.getRate());
                    reviews.add(revResponse);
                }

                ArrayList<MenuGetByUrlResponse> menus = new ArrayList<>();
                for(MenuGetByUrlEntity menuEntity : restaurantGetByUrlEntity.getMenus()){

                    MenuGetByUrlResponse menu = new MenuGetByUrlResponse();
                    menu.setId(menuEntity.getId());
                    menu.setDate(menuEntity.getDate());
                    menu.setFirst_course(menuEntity.getFirst_course());
                    menu.setSecond_course(menuEntity.getSecond_course());
                    menu.setDessert(menuEntity.getDessert());
                    menu.setPriceWithDessert(menuEntity.getPriceWithDessert());
                    menu.setPriceNoDessert(menuEntity.getPriceNoDessert());
                    menus.add(menu);
                }
                RestaurantGetByUrlResponse restaurantGetByUrlResponse = new RestaurantGetByUrlResponse(uid, name, food_type,
                        address, image, phone, schedule, rate, description, city, reviews, menus);

                futureResponse.complete(restaurantGetByUrlResponse);
            }

            @Override
            public void onSearchingFailure(Exception exception) {
                futureResponse.completeExceptionally(exception);
            }
        });

        return futureResponse;
    }

    // ----------------------------------------------------------------------------------------------------------------


}
