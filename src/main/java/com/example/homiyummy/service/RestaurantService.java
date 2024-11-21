package com.example.homiyummy.service;

import com.example.homiyummy.model.restaurant.*;
import com.example.homiyummy.repository.RestaurantRepository;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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

    public CompletableFuture<Map<String,Set<String>>> getFoodTypes() {
        CompletableFuture<Map<String,Set<String>>> futureTypes = new CompletableFuture<>();
        Map<String, Set<String>> object = new HashMap<>();
        restaurantRepository.getAllFoodTypes(new RestaurantRepository.OnTypesGot() {
            @Override
            public void onTypesSuccess(Set<String> types) {
                object.put("food_type", types);
                futureTypes.complete(object);
            }

            @Override
            public void onTypesFailure(Exception exception) {
                //futureTypes.complete(exception);
                // TODO -------> PREGUNTAR QUÉ QUIERE EL FRONTEND SI DA ERROR
            }
        });
        return futureTypes;
    }

    // ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<Map<String, ArrayList<RestaurantGetAllFormatResponse>>> getAllRestaurants(){

        CompletableFuture<Map<String, ArrayList<RestaurantGetAllFormatResponse>>> futureAllRestaurants = new CompletableFuture<>();

        restaurantRepository.getAllRestaurantList(new RestaurantRepository.OnRestaurantListCallback() {
            @Override
            public void onSearchingSuccess(ArrayList<RestaurantEntity> restaurants) {

                ArrayList<RestaurantGetAllFormatResponse> restListResponse = new ArrayList<>();

                for(RestaurantEntity restaurant : restaurants){

                    RestaurantGetAllFormatResponse restaurantGetAllFormatResponse = new RestaurantGetAllFormatResponse();

                    //restaurantGetAllFormatResponse.setUid(restaurant.getUid());
                    restaurantGetAllFormatResponse.setName(restaurant.getName());
                    restaurantGetAllFormatResponse.setDescription_mini(restaurant.getDescription_mini());
                    restaurantGetAllFormatResponse.setUrl(restaurant.getUrl());
                    restaurantGetAllFormatResponse.setAddress(restaurant.getAddress());
                    restaurantGetAllFormatResponse.setPhone(restaurant.getPhone());
                    restaurantGetAllFormatResponse.setSchedule(restaurant.getSchedule());
                    restaurantGetAllFormatResponse.setFood_type(restaurant.getFood_type());
                    restaurantGetAllFormatResponse.setImage(restaurant.getImage());
                    restaurantGetAllFormatResponse.setRate(restaurant.getRate());
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

    public CompletableFuture<FeaturedRestaurantResponse> getTheOneFeaturedRestaurant(){

        CompletableFuture<FeaturedRestaurantResponse> futureList = new CompletableFuture<>();

        restaurantRepository.getAllRestaurantList(new RestaurantRepository.OnRestaurantListCallback() {
            @Override
            public void onSearchingSuccess(ArrayList<RestaurantEntity> allRestaurantsInApp) {
                //System.out.println("Size en el service "  + allRestaurantsInApp.size());
                //ArrayList<RestaurantResponse> allRests = restaurants.getRestaurantResponses();

                // TODO----------->: AHORA FUNCIONANDO CON MENUS. SUSTITUIR PLATOS POR MENÚS CUANDO ESTÉN CON LA CONDICIÓN DE MÍNIMO 7

                ArrayList<RestaurantEntity> restaurantsWithSevenMenus = new ArrayList<>();

                for(RestaurantEntity restaurant : allRestaurantsInApp){
                    System.out.println("El restaurante " + restaurant.getName() +" tiene: " + restaurant.getMenus().size()+ " platos" );
                    if(restaurant.getMenus().size() >= 7) {
                        //System.out.println("Este restaurante tiene: " + restaurant.getDishes().size()+ " platos" );
                        restaurantsWithSevenMenus.add(restaurant);
                    }
                }
                //System.out.println("Tamaño array Restaurantes " + restaurantsWithSevenMenus.size());
                FeaturedRestaurantResponse featuredRestaurantResponse = new FeaturedRestaurantResponse(); // OBJETO QUE VOY A MANDAR AL FRONTTEND

                if(restaurantsWithSevenMenus.isEmpty()){
                    //System.out.println("-----X-----");
                    futureList.complete(featuredRestaurantResponse);                                      // MANDO UN OBJETO VACÍO
                }

                int quantity = restaurantsWithSevenMenus.size();                                          // TAMAÑO DEL ARRAY CON RESTAURANTES Q CUMPLEN EL REQUISITO DE LOS MENÚS
                int random = (int) (Math.random() * quantity) + 1;

                RestaurantEntity chosenRestaurantEntity = restaurantsWithSevenMenus.get(random - 1);      // RESTAURANTE ELEGIDO PARA FEATURED

                featuredRestaurantResponse.setUid(chosenRestaurantEntity.getUid());
                featuredRestaurantResponse.setName(chosenRestaurantEntity.getName());
                featuredRestaurantResponse.setDescription(chosenRestaurantEntity.getDescription());
                featuredRestaurantResponse.setUrl(chosenRestaurantEntity.getUrl());
                featuredRestaurantResponse.setImage(chosenRestaurantEntity.getImage());
                featuredRestaurantResponse.setFood_type(chosenRestaurantEntity.getFood_type());

                futureList.complete(featuredRestaurantResponse);
            }

            @Override
            public void onSearchingFailure(Exception exception) {
                // TODO -------> PREGUNTAR QUÉ QUIERE EL FRONTEND SI DA ERROR
            }
        });

        //getAllFeaturedRestaurants();

        return futureList;
    }

    // ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<ArrayList<RestaurantWithSevenDaysMenusResponse>> getRestaurantsWithNextSevenDaysMenus(){

        CompletableFuture<ArrayList<RestaurantWithSevenDaysMenusResponse>> futureList = new CompletableFuture<>();

        restaurantRepository.getAllRestaurantList(new RestaurantRepository.OnRestaurantListCallback() {
            @Override
            public void onSearchingSuccess(ArrayList<RestaurantEntity> allRestaurantsInApp) {

                ArrayList<RestaurantEntity> restaurantsWithNextSevenDaysMenusEntity = new ArrayList<>(); // PARA METER LOS RESTAURANTES QUE LLEGUEN DEL REPOSITORIO Y CUMPLAN LA CONDICIÓN

                System.out.println("Nº de restaurantes: " + allRestaurantsInApp.size());

                for(RestaurantEntity restaurant : allRestaurantsInApp){
                    // TODO----------->: CREAR CONDICIÓN MENÚ LOS PRÓXIMOS 7 DÍAS
                    if(!restaurant.getMenus().isEmpty()) {
                        restaurantsWithNextSevenDaysMenusEntity.add(restaurant);
                    }
                }

                //System.out.println("Nº de restaurantes con más de 6 menús: " + restaurantsWithSevenDaysMenusEntity.size());

                if(restaurantsWithNextSevenDaysMenusEntity.isEmpty()){
                    System.out.println("-----X-----");
                    futureList.complete(new ArrayList<RestaurantWithSevenDaysMenusResponse>());                                      // MANDO UN OBJETO VACÍO
                }
                else {
                    ArrayList<RestaurantWithSevenDaysMenusResponse> restaurantsWithNextSevenDaysMenusResponse = new ArrayList<>();

                    for(RestaurantEntity re : restaurantsWithNextSevenDaysMenusEntity){

                        RestaurantWithSevenDaysMenusResponse restResponseToBeAdded = new RestaurantWithSevenDaysMenusResponse();

                        restResponseToBeAdded.setName(re.getName());
                        restResponseToBeAdded.setDescription_mini(re.getDescription_mini());
                        restResponseToBeAdded.setUrl(re.getUrl());
                        restResponseToBeAdded.setAddress(re.getAddress());
                        restResponseToBeAdded.setPhone(re.getPhone());
                        restResponseToBeAdded.setSchedule(re.getSchedule());
                        restResponseToBeAdded.setFood_type(re.getFood_type());
                        restResponseToBeAdded.setImage(re.getImage());
                        restResponseToBeAdded.setRate(re.getRate());
                        restResponseToBeAdded.setAverage_price(re.getAverage_price());
                        restResponseToBeAdded.setLocation(re.getLocation());

//                        ArrayList<DishResponse> dishResponses = new ArrayList<>();
//
//                        for(DishEntity de : re.getDishes()){
//
//                            DishResponse dr = new DishResponse();
//
//                            dr.setId(de.getId());
//                            dr.setName(de.getName());
//                            dr.setIngredients(de.getIngredients());
//                            dr.setAllergens(de.getAllergens());
//                            dr.setImage(de.getImage());
//                            dr.setType(de.getType());
//
//                            dishResponses.add(dr);
//                        }
                        restaurantsWithNextSevenDaysMenusResponse.add(restResponseToBeAdded);
                    }
                    //System.out.println(restaurantsWithAtLeastSevenMenusResponse.size());
                    //for(RestaurantGetAllFormatResponse r : restaurantsWithAtLeastSevenMenusResponse){
                        //System.out.println("Name: " + r.getName());
                    //}
                    futureList.complete(restaurantsWithNextSevenDaysMenusResponse);
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

}
