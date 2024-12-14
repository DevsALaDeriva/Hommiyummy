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


    public CompletableFuture<String> getUidFromEmail(String email) {
        CompletableFuture<String> futureId = new CompletableFuture<>();
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
            futureId.complete(userRecord.getUid());
        } catch (FirebaseAuthException e) {
            futureId.completeExceptionally(e);
        }
        return futureId;
    }
}
