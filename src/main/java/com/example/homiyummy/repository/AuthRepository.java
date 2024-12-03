package com.example.homiyummy.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.springframework.stereotype.Repository;

import java.util.concurrent.CompletableFuture;


@Repository
public class AuthRepository {

    private final FirebaseAuth firebaseAuth;

    public AuthRepository( FirebaseAuth firebaseAuth){
        this.firebaseAuth = firebaseAuth;
    }

    // BORRAR LO COMENTADO SI FUNCIONA EL MÉTO DO DE ABAJO

//    public CompletableFuture<String> getUidFromEmail(String email) {
//        CompletableFuture<String> futureId = new CompletableFuture<>();
//        try {
//            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
//            //System.out.println("UID: " + userRecord.getUid());
//            futureId.complete(userRecord.getUid()); // COMPLETA EL FUTURO CON EL UID DEL USUARIO SI LO ENCUENTRA
//        } catch (FirebaseAuthException e) {
//            futureId.complete("");      // SI NO LO ENCUENTRA LANZA UNA EXCEPCION
//            //futureId.completeExceptionally(e);      // SI NO LO ENCUENTRA LANZA UNA EXCEPCION
//        }
//        return futureId;
//    }

    public CompletableFuture<String> getUidFromEmail(String email) {
        CompletableFuture<String> futureId = new CompletableFuture<>();
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
            futureId.complete(userRecord.getUid()); // COMPLETA EL FUTURO CON EL UID DEL USUARIO SI LO ENCUENTRA
        } catch (FirebaseAuthException e) {
            futureId.completeExceptionally(e);      // SI NO LO ENCUENTRA LANZA UNA EXCEPCION
        }
        return futureId;
    }
}
