package com.example.curemetik.ui.top;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.curemetik.databinding.FragmentTopBinding;

public class TopFragment extends Fragment {

    private FragmentTopBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        TopViewModel topViewModel =
                new ViewModelProvider(this).get(TopViewModel.class);

        binding = FragmentTopBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textTop;
        topViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}