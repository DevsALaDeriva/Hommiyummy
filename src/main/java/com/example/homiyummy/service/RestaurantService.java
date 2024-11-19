package com.example.homiyummy.service;

import com.example.homiyummy.model.dish.DishEntity;
import com.example.homiyummy.model.dish.DishResponse;
import com.example.homiyummy.model.restaurant.*;
import com.example.homiyummy.repository.RestaurantRepository;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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

    public CompletableFuture<FeaturedRestaurantResponse> getTheOneFeaturedRestaurant(){

        CompletableFuture<FeaturedRestaurantResponse> futureList = new CompletableFuture<>();

        restaurantRepository.getAllRestaurantList(new RestaurantRepository.OnRestaurantListCallback() {
            @Override
            public void onSearchingSuccess(ArrayList<RestaurantEntity> allRestaurantsInApp) {
                //System.out.println("Size en el service "  + restaurants.size());
                //ArrayList<RestaurantResponse> allRests = restaurants.getRestaurantResponses();

                // TODO----------->: AHORA FUNCIONANDO CON MENUS. SUSTITUIR PLATOS POR MENÚS CUANDO ESTÉN CON LA CONDICIÓN DE MÍNIMO 7

                ArrayList<RestaurantEntity> restaurantsWithSevenMenus = new ArrayList<>();

                for(RestaurantEntity restaurant : allRestaurantsInApp){
                    //System.out.println("Este restaurante tiene: " + restaurant.getDishes().size()+ " platos" );
                    if(restaurant.getDishes().size() >= 2) {
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

        getAllFeaturedRestaurants();

        return futureList;
    }

    // ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<ArrayList<RestaurantResponse>> getAllFeaturedRestaurants(){

        CompletableFuture<ArrayList<RestaurantResponse>> futureList = new CompletableFuture<>();

        // return restaurantRepository.getRestaurantList();
        restaurantRepository.getAllRestaurantList(new RestaurantRepository.OnRestaurantListCallback() {
            @Override
            public void onSearchingSuccess(ArrayList<RestaurantEntity> allRestaurantsInApp) {
                //System.out.println("Size en el service "  + restaurants.size());
                //ArrayList<RestaurantResponse> allRests = restaurants.getRestaurantResponses();

                ArrayList<RestaurantEntity> restaurantsWithSevenMenusEntity = new ArrayList<>(); // PARA METER LOS RESTAURANTES QUE LLEGUEN DEL REPOSITORIO Y CUMPLAN LA CONDICIÓN

                for(RestaurantEntity restaurant : allRestaurantsInApp){
                    //System.out.println("Este restaurante tiene: " + restaurant.getDishes().size()+ " platos" );
                    // TODO----------->: AHORA FUNCIONANDO CON MENUS. SUSTITUIR PLATOS POR MENÚS CUANDO ESTÉN CON LA CONDICIÓN DE MÍNIMO 7
                    if(restaurant.getDishes().size() >= 2) {
                        //System.out.println("Este restaurante tiene: " + restaurant.getDishes().size()+ " platos" );
                        restaurantsWithSevenMenusEntity.add(restaurant);
                    }
                }

                if(restaurantsWithSevenMenusEntity.isEmpty()){
                    //System.out.println("-----X-----");
                    futureList.complete(new ArrayList<RestaurantResponse>());                                      // MANDO UN OBJETO VACÍO
                }
                else {
                    //System.out.println("tamaño: " + restaurantsWithSevenMenus.size());
                    ArrayList<RestaurantResponse> restaurantsWithAtLeastSevenMenusResponse = new ArrayList<>();

                    for(RestaurantEntity re : restaurantsWithSevenMenusEntity){

                        RestaurantResponse restResponseToBeAdded = new RestaurantResponse();

                        restResponseToBeAdded.setEmail(re.getEmail());
                        restResponseToBeAdded.setName(re.getName());
                        restResponseToBeAdded.setDescription_mini(re.getDescription_mini());
                        restResponseToBeAdded.setDescription(re.getDescription());
                        restResponseToBeAdded.setUrl(re.getUrl());
                        restResponseToBeAdded.setAddress(re.getAddress());
                        restResponseToBeAdded.setCity(re.getCity());
                        restResponseToBeAdded.setPhone(re.getPhone());
                        restResponseToBeAdded.setSchedule(re.getSchedule());
                        restResponseToBeAdded.setFood_type(re.getFood_type());

                        ArrayList<DishResponse> dishResponses = new ArrayList<>();

                        for(DishEntity de : re.getDishes()){

                            DishResponse dr = new DishResponse();

                            dr.setUid(de.getUid());
                            dr.setId(de.getId());
                            dr.setName(de.getName());
                            dr.setIngredients(de.getIngredients());
                            dr.setAllergens(de.getAllergens());
                            dr.setImage(de.getImage());
                            dr.setType(de.getType());

                            dishResponses.add(dr);
                        }
                        restaurantsWithAtLeastSevenMenusResponse.add(restResponseToBeAdded);
                    }
                    //System.out.println(restaurantsWithAtLeastSevenMenusResponse.size());
                    for(RestaurantResponse r : restaurantsWithAtLeastSevenMenusResponse){
                        System.out.println("Name: " + r.getName());
                    }
                    futureList.complete(restaurantsWithAtLeastSevenMenusResponse);
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
