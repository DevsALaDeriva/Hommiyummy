package com.example.homiyummy.service;

import com.example.homiyummy.model.user.UserDTO;
import com.example.homiyummy.model.user.UserEntity;
import com.example.homiyummy.model.user.UserReadResponse;
import com.example.homiyummy.model.user.UserResponse;
import com.example.homiyummy.repository.UserRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference bbddRef;

    // CONSTRUCTOR PARA INYECTAR FIREBASEAPP
    public UserService(FirebaseAuth firebaseAuth, FirebaseApp firebaseApp){
        bbddRef = FirebaseDatabase.getInstance().getReference(); // INYECTA LA REFERENCIA DE LA BBDD
        this.firebaseAuth = firebaseAuth;
    }

    // ------------------------------------------------------------------------------------------------------------
    public UserResponse createUser(UserDTO userDTO) {
        UserEntity userEntity = new UserEntity(); // USAMOS EL CONSTRUCTOR SIN PASSWORD DE UserEntity. LA CONTRASEÑA NO LA GUARDAMOS EN REALTIME
        userEntity.setUid(userDTO.getUid());
        userEntity.setName(userDTO.getName());
        userEntity.setAddress(userDTO.getAddress());
        userEntity.setCity(userDTO.getCity());
        userEntity.setSurname(userDTO.getSurname());
        userEntity.setEmail(userDTO.getEmail());
        userEntity.setPhone(userDTO.getPhone());
        userEntity.setAllergens(userDTO.getAllergens());

        System.out.println("Allergens: " + userDTO.getAllergens());

        /**
         * COMO EL saveUser DEL REPOSITORIO DEVUELVE EL UserResponse QUE QUEREMOS RECIBIR (PARA LUEGO MANDÁRSELO DESDE AQUÍ AL CONTROLLER)
         * USANDO UN CALLBACK,
         * 1º CREAMOS UN "Futuro" DONDE LO ALMACENAREMOS Y
         * 2º LE TENEMOS QUE PASAR ESE CALLBACK QUE COMPLETE EL FUTURO
         */

        CompletableFuture<UserResponse> futureResponse = new CompletableFuture<>();

        userRepository.saveUser(userEntity, new UserRepository.SaveUserCallback() {
            @Override
            public void onSuccess(UserResponse userResponse) {
                futureResponse.complete(userResponse); // Completa el futuro con éxito
            }
            @Override
            public void onFailure(Exception exception) {
                futureResponse.completeExceptionally(exception); // Completa el futuro con excepción
            }
        });

        // Bloquea y espera el resultado de futureResponse antes de retornar
        try {
            return futureResponse.get(); // Retorna el UserResponse cuando esté disponible
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar el usuario en Firebase", e);
        }

    }

// ------------------------------------------------------------------------------------------------------------

    public Boolean updateUser(UserDTO userDTO)  {

        CompletableFuture<Boolean> futureBoolean = new CompletableFuture<>();

        UserEntity userEntity = new UserEntity();
        userEntity.setUid(userDTO.getUid());
        userEntity.setName(userDTO.getName());
        userEntity.setSurname(userDTO.getSurname());
        userEntity.setPhone(userDTO.getPhone());
        userEntity.setAllergens(userDTO.getAllergens());

        userRepository.updateUserData(userEntity, new UserRepository.GetUpdateConfirmationCallback() { // PASAMOS UNA IMPLEMENTACIÓN ANÓNIMA DE LA INTERFAZ AL REPOSITORIO
            @Override
            public void onSuccess(Boolean confirmation) {
                futureBoolean.complete(confirmation);
            }
            @Override
            public void onFailure(Exception exception) {
                futureBoolean.completeExceptionally(exception);
            }
        });
        try {
            return futureBoolean.get();
        }
        catch (Exception e){
            throw new RuntimeException("Error al obtener confirmación de la actualización del usuario", e);
        }
    }

// ----------------------------------------------------------------------------------------------------------------

    // LE LLAMARÁ getUserTypeByUid DESDE UserTypeService. SERVICIO CREADO SOLO PARA IDENTIFICAR SI UN USUARIO ES user o restaurant.
    // PQ HAY QUE HACER UNA CONSULTA SIN SABER DE ENTRADA A CUÁL DE LOS DOS SERVICIOS SE LO MANDAMOS.
    public CompletableFuture<Boolean> existsByUid(String uid) {
        return userRepository.existsByUid(uid); // SE LO PASAMOS AL REPOSITORIO
    }

// ----------------------------------------------------------------------------------------------------------------

    public UserReadResponse findUserByUid(String uid){

        CompletableFuture<UserReadResponse> futureUser = new CompletableFuture<>();

        userRepository.find(uid, new UserRepository.FindUserCallback() {
            @Override
            public void onSuccess(UserReadResponse userReadResponse) {
                futureUser.complete(userReadResponse);
            }
            @Override
            public void onFailure(UserReadResponse userReadResponse) {
                futureUser.complete(userReadResponse);
            }
        });
        try {
            return futureUser.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

// ----------------------------------------------------------------------------------------------------------------

//    private UserResponse convertToUserResponse(UserEntity userEntity, String userId) {
//        UserResponse userResponse = new UserResponse();
//        userResponse.setUid(userId); // Establecer el userId en UserResponse
//        userResponse.setName(userEntity.getName());
//        userResponse.setEmail(userEntity.getEmail());
//        // Otros campos necesarios en UserResponse
//        return userResponse;
//    }

// ----------------------------------------------------------------------------------------------------------------



}
