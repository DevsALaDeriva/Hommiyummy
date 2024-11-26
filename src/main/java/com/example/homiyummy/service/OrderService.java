package com.example.homiyummy.service;

import com.example.homiyummy.model.order.OrderCreatedResponse;
import com.example.homiyummy.model.order.OrderDTO;
import com.example.homiyummy.model.order.OrderEntity;
import com.example.homiyummy.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository){
        this.orderRepository = orderRepository;
    }


    // ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<Integer> findLastId(String uid){

        CompletableFuture<Integer> futureId = new CompletableFuture<Integer>();

        orderRepository.findId(uid, new OrderRepository.OnOrderIdGot() {
            @Override
            public void onFindingSuccess(int actualId) {
                System.out.println("Id actual desde findLastId en service: " + actualId);
                futureId.complete(actualId);
            }

            @Override
            public void onFindingFailure(Exception exception) {
                futureId.completeExceptionally(exception);
            }
        });

        return futureId;
    }

    public CompletableFuture<OrderCreatedResponse> createOrder(OrderDTO orderDTO, int lastOrderId){
        System.out.println("-------------------------------> 1");

        int newOrderId = lastOrderId + 1;
        CompletableFuture<OrderCreatedResponse> futureOrder = new CompletableFuture<>();
        OrderEntity orderEntity = new OrderEntity();

        orderEntity.setUid(orderDTO.getUid());
        orderEntity.setNumOrder(orderDTO.getNumOrder());
        orderEntity.setDate(orderDTO.getDate());
        orderEntity.setCustomerUid(orderDTO.getCustomerUid());
        orderEntity.setMenus(orderDTO.getMenus());
        orderEntity.setTotal(orderDTO.getTotal());

        orderRepository.save(orderEntity, newOrderId, new OrderRepository.OnSavingOrderCallback() {
            @Override
            public void onSavingOrderSuccess(OrderCreatedResponse orderCreatedResponse) {
                System.out.println("-------------------------------> 2");
                futureOrder.complete(orderCreatedResponse);
            }

            @Override
            public void onSavingOrderFailure(Exception exception) {
                futureOrder.completeExceptionally(exception);
            }
        });
        System.out.println("-------------------------------> 3");
        return futureOrder;
    }
}
