package com.example.homiyummy.repository;

import com.example.homiyummy.model.course.CourseEntity;
import com.example.homiyummy.model.menu.MenuGetByNumEntity;
import com.example.homiyummy.model.menu.MenuInGetTaskEntity;
import com.example.homiyummy.model.order.*;
import com.example.homiyummy.model.user.UserInGetTaskEntity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class OrderRepository {

    DatabaseReference databaseReference;

    public OrderRepository(DatabaseReference databaseReference){
        this.databaseReference = databaseReference;
    }


    // ----------------------------------------------------------------------------------------------------------------
    // -> OK REVISITÓN DE CALLBACKS Y EXCEPCIONES

    /**
     * La 1ª vez que se compruebe si un Id existe, crea el nodo "counter",si no existe de antes.
     * Con dataSnapshot.exists() lo que realmente se comprueba es si este nodo contiene datos, no si existe.
     * Por eso, la primera vez devuelve false y salta al else, en el que asignamos 0.
     *
     * @param callback: usamos la versión exitosa cuando:
     *                              - encuentra un dato guardado en el nodo
     *                              - y cuando no lo encuentra (que asigna 0)
     *                  usamos la versión errónea cuando:
     *                              - se encuentra como dato un null
     *                              - o cuando directamente ha entrado en onCancelled, por un error de conexión, mala configuración del acceso...
     *
     *  @param uid el identificador único del restaurante
     */

    public void findId(String uid, OnOrderIdGot callback) {

        DatabaseReference restaurantRef = databaseReference.child("restaurants").child(uid);
        DatabaseReference ordersRef = restaurantRef.child("orders");
        DatabaseReference counterRef = ordersRef.child("counter");       //----> ESTO SOLO CREA LA UBICACIÓN

        counterRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){                                        //----> LA 1º VEZ QUE SE VAYA A USAR DARÁ FALSE PQ ESA UBICACIÓN RECIÉN CREADA ESTÁ VACÍA
                    Integer actualCounter = dataSnapshot.getValue(Integer.class);
                    if(actualCounter != null) {
                        callback.onFindingSuccess(actualCounter);
                    }
                    else{
                        callback.onFindingFailure(new Exception("El valor del contador en Realtime es null"));
                    }
                }
                else{
                    //System.out.println("Ha dado false pq, al ser la 1ª vez, el nodo se acaba de crear y no tiene datos");
                    callback.onFindingSuccess(0);                              //----> LA 1ª VEZ, SALTA AQUÍ Y LE ASIGNAMOS UN 0
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFindingFailure(databaseError.toException());
            }
        });
    }

    // ----------------------------------------------------------------------------------------------------------------
    // -> OK REVISITÓN DE CALLBACKS Y EXCEPCIONES

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

            counterRef.addListenerForSingleValueEvent(new ValueEventListener() {// NOS DIRIGIMOS A COUNTER, PQ QUIERO LEER EL VALOR QUE ACABO DE GRABAR EN COUNTER
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Integer orderId = dataSnapshot.getValue(Integer.class); // OBTENEMOS EL VALOR QUE ACABAMOS DE GUARDAR PARA ASEGURARNOS Q USAMOS EL CORRECTO
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

    public void getOrderDataInRestaurantSide(String orderNumber, OnOrderGotCallback callback){

        DatabaseReference allRestaurantsRef = databaseReference.child("restaurants");
        allRestaurantsRef.addListenerForSingleValueEvent(new ValueEventListener() {               // TODOS LOS RESTAURANTES
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot restNode : dataSnapshot.getChildren()){            // RECORREMOS TODOS LOS RESTAURANTES
                        DataSnapshot ordersRef = restNode.child("orders/items");   // ENTRAMOS EN LOS PEDIDOS DE CADA RESTAURANTE SI LOS TIENE

                        System.out.println(ordersRef.exists());

                        if(ordersRef.exists()){

                            for(DataSnapshot order : ordersRef.getChildren()){

                                String value = order.child("num_order").getValue(String.class);

                                if(order.child("num_order").getValue(String.class).equals(orderNumber)){
                                    String restUID = restNode.getKey();                             // GUARDAMOS EL UID DEL RESTAURANTE QUE TIENE EL PEDIDO
                                    String customerUID = order.child("uidCustomer").getValue(String.class); // GUARDAMOS EL UID DEL CLIENTE
                                    //String restName = restNode.child("name").getValue(String.class); // GUARDAMOS NOMBRE DEL RESTAURANTE
                                    int date = order.child("date").getValue(Integer.class); // GUARDAMOS FECHA DEL PEDIDO
                                    float totalOrder = order.child("total").getValue(Float.class); // GUARDAMOS TOTAL DEL PEDIDO

                                    DataSnapshot menusSnapshot = order.child("menus");

                                    if(menusSnapshot.exists()){

                                        ArrayList<MenuGetByNumEntity> menusInOrder = new ArrayList<>();

                                        for(DataSnapshot menu : menusSnapshot.getChildren()){

                                            int menuId = menu.child("id").getValue(Integer.class);
                                            System.out.println("menuId: " + menuId);
                                            int firsCourseID = menu.child("first_course").getValue(Integer.class);
                                            int secondCourseID = menu.child("second_course").getValue(Integer.class);
                                            int dessertID = menu.child("dessert").getValue(Integer.class);
                                            float totalMenu = menu.child("price").getValue(Float.class);
                                            String status = menu.child("status").getValue(String.class);
                                            int menuDate = restNode.child("menus/items").child(String.valueOf(menuId)).child("date").getValue(Integer.class);

                                            MenuGetByNumEntity singleMenu = new MenuGetByNumEntity(menuId, menuDate, firsCourseID, secondCourseID, dessertID, totalMenu, status); // CREAMOS CADA MENU

                                            menusInOrder.add(singleMenu); // Y LO METEMOS DENTRO DEL ARRAY DE MENÚS DEL PEDIDO
                                        }

                                        OrderGotByNumEntity dataEntity = new OrderGotByNumEntity(restUID, date, customerUID, menusInOrder, totalOrder);

                                        callback.onFindingSuccess(dataEntity);
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

    // ESTO OBTIENE TODOS LOS MENUS COMPRADOS DE UN CLIENTE

//    public void getClientOrders(String clientUID, OnClientOrdersGotCallback callback){
//
//        DatabaseReference allRestaurantsRef = databaseReference.child("restaurants");
//
//        allRestaurantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                //System.out.println("DataSnapshot: " + dataSnapshot.getValue());
//                if(dataSnapshot.exists()){
//
//                    ArrayList<OrderGetClientOrdersEntity> allOrdersEntity = new ArrayList<>(); // GUARDA TODOS LOS PEDIDOS DEL CLIENTE
//
//                    for(DataSnapshot restNode : dataSnapshot.getChildren()){
//
//                        DataSnapshot ordersRef = restNode.child("orders/items");
//
//                        if(ordersRef.exists()){
//                            for(DataSnapshot ord : ordersRef.getChildren()){
//                                //System.out.println("orden: " + ord.getKey());
//                                //System.out.println("orden: " + restNode.child("name").getValue(String.class));
//                                String clientSavedUid = ord.child("uidCustomer").getValue(String.class);
//                                //System.out.println("clienteUID en la orden: " + clientSavedUid);
//                                //System.out.println("clienteUID q llega: " + clientUID);
//                                if(clientSavedUid.equals(clientUID)){
//                                    //System.out.println("es igual");
//                                    DataSnapshot menusRef = ord.child("menus");
//
//                                    if (menusRef.exists()){
//
//                                        for(DataSnapshot menu : menusRef.getChildren()){
//
//                                            OrderGetClientOrdersEntity orderEntity = new OrderGetClientOrdersEntity();
//
//                                            String restName = restNode.child("name").getValue(String.class);
//                                            String restImage = restNode.child("image").getValue(String.class);
//                                            Integer date = ord.child("date").getValue(Integer.class);
//                                            String numOrder = ord.child("num_order").getValue(String.class);
//                                            Float total = ord.child("total").getValue(Float.class);
//                                            String status = menu.child("status").getValue(String.class);
//
//                                            //System.out.println("Pedido: " + numOrder + " - Status : " + status);
//
//                                            orderEntity.setName_restaurant(restName);
//                                            orderEntity.setImage_restaurant(restImage);
//                                            orderEntity.setDate(date);
//                                            orderEntity.setNum_order(numOrder);
//                                            orderEntity.setTotal(total);
//                                            orderEntity.setStatus(status);
//
//                                            allOrdersEntity.add(orderEntity);
//                                        }
//                                        callback.onFindingSuccess(allOrdersEntity);
//                                    }
//                                    callback.onFindingFailure(new Exception("No hay menús a nombre del cliente"));
//                                }
//                            }
//                        }
//                    }
//                    callback.onFindingFailure(new Exception("No hay pedidos en ningún restaurante"));
//                }
//                else{
//                    //System.err.println("No tiene hijos.");
//                    callback.onFindingFailure(new Exception("No existen restaurantes registrados."));
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                    callback.onFindingFailure(new Exception("Error de conexión."));
//            }
//        });
//    }

// ----------------------------------------------------------------------------------------------------------------
    public void getClientOrders(String clientUID, OnClientOrdersGotCallback callback){

        DatabaseReference allRestaurantsRef = databaseReference.child("restaurants");

        allRestaurantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    ArrayList<OrderGetClientOrdersEntity> allOrdersEntity = new ArrayList<>(); // GUARDA TODOS LOS PEDIDOS DEL CLIENTE

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
                    //System.err.println("No tiene hijos.");
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
                                            Integer date = order.child("date").getValue(Integer.class);
                                            String numOrder = order.child("num_order").getValue(String.class);
                                            Float total = order.child("total").getValue(Float.class);
                                            int numMenus = (int)order.child("menus").getChildrenCount();
                                            String status = "complete";

                                            DataSnapshot menusSnapshot = order.child("menus");
                                            //System.out.println(menusSnapshot.exists());

                                            if(menusSnapshot.exists()){

                                                for(DataSnapshot menu : menusSnapshot.getChildren()){
                                                    if(!menu.child("status").getValue(String.class).equals("complete")){
                                                        status = "in_progress";

                                                        break;
                                                    }
                                                }
                                            }

                                            orderEntity.setName_client(clientName);
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
                                if(rounds[0] == totalOrders){ // AUNQUE SEA NULO AUMENTAMOS EN 1
                                    callback.onFindingSuccess(orders);
                                }
                            }
                        }
                    }
                } else {
                    callback.onFindingSuccess(new ArrayList<>()); // PQ NO EXISTEN DATOS
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

                                    // 1º --- OBTENEMOS EL NÚMERO DE PEDIDO
                                    String num_order = order.child("num_order").getValue(String.class);

                                    // 2º --- CREAMOS EL OBJETO MENÚ
                                    int firstNum = menu.child("first_course").getValue(Integer.class);
                                    String firstName = restSnap.child("dishes/items").child(String.valueOf(firstNum)).child("name").getValue(String.class);
                                    String ingsFirst = restSnap.child("dishes/items").child(String.valueOf(firstNum)).child("ingredients").getValue(String.class);
                                    String imgFirst = restSnap.child("dishes/items").child(String.valueOf(firstNum)).child("image").getValue(String.class);
                                    String allAllergensFirst = "";
                                    for(DataSnapshot allergen : restSnap.child("dishes/items").child(String.valueOf(firstNum)).child("allergens").getChildren()){
                                        allAllergensFirst += allergen.getValue(String.class) + ", ";
                                    }
                                    if(allAllergensFirst.length() > 4){
                                        allAllergensFirst = allAllergensFirst.substring(0, allAllergensFirst.length() - 2);
                                    }
                                    CourseEntity first_course = new CourseEntity(firstName, ingsFirst, allAllergensFirst, imgFirst);


                                    int secondNum = menu.child("second_course").getValue(Integer.class);
                                    String secondName = restSnap.child("dishes/items").child(String.valueOf(secondNum)).child("name").getValue(String.class);
                                    String ingsSecond = restSnap.child("dishes/items").child(String.valueOf(secondNum)).child("ingredients").getValue(String.class);
                                    String imgSecond = restSnap.child("dishes/items").child(String.valueOf(secondNum)).child("image").getValue(String.class);
                                    String allergensSecond = "";
                                    for(DataSnapshot allergen : restSnap.child("dishes/items").child(String.valueOf(secondNum)).child("allergens").getChildren()){
                                        allergensSecond += allergen.getValue(String.class) + ", ";
                                    }
                                    if(allergensSecond.length() > 4) {
                                        allergensSecond = allergensSecond.substring(0, allergensSecond.length() - 2);
                                    }
                                    CourseEntity second_course = new CourseEntity(secondName, ingsSecond, allergensSecond, imgSecond);


                                    int dessertNum = menu.child("dessert").getValue(Integer.class);
                                    String dessertName = restSnap.child("dishes/items").child(String.valueOf(dessertNum)).child("name").getValue(String.class);
                                    String ingsDessert = restSnap.child("dishes/items").child(String.valueOf(dessertNum)).child("ingredients").getValue(String.class);
                                    String imgDessert = restSnap.child("dishes/items").child(String.valueOf(dessertNum)).child("image").getValue(String.class);
                                    String allergensDessert = "";
                                    for(DataSnapshot allergen : restSnap.child("dishes/items").child(String.valueOf(dessertNum)).child("allergens").getChildren()){
                                        allergensDessert += allergen.getValue(String.class) + ", ";
                                    }
                                    if(allergensDessert.length() > 4){
                                        allergensDessert = allergensDessert.substring(0, allergensDessert.length() - 2);
                                    }
                                    CourseEntity dessert = new CourseEntity(dessertName, ingsDessert, allergensDessert, imgDessert);


                                    MenuInGetTaskEntity menuEntity = new MenuInGetTaskEntity(menuId, menuDate, first_course, second_course, dessert);


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
                    //System.out.println("Se ha producido una excepción: " + e.getMessage());
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

}
