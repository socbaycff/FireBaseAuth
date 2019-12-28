package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    public static int RC_LOGIN = 1;
    boolean flag; // flag danh dau khi nao co data tren database: true la k ton tai data, false la ton tai
    FirebaseFirestore db; // tham chieu root
    FirebaseAuth firebaseAuth; // doi tuong xac thuc user
    FirebaseAuth.AuthStateListener listener; // su kien xac dinh nguoi dung

    EditText tenTV;
    EditText idTV;
    Button saveBT;

    DocumentReference ref; // tham chieu database
    ListenerRegistration listenerRegistration; // thay chieu dang ky data listener
    EventListener eventListener; // listener data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // lay tham chieu view
        tenTV = findViewById(R.id.ten);
        idTV = findViewById(R.id.id);
        saveBT = findViewById(R.id.save);

        db = FirebaseFirestore.getInstance(); // tao tham chieu quan ly database
        firebaseAuth = FirebaseAuth.getInstance(); // tham chieu quan ly Xac thuc


        // tao listener su kien login,logout
        listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) { // neu da dang nhap thi user khac null
                    attachDataListener(currentUser.getUid()); // lay du lieu bang eventListener

                } else { // chua dang nhap thi chuyen sang activity dang nhap

                    // tao lien ket may chu facebook
                    AuthUI.IdpConfig facebookIdp = new AuthUI.IdpConfig.FacebookBuilder().build();

                    // tao lien ket google account
                    AuthUI.IdpConfig ggIdp = new AuthUI.IdpConfig.GoogleBuilder().build();

                    // lien ket email
                    AuthUI.IdpConfig emailIdp = new AuthUI.IdpConfig.EmailBuilder().build();

                    // chuyen sang activity dang nhap
                    startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder() // tao builder intent cho cua so dang nhap
                            .setAvailableProviders(Arrays.asList(facebookIdp, ggIdp, emailIdp))
                            .setIsSmartLockEnabled(true)
                            .setTheme(R.style.Trandan)
                            .build(), RC_LOGIN);
                }
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(listener); // add listener trang thai dang nhap
    }

    @Override
    protected void onPause() {
        super.onPause();
        firebaseAuth.removeAuthStateListener(listener); // xoa listener khi pause activity
    }

    public void attachDataListener(String userid) { // ham gan listener cho data khi tren database co san
        ref = db.collection("info").document(userid); // tham chieu vao database cua user dang nhap


        listenerRegistration = ref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) { // neu loi read data thi khong update UI
                    Toast.makeText(getApplicationContext(), "khong co data", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) { // neu ton tai va snapshot khac null
                    Map<String, Object> datas = documentSnapshot.getData();
                    tenTV.setText(datas.get("ten").toString());
                    idTV.setText(datas.get("id").toString());

                } else {

                    Toast.makeText(getApplicationContext(), "Khong co data", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


    public void signout(View view) {

        listenerRegistration.remove();

        AuthUI.getInstance().signOut(this); // dang xuat
        // xoa UI
        tenTV.setText("");
        idTV.setText("");

    }

    public void save(View view) {
        String id = idTV.getText().toString();
        String ten = tenTV.getText().toString();

        if (id != "" && ten != "") {
            // tao hashmap luu tru toan bo data
            HashMap<String, Object> datas = new HashMap<>();
            datas.put("id", id);
            datas.put("ten", ten);
            // set data tren database
            db.collection("info").document(firebaseAuth.getCurrentUser().getUid()).set(datas).addOnCompleteListener(new OnCompleteListener<Void>() { // them listener bao cao thanh cong
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "thanh cong", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "khong thanh cong", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }


}
