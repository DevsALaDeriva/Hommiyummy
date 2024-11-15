package com.example.homiyummy.repository;

import com.example.homiyummy.service.AuthService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.springframework.stereotype.Repository;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Repository
public class AuthRepository {

    private final FirebaseAuth firebaseAuth;

    public AuthRepository( FirebaseAuth firebaseAuth){
        this.firebaseAuth = firebaseAuth;
    }




    public CompletableFuture<String> getUidFromEmail(String email) {
        CompletableFuture<String> futureId = new CompletableFuture<>();
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
            //System.out.println("UID: " + userRecord.getUid());
            futureId.complete(userRecord.getUid()); // COMPLETA EL FUTURO CON EL UID DEL USUARIO SI LO ENCUENTRA
        } catch (FirebaseAuthException e) {
            futureId.complete("");      // SI NO LO ENCUENTRA LANZA UNA EXCEPCION
            //futureId.completeExceptionally(e);      // SI NO LO ENCUENTRA LANZA UNA EXCEPCION
        }
        return futureId;
    }


}
