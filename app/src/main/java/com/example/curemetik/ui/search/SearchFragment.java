package com.example.curemetik.ui.search;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.curemetik.R;
import com.example.curemetik.databinding.FragmentSearchBinding;
import com.example.curemetik.models.Product;
import com.example.curemetik.models.Product1;
import com.example.curemetik.ui.camera.ProductDetailsFragment;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private EditText searchEditText;
    private RecyclerView recyclerView;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private FirebaseRecyclerAdapter<Product1, ProductViewHolder> adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SearchViewModel searchViewModel =
                new ViewModelProvider(this).get(SearchViewModel.class);

        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        searchEditText = root.findViewById(R.id.searchEditText);
        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        root.findViewById(R.id.searchButton).setOnClickListener(v -> performSearch());

        return root;
    }

    private void performSearch() {
        String searchQuery = searchEditText.getText().toString().trim();
        if (!searchQuery.isEmpty()) {
            Query query = FirebaseDatabase.getInstance().getReference().child("products")
                    .orderByChild("name").startAt(searchQuery).endAt(searchQuery + "\uf8ff");

            FirebaseRecyclerOptions<Product1> options =
                    new FirebaseRecyclerOptions.Builder<Product1>()
                            .setQuery(query, Product1.class)
                            .build();

            adapter = new FirebaseRecyclerAdapter<Product1, ProductViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull ProductViewHolder holder, int position, @NonNull Product1 model) {
                    // Этот метод вызывается для обновления данных в ViewHolder
                    holder.bind(model);
                }

                @NonNull
                @Override
                public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    // Этот метод создает новый ViewHolder
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_product, parent, false);
                    return new ProductViewHolder(view);
                }
            };
            recyclerView.setAdapter(adapter);
            adapter.startListening();
        } else {
            Toast.makeText(getContext(), "Please enter a search query", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        private final TextView productName;
        private final TextView productRating;
        private final ImageView productImage;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productRating = itemView.findViewById(R.id.productRating);
            productImage = itemView.findViewById(R.id.productImage);
        }

        public void bind(Product1 product) {
            productName.setText(product.getName());
            productRating.setText(String.valueOf(product.getRating()));
            Picasso.get().load(product.getImageUrl()).into(productImage);

            itemView.setOnClickListener(v -> {
                // Переход на новый фрагмент с деталями продукта
                // Теперь вызываем метод findProductInDatabase
                findProductInDatabase(productName.getText().toString());
            });
        }
    }

    // Метод для поиска продукта в базе данных
    private void findProductInDatabase(String productName) {
        // Указываем путь к таблице 'products'
        DatabaseReference productsReference = databaseReference.child("products");

        // Производим поиск по полю 'name' в таблице 'products'
        productsReference.orderByChild("name").equalTo(productName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                        String productId = productSnapshot.getKey();
                        navigateToProductDetails(productId);
                    }
                } else {
                    Toast.makeText(requireContext(), "Product not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Метод для навигации на фрагмент с деталями продукта
    private void navigateToProductDetails(String productId) {
        Bundle bundle = new Bundle();
        bundle.putString("productId", productId);
        Navigation.findNavController(requireView()).navigate(R.id.action_navigation_search_to_ProductDetailsFragment, bundle);
    }
}