package com.example.homiyummy.service;

import com.example.homiyummy.model.menu.*;
import com.example.homiyummy.model.restaurant.*;
import com.example.homiyummy.model.reviews.ReviewsEntity;
import com.example.homiyummy.model.reviews.ReviewsResponse;
import com.example.homiyummy.repository.RestaurantRepository;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
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

    /**
     * RECIBE UN RestaurantDTO DEL CONTROLLER, LO CONVIERTE EN UN RestaurantEntity Y LO MANDA AL REPOSITORIO
     * @param restaurantDTO RESTAURANTE ENVIADO POR EL FRONTEND
     * @return SI TIENE ÉXITO, UN OBJETO EL OBJETO RestaurantResponse PROCEDENTE DEL REPOSITORIO
     *         SI NO, UNA EXCEPCIÓN
     */
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
            return future.get();
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

    /**
     * SOLICITA AL REPOSITORIO QUE BUSQUE UN RESTARUANTE CON EL UID QUE LLEGA COMO PARÁMETRO.
     * LLEGA EN FORMATO RestaurantReadResponse
     * @param uid UID DEL RESTAURANTE
     * @return SI TIENE EXITO, DEVUELVE EL RestaurantReadResponse, QUE LLEGARÁ CON DATOS (SI LOS HABÍA) O VACÍO
     */
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

    /**
     * ENVÍA AL REPOSITORIO UNA PETICIÓN PARA EXTRAER TODOS LOS RESTAURANTES QUE CONTIENEN MENÚS.
     * MANEJA LA RESPUESTA CON LA INTERFAZ RestaurantRepository.OnRestaurantListCallback
     *
     * @return SI TIENE EXITO CONVIERTE EL ARRAYLIST DE OBJETOS RestaurantEntity QUE LLEGA
     *         EN UN ARRAYLIST CON OBJETOS RestaurantWithMenusResponse
     */
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
                futureList.completeExceptionally(exception);
            }
        });

        return futureList;
    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     * MANDA AL REPOSITORIO LA URL DEL RESTAURANTE QUE QUIERE OBTENER.
     * COMO SEGUNDO PARÁMETRO USAMOS UNA INSTANCIA DE LA INTERFAZ RestaurantRepository.OnRestByUrlGot
     * QUE USAREMOS PARA MANEJAR LA RESPUESTA
     * @param url URL DEL RESTAURANTE
     * @return SI ES EXITOSO RECIBIMOS UN OBJETO RestaurantGetByUrlEntity, LO CONVERTIMOS EN RestaurantGetByUrlResponse Y LO MANDAMOS AL CONTROLLER
     */
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
                Integer average_rate = restaurantGetByUrlEntity.getAverage_rate();
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
                        address, image, phone, schedule, average_rate, description, city, reviews, menus);

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

    public CompletableFuture<RestaurantGetAllMenusResponse> getAllMenus(String uid){

        CompletableFuture<RestaurantGetAllMenusResponse> futureMenus = new CompletableFuture<>();

        restaurantRepository.getMenus(uid, new RestaurantRepository.OnMenusGot() {
            @Override
            public void onSearchingSuccess(RestaurantGetAllMenusEntity allMenusEntity) {

                RestaurantGetAllMenusResponse menusResponse = new RestaurantGetAllMenusResponse();

                ArrayList<MenuGetAllMenusResponse> menus = new ArrayList<>();

                for(MenuGetAllMenusEntity menuEntity : allMenusEntity.getMenus()){

                    MenuGetAllMenusResponse menu = new MenuGetAllMenusResponse();

                    menu.setId(menuEntity.getId());
                    menu.setDate(menuEntity.getDate());
                    menu.setFirst_course(menuEntity.getFirst_course());
                    menu.setSecond_course(menuEntity.getSecond_course());
                    menu.setDessert(menuEntity.getDessert());
                    menu.setPriceWithDessert(menuEntity.getPriceWithDessert());
                    menu.setPriceNoDessert(menuEntity.getPriceNoDessert());
                    menus.add(menu);
                }

                menusResponse.setMenus(menus);
                futureMenus.complete(menusResponse);
            }

            @Override
            public void onSearchingFailure(Exception exception) {
                futureMenus.completeExceptionally(exception);
            }
        });
        return futureMenus;
    }

    // ----------------------------------------------------------------------------------------------------------------

}
