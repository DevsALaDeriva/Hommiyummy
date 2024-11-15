package com.example.homiyummy.repository;

import com.example.homiyummy.model.user.UserEntity;
import com.example.homiyummy.model.user.UserReadResponse;
import com.example.homiyummy.model.user.UserResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class UserRepository {

    @Autowired
    private FirebaseDatabase firebaseDatabase;
    @Autowired
    private FirebaseAuth firebaseAuth;

    DatabaseReference databaseReference;

    public UserRepository( DatabaseReference databaseReference){
        this.databaseReference = databaseReference;
    }


    // ------------------------------------------------------------------------------------------------------------

    // QUIEN LLAME A ESTE MÉTODO ESPERARÁ RECIBIR LA RESPUESTA (el UserResponse) VÍA CALLBACK
    public void saveUser(UserEntity userEntity, SaveUserCallback callback)  {

        // GUARDO EN BASE DE DATOS AL USUARIO
        // USAMOS EL ID QUE TRAE UserEntity PARA CREAR EL NODO DEL USUARIO;
        DatabaseReference userRef = firebaseDatabase.getReference("users").child(userEntity.getUid());

        // CREO MAPA PARA GUARDAR EL UserEntity SIN INCLUIR ID
        Map<String, Object> userEntityToSave = new HashMap<>();

        userEntityToSave.put("name", userEntity.getName());
        userEntityToSave.put("surname", userEntity.getSurname());
        userEntityToSave.put("email", userEntity.getEmail());
        userEntityToSave.put("phone", userEntity.getPhone());
        userEntityToSave.put("allergens", userEntity.getAllergens());
        userEntityToSave.put("city", userEntity.getCity());
        userEntityToSave.put("address", userEntity.getAddress());

        // 1º userRef.setValue GRABA LOS DATOS DE userEntityToSave EN REALTIME
        // 2º SE EJECUTA EL PRIMER CALLBACK  (PREDEFINIDO POR FIREBASE PARA MANEJAR UN RESULTADO EN OPERACIONES ASÍNCRONAS)
        userRef.setValue(userEntityToSave, (databaseError, databaseReference) -> {
            if (databaseError == null) {
                // CONFIRMADO QUE NO HAY ERROR, LEEMOS LOS DATOS RECIÉN GUARDADOS
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // CREAMOS UN OBJETO UserResponse CON ELLOS
                        UserResponse userResponse = dataSnapshot.getValue(UserResponse.class);
                        userResponse.setUid(userRef.getKey());   // LE AÑADO EL ID DE SU NODO
                        callback.onSuccess(userResponse);       // 3º Y DEVOLVEMOS ESE OBJETO COMO PARÁMETRO DEL SEGUNDO CALLBACK (DE NUESTRA INTERFACE) SI ES EXITOSO
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        callback.onFailure(databaseError.toException()); // 3º EJECUTAMOS EL CALLBACK DE NUESTRA INTERFACE SI DA ERROR
                    }
                });
            } else {
                callback.onFailure(databaseError.toException());
            }
        });
    }

// ------------------------------------------------------------------------------------------------------------

    public interface SaveUserCallback {
        void onSuccess(UserResponse userResponse);
        void onFailure(Exception exception);
    }
// ------------------------------------------------------------------------------------------------------------

    public void updateUserData(UserEntity userEntity, GetUpdateConfirmationCallback callback) { // IMPLEMENTA LA INTERFAZ QUE LE SERVIRÁ AL SERVICIO PARA OBTENER LA CONFIRMACIÓN DEL ÉXITO O FALLO DE LA ACTUALIZACIÓN
       System.out.println("uid: " + userEntity.getUid());
       DatabaseReference userRef = firebaseDatabase.getReference("users").child(userEntity.getUid());

       UserResponse userResponse = new UserResponse();

       //EL uid NO SE INCLUYE ENTRE LOS DATOS DEL USUARIO, VA A PARTE (EN EL NODO)

        userRef.addListenerForSingleValueEvent(new ValueEventListener() { // PRIMERO VER EL CONTENIDO GUARDADO EN REALTIME DEL USUARIO ANTES DE GUARDAR EL NUEVO
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        String currentName = dataSnapshot.child("name").getValue(String.class);
                        String currentSurname = dataSnapshot.child("surname").getValue(String.class);
                        String currentPhone = dataSnapshot.child("phone").getValue(String.class);
                        ArrayList<String> currentAllergens = (ArrayList<String>) dataSnapshot.child("allergens").getValue();
                        String currentEmail = dataSnapshot.child("email").getValue(String.class);
                        String currentCity = dataSnapshot.child("city").getValue(String.class);
                        String currentAddress = dataSnapshot.child("address").getValue(String.class);

                        UserEntity userEntityToBeSaved = new UserEntity(); // PODRÍA NO CREAR OTRO Y GRABARLO SOBRE EL MISMO QUE LLEGA, PERO LO HAGO ASÍ

                        userEntityToBeSaved.setName(userEntity.getName() != null && !userEntity.getName().isEmpty() ? userEntity.getName() : currentName);
                        userEntityToBeSaved.setSurname(userEntity.getSurname() != null && !userEntity.getSurname().isEmpty() ? userEntity.getSurname() : currentSurname);
                        userEntityToBeSaved.setPhone(userEntity.getPhone() != null && !userEntity.getPhone().isEmpty() ? userEntity.getPhone() : currentPhone);
                        userEntityToBeSaved.setAllergens(userEntity.getAllergens() != null  ? userEntity.getAllergens() : currentAllergens);
                        // LOS VALORES PARA EMAIL, CITY Y ADDRESS SE MANTIENEN LOS QUE HABÍA GUARDADOS
                        userEntityToBeSaved.setEmail(currentEmail);
                        userEntityToBeSaved.setCity(currentCity);
                        userEntityToBeSaved.setAddress(currentAddress);

                        // AHORA GUARDAMOS EL OBJETO ENTERO RECIÉN CONFIGURADO Y LLENADO EN REALTIME
                        userRef.setValue(userEntityToBeSaved, ((databaseError, databaseReference) -> {
                            if(databaseError == null){                                            // SI NO HAY ERROR
                                userRef.addListenerForSingleValueEvent(new ValueEventListener() { // VUELVO A ENTRAR EN EL NODO PARA VER SI SE HA GRABADO
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        UserResponse ur = dataSnapshot.getValue(UserResponse.class);                           // SOLO LO CREO PARA VER SI TIENE COSAS
                                        if(!ur.getName().isEmpty() && !ur.getSurname().isEmpty() && !ur.getPhone().isEmpty()){ // SI HAY DATOS GRABADOS DEVUELVE TRUE
                                            callback.onSuccess(true);
                                        }
                                        else{
                                            callback.onSuccess(false);
                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        callback.onFailure(databaseError.toException());
                                    }
                                });
                            }
                            else {
                                callback.onFailure(databaseError.toException());
                            }
                        }));
                    }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

// -----------------------------------------------------------------------------------------------------------------------------

    public interface GetUpdateConfirmationCallback {
        void onSuccess(Boolean confirmation);
        void onFailure(Exception exception);
    }

// -----------------------------------------------------------------------------------------------------------------------------

    // DICE SI EL USUARIO EXISTE EN EL NODO "users" POR LO QUE DE EXISTIR SERÍA UN "user"
    public CompletableFuture<Boolean> existsByUid(String uid) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        DatabaseReference userRef = firebaseDatabase.getReference("users").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // Si el snapshot existe, el usuario existe
                future.complete(snapshot.exists());
            }
            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });

        return future;
    }

// -----------------------------------------------------------------------------------------------------------------------------

    public void find(String uid, FindUserCallback callback){
        DatabaseReference userRef = databaseReference.child("users").child(uid);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        UserReadResponse user = dataSnapshot.getValue(UserReadResponse.class);
                        if(!user.getName().isEmpty() && !user.getEmail().isEmpty()){
                            callback.onSuccess(user);
                        }
                        else{
                            UserReadResponse emptyUser = new UserReadResponse();
                            callback.onFailure(emptyUser);
                        }
                    }
                    else{
                        UserReadResponse emptyUser = new UserReadResponse();
                        callback.onFailure(emptyUser);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) { // FALTA MANEJAR EL ERROR
                    UserReadResponse emptyUser = new UserReadResponse();
                    callback.onFailure(emptyUser);
                }
            });
    }


    public interface FindUserCallback{
        void onSuccess(UserReadResponse userReadResponse);
        void onFailure(UserReadResponse userReadResponse);
    }











































//
//    public static UserResponse getUser(UserEntity userEntity) {
//        // Crear un nuevo UserResponse y mapear los datos de UserEntity
//        UserResponse userResponse = new UserResponse();
//        userResponse.setUid(userEntity.getUid());
//        userResponse.setName(userEntity.getName());
//        userResponse.setSurname(userResponse.getSurname());
//        userResponse.setEmail(userEntity.getEmail());
//        userResponse.setPhone(userEntity.getPhone());
//
//        // Si UserResponse tiene campos adicionales que no estén en UserEntity, inicialízalos según sea necesario.
//        //userResponse.setStatus("ACTIVE"); // Ejemplo de campo adicional
//
//        return userResponse;
//    }
//
//
//
//
//    // Método para convertir UserEntity en un Map
//    private Map<String, Object> convertToMap(UserEntity userEntity) {
//        Map<String, Object> userMap = new HashMap<>();
//        userMap.put("name", userEntity.getName());
//        userMap.put("email", userEntity.getEmail());
//        // Agrega aquí los demás campos necesarios de UserEntity
//        return userMap;
//    }
//
//
//
//
//
//    public CompletableFuture<UserEntity> getUserById(String userId) {
//        CompletableFuture<UserEntity> future = new CompletableFuture<>();
//
//        DatabaseReference userRef = firebaseDatabase.getReference("users").child(userId);
//        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                UserEntity userEntity = snapshot.getValue(UserEntity.class);
//                future.complete(userEntity);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                future.completeExceptionally(error.toException());
//            }
//        });
//
//        return future;
//    }
//
//
//
//    public CompletableFuture<Void> deleteUser(String userId) {
//        CompletableFuture<Void> future = new CompletableFuture<>();
//
//        DatabaseReference userRef = firebaseDatabase.getReference("users").child(userId);
//        userRef.removeValue((error, ref) -> {
//            if (error == null) {
//                future.complete(null); // Eliminación exitosa
//            } else {
//                System.out.println("Error al eliminar en Firebase: " + error.getMessage());
//                future.completeExceptionally(error.toException());
//            }
//        });
//
//        return future;
//    }




}
