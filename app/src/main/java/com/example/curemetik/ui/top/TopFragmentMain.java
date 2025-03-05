package com.example.curemetik.ui.top;

import android.os.Bundle;
import android.widget.Button;
import com.example.curemetik.ui.top.TopFragment;
import androidx.appcompat.app.AppCompatActivity;
import com.example.curemetik.R;

public class TopFragmentMain extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_top_main);

        Button btnMakeup = findViewById(R.id.btnMakeup);
        Button btnFace = findViewById(R.id.btnFace);
        Button btnEyes = findViewById(R.id.btnEyes);
        Button btnLips = findViewById(R.id.btnLips);
        Button btnEyebrows = findViewById(R.id.btnEyebrows);
        Button btnCare = findViewById(R.id.btnCare);
        Button btnHair = findViewById(R.id.btnHair);

        btnMakeup.setOnClickListener(v -> openTopFragment("Макияж"));
        btnFace.setOnClickListener(v -> openTopFragment("Лицо"));
        btnEyes.setOnClickListener(v -> openTopFragment("Глаза"));
        btnLips.setOnClickListener(v -> openTopFragment("Губы"));
        btnEyebrows.setOnClickListener(v -> openTopFragment("Брови"));
        btnCare.setOnClickListener(v -> openTopFragment("Уход"));
        btnHair.setOnClickListener(v -> openTopFragment("Волосы"));
    }

    private void openTopFragment(String category) {
        TopFragment topFragment = new TopFragment();
        Bundle args = new Bundle();
        args.putString("category", category);
        topFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, topFragment)
                .addToBackStack(null)
                .commit();
    }
}
