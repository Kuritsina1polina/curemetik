package com.example.curemetik.ui.top;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.curemetik.R;
import com.example.curemetik.databinding.FragmentTopBinding;
import com.example.curemetik.models.Product;
import com.google.firebase.firestore.Query;
import com.example.curemetik.ui.camera.ProductDetailsFragment;
import com.example.curemetik.ui.top.ProductAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class TopFragment extends Fragment {

    private FragmentTopBinding binding;
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private FirebaseFirestore db;
    private String category;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTopBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);

        db = FirebaseFirestore.getInstance();

        // Получаем категорию из аргументов
        if (getArguments() != null) {
            category = getArguments().getString("category");
        }

        // Загрузка топ-продуктов для выбранной категории
        loadTopProductsByCategory(category, 5);

        productAdapter.setOnItemClickListener(product -> {
            ProductDetailsFragment fragment = ProductDetailsFragment.newInstance(product);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        return root;
    }

    private void loadTopProductsByCategory(String category, int limit) {
        db.collection("products")
                .whereEqualTo("category", category)
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            productList.add(product);
                        }
                        productAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
