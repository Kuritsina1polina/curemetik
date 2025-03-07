package com.example.curemetik.ui.top;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.curemetik.R;

public class TopFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_top, container, false);

        Button btnMakeup = root.findViewById(R.id.btnMakeup);
        Button btnFace = root.findViewById(R.id.btnFace);
        Button btnEyes = root.findViewById(R.id.btnEyes);
        Button btnLips = root.findViewById(R.id.btnLips);
        Button btnEyebrows = root.findViewById(R.id.btnEyebrows);
        Button btnCare = root.findViewById(R.id.btnCare);
        Button btnHair = root.findViewById(R.id.btnHair);

        btnMakeup.setOnClickListener(v -> navigateToTopFragmentDetails("Макияж"));
        btnFace.setOnClickListener(v -> navigateToTopFragmentDetails("Лицо"));
        btnEyes.setOnClickListener(v -> navigateToTopFragmentDetails("Глаза"));
        btnLips.setOnClickListener(v -> navigateToTopFragmentDetails("Губы"));
        btnEyebrows.setOnClickListener(v -> navigateToTopFragmentDetails("Брови"));
        btnCare.setOnClickListener(v -> navigateToTopFragmentDetails("Уход"));
        btnHair.setOnClickListener(v -> navigateToTopFragmentDetails("Волосы"));

        return root;
    }

    private void navigateToTopFragmentDetails(String category) {
        Bundle args = new Bundle();
        args.putString("category", category);
        Navigation.findNavController(requireView()).navigate(R.id.action_to_topFragmentDetails, args);
    }
}
