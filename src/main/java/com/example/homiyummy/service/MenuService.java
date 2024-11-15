package com.example.homiyummy.service;

import com.example.homiyummy.repository.MenuRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MenuService {
    
    @Autowired
    private MenuRepository menuRepository;
    private DatabaseReference bbddRef;
    
    public MenuService(FirebaseApp firebaseApp) {
        bbddRef = FirebaseDatabase.getInstance().getReference();
    }
    
    
}
