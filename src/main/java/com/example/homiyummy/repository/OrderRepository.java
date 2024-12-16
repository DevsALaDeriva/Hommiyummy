package com.example.homiyummy.repository;

import com.example.homiyummy.model.dish.DishGetByEntity;
import com.example.homiyummy.model.dish.DishGetDayTaskEntity;
import com.example.homiyummy.model.menu.MenuGetByUrlEntity;
import com.example.homiyummy.model.menu.MenuGetByNumEntity;
import com.example.homiyummy.model.menu.MenuInGetTaskEntity;
import com.example.homiyummy.model.order.*;
import com.example.homiyummy.model.reviews.ReviewsGetByNumOrderEntity;
import com.example.homiyummy.model.user.UserInGetTaskEntity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderRepository {

    DatabaseReference databaseReference;

    public OrderRepository(DatabaseReference databaseReference){
        this.databaseReference = databaseReference;
    }


    // ----------------------------------------------------------------------------------------------------------------

    /**
     * CREAMOS NODO COUNTER PARA GUARDAR EL ID ACTUAL.
     * CON DATASNAPSHOT.EXISTS() COMPROBAMOS SI EL NODO CONTIENE DATOS, NO SI EXISTE.
     * POR ESO, LA PRIMERA VEZ DEVUELVE FALSE Y SALTA AL ELSE, EN EL QUE ASIGNAMOS 0.
     *
     * @PARAM CALLBACK: ONFINDINGSUCCESS CUANDO:
     *                              - ENCUENTRA UN DATO GUARDADO EN EL NODO
     *                              - Y CUANDO NO LO ENCUENTRA (QUE ASIGNA 0)
     *                  ONFINDINGFAILURE CUANDO:
     *                              - SE ENCUENTRA COMO DATO UN NULL
     *                              - O CUANDO ENTRA EN ONCANCELLED
     * @PARAM UID EL IDENTIFICADOR ÚNICO DEL RESTAURANTE
     */

    public void findId(String uid, OnOrderIdGot callback) {

        DatabaseReference restaurantRef = databaseReference.child("restaurants").child(uid);
        DatabaseReference ordersRef = restaurantRef.child("orders");
        DatabaseReference counterRef = ordersRef.child("counter");

        counterRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Integer actualCounter = dataSnapshot.getValue(Integer.class);
                    if(actualCounter != null) {
                        callback.onFindingSuccess(actualCounter);
                    }
                    else{
                        callback.onFindingFailure(new Exception("El valor del contador en Realtime es null"));
                    }
                }
                else{
                    callback.onFindingSuccess(0);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFindingFailure(databaseError.toException());
            }
        });
    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     *
     * @param orderDTO EL OBJETO ENTRANTE QUE HAY Q CONVERTIR EN ENTITY ANTES DE GUARDAR
     * @param newId EL ID QUE SE LE VA A ASIGNAR AL PEDIDO
     * @param callback SI SUCCESS -> DEVUELVE EL OBJETO GUARDADO
     *                 SI ERRROR -> DEVUELVE UNA EXCEPCIÓN CON EL TEXTO PERTINENTE
     */
    public void save(OrderDTO orderDTO, int newId, OnSavingOrderCallback callback){

        OrderEntity orderEntity = new OrderEntity();

        orderEntity.setNum_order(orderDTO.getNum_order());
        orderEntity.setDate(orderDTO.getDate());
        orderEntity.setUidCustomer(orderDTO.getUidCustomer());
        orderEntity.setMenus(orderDTO.getMenus());
        orderEntity.setTotal(orderDTO.getTotal());

        DatabaseReference restaurantRef = databaseReference.child("restaurants").child(orderDTO.getUid());
        DatabaseReference allOrdersRef = restaurantRef.child("orders");
        DatabaseReference counterRef = allOrdersRef.child("counter");
        DatabaseReference itemsRef = allOrdersRef.child("items");
        DatabaseReference thisOrderRef = itemsRef.child(String.valueOf(newId)); // A LA VEZ QUE CREAMOS EL NODO DEL NUEVO PEDIDO, LO REFERENCIAMOS

        counterRef.setValue(newId, (databaseCounterError, databaseCounterReference) -> {
            if(databaseCounterError != null){
                callback.onSavingOrderFailure(new Exception("Error al actualizar el contador de los pedidos: " + databaseCounterError.getMessage()));
                return;
            }

            counterRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Integer orderId = dataSnapshot.getValue(Integer.class);
                    if(orderId != null){
                        thisOrderRef.setValue(orderEntity, (databaseOrderError, databaseOrderReference) -> {
                            if(databaseOrderError != null){
                                callback.onSavingOrderFailure(new Exception("Error al guardar el pedido: " + databaseOrderError.getMessage()));
                                return;
                            }

                            OrderCreatedResponse orderCreatedResponse = new OrderCreatedResponse(orderId);
                            callback.onSavingOrderSuccess(orderCreatedResponse);
                        });
                    } else {
                        callback.onSavingOrderFailure(new Exception("El valor del contador es nulo"));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    callback.onSavingOrderFailure(databaseError.toException());
                }
            });
        });

    }

    // ----------------------------------------------------------------------------------------------------------------

    public interface OnOrderIdGot{
        void onFindingSuccess(int id);
        void onFindingFailure(Exception exception);
    }

    // ----------------------------------------------------------------------------------------------------------------

    public interface OnSavingOrderCallback{
        void onSavingOrderSuccess(OrderCreatedResponse orderCreatedResponse);
        void onSavingOrderFailure(Exception exception);
    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     * PROVEE DE LOS DATOS DE UN PEDIDO DE CARA AL RESTAURANTE
     *
     * @param orderNumber NÚMERO DE PEDIDO
     * @param callback SI SUCCESS -> DEVUEVLE UN OBJETO OrderGotByNumEntity CON LOS DATOS DEL PEDIDO OBTENIDOS DIRECTAMENTE DE LA BBDD
     *                 SI ERROR   -> DEVUELVE CON onFindingFailure UNA EXCEPCIÓN CON EL MENSAJE DE LO OCURRIDO
     */

    public void getOrderDataInRestaurantSide(String orderNumber, OnOrderGotCallback callback){

        DatabaseReference allRestaurantsRef = databaseReference.child("restaurants");
        allRestaurantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot restNode : dataSnapshot.getChildren()){

                        DataSnapshot ordersRef = restNode.child("orders/items");

                        if(ordersRef.exists()){
                            for(DataSnapshot order : ordersRef.getChildren()){

                                String value = order.child("num_order").getValue(String.class);

                                if(value.equals(orderNumber)){
                                    String restUID = restNode.getKey();
                                    String customerUID = order.child("uidCustomer").getValue(String.class);
                                    int date = order.child("date").getValue(Integer.class);
                                    float totalOrder = order.child("total").getValue(Float.class);

                                    DataSnapshot menusSnapshot = order.child("menus");

                                    if(menusSnapshot.exists()){

                                        ArrayList<MenuGetByNumEntity> menusInOrder = new ArrayList<>();

                                        String orderStatus = "";
                                        int menusQuantity = (int)menusSnapshot.getChildrenCount();
                                        int contador = 0;

                                        for(DataSnapshot menu : menusSnapshot.getChildren()){

                                            int menuId = menu.child("id").getValue(Integer.class);
                                            int menuDate = restNode.child("menus/items").child(String.valueOf(menuId)).child("date").getValue(Integer.class);


                                            int firstCourseID = menu.child("first_course").getValue(Integer.class);
                                            String firstCourseName = restNode.child("dishes/items").child(String.valueOf(firstCourseID)).child("name").getValue(String.class);
                                            String firstCourseIngs = restNode.child("dishes/items").child(String.valueOf(firstCourseID)).child("ingredients").getValue(String.class);
                                            ArrayList<String> firstAllergens = new ArrayList<>();
                                            DataSnapshot firstCourseAllergens = restNode.child("dishes/items")
                                                    .child(String.valueOf(firstCourseID)).child("allergens");
                                            for(DataSnapshot allergen: firstCourseAllergens.getChildren()){
                                                firstAllergens.add(allergen.getValue(String.class));
                                            }

                                            String firstCourseImg = restNode.child("dishes/items").child(String.valueOf(firstCourseID)).child("image").getValue(String.class);

                                            DishGetByEntity firstCourseEntity = new DishGetByEntity(firstCourseID, firstCourseName, firstCourseIngs, firstAllergens, firstCourseImg);


                                            int secondCourseID = menu.child("second_course").getValue(Integer.class);
                                            String secondCourseName = restNode.child("dishes/items").child(String.valueOf(secondCourseID)).child("name").getValue(String.class);
                                            String secondCourseIngs = restNode.child("dishes/items").child(String.valueOf(secondCourseID)).child("ingredients").getValue(String.class);
                                            ArrayList<String> secondAllergens = new ArrayList<>();
                                            DataSnapshot secondCourseAllergens = restNode.child("dishes/items")
                                                    .child(String.valueOf(secondCourseID)).child("allergens");
                                            for(DataSnapshot allergen : secondCourseAllergens.getChildren()){
                                                secondAllergens.add(allergen.getValue(String.class));
                                            }

                                            String secondCourseImg = restNode.child("dishes/items").child(String.valueOf(secondCourseID)).child("image").getValue(String.class);
                                            DishGetByEntity secondCourseEntity = new DishGetByEntity(secondCourseID, secondCourseName, secondCourseIngs, secondAllergens, secondCourseImg);


                                            int dessertID = menu.child("dessert").getValue(Integer.class);
                                            String dessertName = restNode.child("dishes/items").child(String.valueOf(dessertID)).child("name").getValue(String.class);
                                            String dessertIngs = restNode.child("dishes/items").child(String.valueOf(dessertID)).child("ingredients").getValue(String.class);
                                            ArrayList<String> dessertAllergens = new ArrayList<>();
                                            DataSnapshot dessertCourseAllergens = restNode.child("dishes/items")
                                                    .child(String.valueOf(dessertID)).child("allergens");
                                            for(DataSnapshot allergen : dessertCourseAllergens.getChildren()){
                                                dessertAllergens.add(allergen.getValue(String.class));
                                            }

                                            String dessertImg = restNode.child("dishes/items").child(String.valueOf(dessertID)).child("image").getValue(String.class);
                                            DishGetByEntity dessertEntity = new DishGetByEntity(dessertID, dessertName, dessertIngs, dessertAllergens, dessertImg);

                                            float totalMenu = menu.child("price").getValue(Float.class);
                                            String status = menu.child("status").getValue(String.class);


                                            if(menu.child("status").getValue(String.class).equals("complete")){
                                                contador++;
                                            }
                                            if(menusQuantity == contador){
                                                orderStatus = "complete";
                                            } else {
                                                orderStatus = "in_progress";
                                            }

                                            MenuGetByNumEntity singleMenu = new MenuGetByNumEntity(menuId, menuDate, firstCourseEntity, secondCourseEntity, dessertEntity, totalMenu, status); // CREAMOS CADA MENU
                                            menusInOrder.add(singleMenu);
                                        }

                                        ReviewsGetByNumOrderEntity reviewEntity = null;
                                        DataSnapshot reviewSnapshot = order.child("reviews");
                                        if (reviewSnapshot.exists()) {
                                            int reviewRate = reviewSnapshot.child("rate").getValue(Integer.class);
                                            String text = reviewSnapshot.child("review").getValue(String.class);
                                            reviewEntity = new ReviewsGetByNumOrderEntity();
                                            reviewEntity.setRate(reviewRate);
                                            reviewEntity.setReview(text);

                                            OrderGotByNumEntity dataEntity = new OrderGotByNumEntity(restUID, date, customerUID, menusInOrder, reviewEntity, orderStatus, totalOrder);
                                            callback.onFindingSuccess(dataEntity);
                                        } else {
                                            ArrayList<String> emptyReview = new ArrayList<>();
                                            OrderGotByNumEntity dataEntity = new OrderGotByNumEntity(restUID, date, customerUID, menusInOrder, emptyReview, orderStatus, totalOrder);
                                            callback.onFindingSuccess(dataEntity);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    callback.onFindingFailure(new Exception("No existe ese número de pedido."));
                }
                else {
                    callback.onFindingFailure(new Exception("No se han encontrado restaurantes en la base de datos."));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFindingFailure(databaseError.toException());
            }
        });
    }

    // ----------------------------------------------------------------------------------------------------------------

    public interface OnOrderGotCallback{
        void onFindingSuccess(OrderGotByNumEntity orderEntity);
        void onFindingFailure(Exception exception);
    }

// ----------------------------------------------------------------------------------------------------------------

    /**
     *  RECORRE TODOS LOS RESTAURANTES Y OBTIENE LOS PEDIDOS QUE EL CLIENTE TENGA
     *
     * @param clientUID EL NÚMERO DE USUARIO DEL CLIENTE EN LA APLICACIÓN (ASIGNADO EN AUTHENTICATION DE FIREBASE)
     * @param callback SI SUCCESS -> UN ARRAYLIST CON TODOS LOS PEDIDOS EN FORMATO OrderGetClientOrdersEntity
     *                 SI ERROR -> DEVUELVE UNA EXCECPIÓN CON UN MENSAJE
     */

    public void getClientOrders(String clientUID, OnClientOrdersGotCallback callback){

        DatabaseReference allRestaurantsRef = databaseReference.child("restaurants");

        allRestaurantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    ArrayList<OrderGetClientOrdersEntity> allOrdersEntity = new ArrayList<>();

                    for(DataSnapshot restNode : dataSnapshot.getChildren()){
                        DataSnapshot ordersRef = restNode.child("orders/items");

                        if(ordersRef.exists()){
                            for(DataSnapshot ord : ordersRef.getChildren()){

                                String clientSavedUid = ord.child("uidCustomer").getValue(String.class);

                                if(clientSavedUid != null && clientSavedUid.equals(clientUID)) {
                                    if(clientSavedUid.equals(clientUID)){

                                        String restName = restNode.child("name").getValue(String.class);
                                        String restImage = restNode.child("image").getValue(String.class);
                                        Integer date = ord.child("date").getValue(Integer.class);
                                        String numOrder = ord.child("num_order").getValue(String.class);
                                        Float total = ord.child("total").getValue(Float.class);

                                        DataSnapshot menusSnapshot = ord.child("menus");
                                        int menusQuantity = (int) menusSnapshot.getChildrenCount();
                                        int contador = 0;
                                        String orderStatus = "";

                                        for(DataSnapshot menu : menusSnapshot.getChildren()){
                                            if("complete".equals(menu.child("status").getValue(String.class))){
                                                contador++;
                                            }
                                        }

                                        if(menusQuantity == contador){
                                            orderStatus = "complete";
                                        } else {
                                            orderStatus = "in_progress";
                                        }

                                        OrderGetClientOrdersEntity orderEntity = new OrderGetClientOrdersEntity();

                                        orderEntity.setName_restaurant(restName);
                                        orderEntity.setImage_restaurant(restImage);
                                        orderEntity.setDate(date);
                                        orderEntity.setNum_order(numOrder);
                                        orderEntity.setTotal(total);
                                        orderEntity.setStatus(orderStatus);

                                        allOrdersEntity.add(orderEntity);
                                    }
                                }
                            }
                        }
                    }
                    callback.onFindingSuccess(allOrdersEntity);
                }
                else{
                    callback.onFindingFailure(new Exception("No existen restaurantes registrados."));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFindingFailure(new Exception("Error de conexión."));
            }
        });
    }

    // ----------------------------------------------------------------------------------------------------------------

    public interface OnClientOrdersGotCallback{
        void onFindingSuccess(ArrayList<OrderGetClientOrdersEntity> ordersEntity);
        void onFindingFailure(Exception exception);
    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     *  PROVEE DE TODOS LOS PEDIDOS DE UN RESTAURANTE
     *
     * @param uid EL NUMERO DE USUARIO DEL RESTAURANTE EN LA APP (ASIGNADO EN AUTHENTICATION DE FIREBASE)
     * @param callback SI SUCCESS -> DEVUELVE UN ARRAY CON TODOS LOS PEDIDOS EN FORMATO OrderGetRestaurantOrdersEntity, O VACÍO SI NO TIENE
     *                 SI FALLO   -> DEVUELVE UNA EXCEPCIÓN CON EL ERROR
     */
    public void getAllOrdersInARestaurant(String uid, OnRestaurantOrdersGotCallback callback){
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    DataSnapshot restaurantRef = dataSnapshot.child("restaurants").child(uid);

                    if (restaurantRef.exists()) {
                        DataSnapshot ordersRef = restaurantRef.child("orders/items");

                        ArrayList<OrderGetRestaurantOrdersEntity> orders = new ArrayList<>();

                        final int[] rounds = {0};
                        int totalOrders = (int)ordersRef.getChildrenCount();

                        if (totalOrders == 0) {
                            callback.onFindingSuccess(orders);
                            return;
                        }

                        for (DataSnapshot order : ordersRef.getChildren()) {

                            String clientUid = order.child("uidCustomer").getValue(String.class);

                            if(clientUid != null){

                                DatabaseReference clientRef = databaseReference.child("users").child(clientUid);

                                clientRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot clientSnapshot) {
                                        if(clientSnapshot.exists()){

                                            OrderGetRestaurantOrdersEntity orderEntity = new OrderGetRestaurantOrdersEntity();

                                            String clientName = clientSnapshot.child("name").getValue(String.class);
                                            String clientSurname = clientSnapshot.child("surname").getValue(String.class);
                                            String fullClientName = clientName + " " + clientSurname;
                                            Integer date = order.child("date").getValue(Integer.class);
                                            String numOrder = order.child("num_order").getValue(String.class);
                                            Float total = order.child("total").getValue(Float.class);
                                            int numMenus = (int)order.child("menus").getChildrenCount();
                                            String status = "complete";

                                            DataSnapshot menusSnapshot = order.child("menus");

                                            if(menusSnapshot.exists()){

                                                for(DataSnapshot menu : menusSnapshot.getChildren()){
                                                    if(!menu.child("status").getValue(String.class).equals("complete")){
                                                        status = "in_progress";

                                                        break;
                                                    }
                                                }
                                            }

                                            orderEntity.setName_client(fullClientName);
                                            orderEntity.setDate(date);
                                            orderEntity.setNum_order(numOrder);
                                            orderEntity.setTotal(total);
                                            orderEntity.setNum_menus(numMenus);
                                            orderEntity.setStatus(status);

                                            orders.add(orderEntity);
                                        }

                                        rounds[0]++;

                                        if(rounds[0] == totalOrders){
                                            callback.onFindingSuccess(orders); // LLAMAMOS CUANDO HEMOS RECIBIDO TODO
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        callback.onFindingFailure(databaseError.toException());
                                    }
                                });
                            } else {
                                rounds[0]++;
                                if(rounds[0] == totalOrders){
                                    callback.onFindingSuccess(orders);
                                }
                            }
                        }
                    }
                } else {
                    callback.onFindingSuccess(new ArrayList<>());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFindingFailure(databaseError.toException());
            }
        });
    }

    // ----------------------------------------------------------------------------------------------------------------

    public interface OnRestaurantOrdersGotCallback{
        void onFindingSuccess(ArrayList<OrderGetRestaurantOrdersEntity> ordersEntity);
        void onFindingFailure(Exception exception);
    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     *  OBTIENE TODOS LOS PEDIDOS QUE UN RESTAURANTE TIENE QUE TRAMITAR EN EL PERIODO DADO
     *
     * @param request OBJETO TIPO OrderGetTasksRequest QUE CONTIENE:
     *                uid: Nº DE USUARIO DEL RESTAURANTE
     *                start_date: ESPECIFICA EL "DESDE" (FECHA DE INICIO)
     *                end_date: ESPECIFICA EL "HASTA DONDE" (FECHA FINAL)
     * @param callback SI EXITO: DEVUELVE UN ARRAYLIST CON LOS PEDIDOS EN FORMATO OrderGetTasksEntity
     *                 SI FALLO: DEVUELVE UNA EXCEPCIÓN
     */
    public void getDaylyTask (OrderGetTasksRequest request, OnDaylyTaskFindingCallback callback){

        int start_date = request.getStart_date();
        int end_date = request.getEnd_date();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try{
                    DataSnapshot restSnap = dataSnapshot.child("restaurants").child(request.getUid());
                    DataSnapshot ordersSnap = restSnap.child("orders/items");

                    if(ordersSnap.exists()){

                        ArrayList<OrderGetTasksEntity> allTaskEntity = new ArrayList<>();

                        for(DataSnapshot order : ordersSnap.getChildren()){
                            DataSnapshot menusInOrderRef = order.child("menus");

                            for(DataSnapshot menu : menusInOrderRef.getChildren()){

                                int menuId = menu.child("id").getValue(Integer.class); // ID DEL MENÚ EN EL PEDIDO
                                int menuDate = restSnap.child("menus/items").child(String.valueOf(menuId)).child("date").getValue(Integer.class);

                                if( menuDate >= start_date && menuDate <= end_date){
                                    System.out.println(menuDate >= start_date && menuDate <= end_date);
                                    // 1º --- OBTENEMOS EL NÚMERO DE PEDIDO
                                    String num_order = order.child("num_order").getValue(String.class);

                                    // 2º --- CREAMOS EL OBJETO MENÚ
                                    int firstNum = menu.child("first_course").getValue(Integer.class);
                                    String firstName = restSnap.child("dishes/items").child(String.valueOf(firstNum)).child("name").getValue(String.class);
                                    String ingsFirst = restSnap.child("dishes/items").child(String.valueOf(firstNum)).child("ingredients").getValue(String.class);
                                    String imgFirst = restSnap.child("dishes/items").child(String.valueOf(firstNum)).child("image").getValue(String.class);
                                    ArrayList<String> allAllergensFirst = new ArrayList<>();
                                    for(DataSnapshot allergen : restSnap.child("dishes/items").child(String.valueOf(firstNum)).child("allergens").getChildren()){
                                        allAllergensFirst.add(allergen.getValue(String.class));
                                    }

                                    DishGetDayTaskEntity first_course = new DishGetDayTaskEntity(firstNum, firstName, ingsFirst, allAllergensFirst, imgFirst);


                                    int secondNum = menu.child("second_course").getValue(Integer.class);
                                    String secondName = restSnap.child("dishes/items").child(String.valueOf(secondNum)).child("name").getValue(String.class);
                                    String ingsSecond = restSnap.child("dishes/items").child(String.valueOf(secondNum)).child("ingredients").getValue(String.class);
                                    String imgSecond = restSnap.child("dishes/items").child(String.valueOf(secondNum)).child("image").getValue(String.class);
                                    ArrayList<String> allergensSecond = new ArrayList<>();
                                    for(DataSnapshot allergen : restSnap.child("dishes/items").child(String.valueOf(secondNum)).child("allergens").getChildren()){
                                        allergensSecond.add(allergen.getValue(String.class));
                                    }

                                    DishGetDayTaskEntity second_course = new DishGetDayTaskEntity(secondNum, secondName, ingsSecond, allergensSecond, imgSecond);


                                    int dessertNum = menu.child("dessert").getValue(Integer.class);
                                    String dessertName = restSnap.child("dishes/items").child(String.valueOf(dessertNum)).child("name").getValue(String.class);
                                    String ingsDessert = restSnap.child("dishes/items").child(String.valueOf(dessertNum)).child("ingredients").getValue(String.class);
                                    String imgDessert = restSnap.child("dishes/items").child(String.valueOf(dessertNum)).child("image").getValue(String.class);
                                    ArrayList<String> allergensDessert = new ArrayList<>();
                                    for(DataSnapshot allergen : restSnap.child("dishes/items").child(String.valueOf(dessertNum)).child("allergens").getChildren()){
                                        allergensDessert.add(allergen.getValue(String.class));
                                    }

                                    DishGetDayTaskEntity dessert = new DishGetDayTaskEntity(dessertNum, dessertName, ingsDessert, allergensDessert, imgDessert);

                                    String status = menu.child("status").getValue(String.class);

                                    MenuInGetTaskEntity menuEntity = new MenuInGetTaskEntity(menuId, menuDate, first_course, second_course, dessert, status);

                                    // 3º --- OBTENEMOS EL CLIENTE
                                    String uidCustomer = order.child("uidCustomer").getValue(String.class);
                                    DataSnapshot userSnap = dataSnapshot.child("users").child(uidCustomer);

                                    String clientName = userSnap.child("name").getValue(String.class);
                                    String clientSurname = userSnap.child("surname").getValue(String.class);
                                    String clientPhone = userSnap.child("phone").getValue(String.class);
                                    String clientEmail = userSnap.child("email").getValue(String.class);
                                    ArrayList<String> clientAllergens = new ArrayList<>();
                                    for(DataSnapshot al : userSnap.child("allergens").getChildren()){
                                        clientAllergens.add(al.getValue(String.class));
                                    }

                                    UserInGetTaskEntity userEntity = new UserInGetTaskEntity(clientName, clientSurname, clientPhone, clientEmail, clientAllergens);

                                    // 4º --- CREAMOS EL OBJETO COMPUESTO POR LOS TRES
                                    OrderGetTasksEntity taskEntity = new OrderGetTasksEntity(num_order, menuEntity, userEntity);
                                    allTaskEntity.add(taskEntity);
                                }
                            }
                        }
                        callback.onFindingSuccess(allTaskEntity);
                    }
                    else{
                        callback.onFindingFailure(new Exception("No existen tareas para este restaurante "));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    callback.onFindingFailure(e);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                    callback.onFindingFailure(databaseError.toException());
            }
        });
    }
    // ----------------------------------------------------------------------------------------------------------------

    public interface OnDaylyTaskFindingCallback{
        void onFindingSuccess(ArrayList<OrderGetTasksEntity> tasksEntity);
        void onFindingFailure(Exception exception);
    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     *  ACTUALIZA LOS DATOS DE UN PEDIDO
     *
     * @param request ES UN OBJETO DE TIPO OrderUpdateStatusRequest CON:
     *                uid: Nº DE USUARIO DEL RESTAURANTE
     *                num_order: Nº DEL PEDIDO EN ESE RESTAURANTE
     *                id_menu: Nº DEL MENÚ EN ESE RESTAURANTE
     *                status: SITUACIÓN DEL PEDIDO
     *
     * @param callback DEVUELVE UN true SI SALE BIEN O UNA EXCEPCIÓN SI SALE MAL
     */
    public void updateMenu (OrderUpdateStatusRequest request, OnStatusFindingCallback callback){

        DatabaseReference ordersRef = databaseReference.child("restaurants").child(request.getUid()).child("orders/items");
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    int contador = 0;
                    int ordersQuantity = (int)dataSnapshot.getChildrenCount();

                    for(DataSnapshot order : dataSnapshot.getChildren()){
                        if(order.child("num_order").getValue(String.class).equals(request.getNum_order())){
                            String key = order.getKey();

                            int contMenus = 0;
                            int menusQuantity = (int)dataSnapshot.child(key).child("menus").getChildrenCount();

                            for(DataSnapshot menu : dataSnapshot.child(key).child("menus").getChildren()){

                                if(menu.child("id").getValue(Integer.class) == request.getId_menu()){
                                    DatabaseReference statusRef = ordersRef.child(key)
                                            .child("menus")
                                            .child(menu.getKey())
                                            .child("status");

                                    statusRef.setValue(request.getStatus(), (databaseError, databaseReference1) -> {
                                        if(databaseError == null){
                                            callback.onFindingSuccess(true);
                                        }
                                        else{
                                            callback.onFindingFailure(new Exception("Error al actualizar el estado"));
                                        }
                                    });
                                }
                                else{
                                    contMenus++;
                                }
                            }
                            if(contMenus == menusQuantity){
                                callback.onFindingFailure(new Exception("No existe el número de menú introducido"));
                            }
                        }
                        else{
                            contador++;
                        }
                    }
                    if(contador == ordersQuantity){
                        callback.onFindingFailure(new Exception("No existe el número de pedido proporcionado"));
                    }
                }
                else{
                    callback.onFindingFailure(new Exception("El  restaurante no tiene pedidos."));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFindingFailure(new Exception("Error al acceder a la base de datos"));
            }
        });
    }

    // ----------------------------------------------------------------------------------------------------------------

    public interface OnStatusFindingCallback{
        void onFindingSuccess(Boolean result);
        void onFindingFailure(Exception exception);
    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     *  AÑADE UNA REVIEW A UN PEDIDO
     *
     * @param numOrder NÚMERO DEL PEDIDO EN EL RESTAURANTE QUE ES USADO PARA RECORRER LA BBDD HASTA ENCONTRARLO
     * @param review OPINIÓN DEL CLIENTE SOBRE EL PEDIDO
     * @param rate NOTA DEL CLIENTE SOBRE EL PEDIDO
     * @param callback DESPUES DE ACCEDER A LA BBDD DE NUEVO:
     *                 SI PUEDE HACERLO DEVUELVE  true
     *                 SI DA ERROR DEVUELVE false
     */
    public void addReviewToOrder(String numOrder, String review, int rate, OnReviewAddedCallback callback) {
        DatabaseReference restaurantsRef = databaseReference.child("restaurants");

        restaurantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean orderFound = false;

                if (dataSnapshot.exists()) {
                    for (DataSnapshot restaurantSnapshot : dataSnapshot.getChildren()) {
                        DataSnapshot ordersSnapshot = restaurantSnapshot.child("orders").child("items");

                        if (ordersSnapshot.exists()) {
                            for (DataSnapshot orderSnapshot : ordersSnapshot.getChildren()) {
                                String currentNumOrder = orderSnapshot.child("num_order").getValue(String.class);

                                // Verificamos si coincide el num_order
                                if (currentNumOrder != null && currentNumOrder.equals(numOrder)) {
                                    orderFound = true;


                                    Map<String, Object> reviewsMap = new HashMap<>();
                                    reviewsMap.put("rate", rate);
                                    reviewsMap.put("review", review);


                                    orderSnapshot.getRef().child("reviews").updateChildren(reviewsMap, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            if (databaseError == null) {
                                                callback.onResult(true);
                                            } else {
                                                callback.onResult(false);
                                            }
                                        }
                                    });
                                    break;
                                }
                            }
                        }

                        if (orderFound) break;
                    }
                }

                if (!orderFound) {
                    callback.onResult(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onResult(false);
            }
        });
    }



    public interface OnReviewAddedCallback {
        void onResult(boolean success);
    }

}
