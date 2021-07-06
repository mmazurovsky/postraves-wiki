//package com.postraves.backend.postraveswiki.security;
//
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.FirebaseOptions;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PostConstruct;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//
//@Component
//@Slf4j
//public class FirebaseConfig {
//    static FileInputStream serviceAccount;
//
//    @PostConstruct
//    private void initFirebaseApp() {
//        try {
//            serviceAccount = new FileInputStream("src/main/resources/secret/postraves-firebase-adminsdk-2s69q-3648f1af4e.json");
//        } catch (FileNotFoundException e) {
//            log.info("Firebase file not found", e);
//        }
//
//        FirebaseOptions options = null;
//        try {
//            options = new FirebaseOptions.Builder()
//                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                    .build();
//        } catch (IOException e) {
//            log.debug("Can't set Firebase credentials", e);
//        }
//
//        assert options != null;
//        FirebaseApp.initializeApp(options);
//    }
//}
