package com.example.curemetik;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.curemetik.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;


public class MainActivity extends AppCompatActivity {
    Button buttonSignIn, buttonRegister;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;
    ConstraintLayout main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        buttonSignIn = findViewById(R.id.buttonSignIn);
        buttonRegister = findViewById(R.id.buttonRegistrer);

        main = findViewById(R.id.main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterWindow();
            }
        });
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignInWindow();
            }
        });
    }

    private void showSignInWindow() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Войти");
        dialog.setMessage("Введите данные для входа");

        LayoutInflater inflater = LayoutInflater.from(this);
        View sign_in_window = inflater.inflate(R.layout.register_window, null);
        dialog.setView(sign_in_window);

        MaterialEditText email = sign_in_window.findViewById(R.id.emailField);
        MaterialEditText password = sign_in_window.findViewById(R.id.passField);

        dialog.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        dialog.setPositiveButton("Войти", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if(TextUtils.isEmpty(email.getText().toString())){
                    Snackbar.make(main, "Введите email", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(password.getText().toString().length() < 5){
                    Snackbar.make(main, "Введите пароль", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                //auth
                auth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                startActivity(new Intent(MainActivity.this, Curemetik.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(main,"Ошибка авторизации" + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                            }
                        });

            }
        });

        dialog.show();
    }

    private void showRegisterWindow() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Зарегистрироваться");
        dialog.setMessage("Введите данные для регистрации");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_window = inflater.inflate(R.layout.register_window, null);
        dialog.setView(register_window);

        MaterialEditText email = register_window.findViewById(R.id.emailField);
        MaterialEditText password = register_window.findViewById(R.id.passField);

        dialog.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        dialog.setPositiveButton("Отправить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if(TextUtils.isEmpty(email.getText().toString())){
                    Snackbar.make(main, "Введите email", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(password.getText().toString().length() < 5){
                    Snackbar.make(main, "Введите пароль, длиной более 5 символов", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                //registration
                auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                User user = new User();
                                user.setEmail(email.getText().toString());
                                user.setPassword(password.getText().toString());

                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Snackbar.make(main, "Готово", Snackbar.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
            }
        });

        dialog.show();
    }
}