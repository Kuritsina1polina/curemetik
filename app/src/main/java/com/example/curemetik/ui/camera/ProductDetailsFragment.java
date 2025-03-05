package com.example.curemetik.ui.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.curemetik.databinding.FragmentProductDetailsBinding;
import com.example.curemetik.models.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailsFragment extends Fragment {

    private FragmentProductDetailsBinding binding;
    private DatabaseReference databaseReference;
    private String productId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProductDetailsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Привяжем кнопку "назад" к Toolbar
        binding.toolbar.setNavigationOnClickListener(v -> {
            // Возврат на предыдущий фрагмент
            requireActivity().onBackPressed();
        });

        // Get product ID from arguments
        productId = getArguments().getString("productId");

        // Initialize Firebase Database Reference
        databaseReference = FirebaseDatabase.getInstance().getReference("products").child(productId);

        // Load product details
        loadProductDetails();

        // Set up comment submission
        Button submitCommentButton = binding.submitCommentButton;
        submitCommentButton.setOnClickListener(v -> submitComment());

        return root;
    }

    private void loadProductDetails() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Получаем общие данные о продукте
                    String name = snapshot.child("name").getValue(String.class);
                    int rating = snapshot.child("rating").getValue(Integer.class);
                    String comments = snapshot.child("comments").getValue(String.class);

                    // Получаем закодированное изображение
                    String base64Image = snapshot.child("base64Image").getValue(String.class);

                    // Декодируем и устанавливаем изображение
                    if (base64Image != null) {
                        decodeAndSetImage(base64Image);
                    }

                    // Остальные данные
                    binding.productName.setText(name);
                    binding.productComments.setText(comments);
                    binding.productRating.setRating(rating);

                    // Получаем список компонентов из внутренней таблицы 'components'
                    List<String> components = new ArrayList<>();
                    DataSnapshot componentsSnapshot = snapshot.child("components");
                    if (componentsSnapshot.exists()) {
                        for (DataSnapshot componentSnapshot : componentsSnapshot.getChildren()) {
                            String component = componentSnapshot.getValue(String.class);
                            components.add(component);
                        }
                    }

                    // Объединяем компоненты в одну строку и выводим
                    String componentsAsString = TextUtils.join(", ", components);
                    binding.productComponents.setText(componentsAsString);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Обработка ошибки
                Toast.makeText(getContext(), "Ошибка при загрузке данных: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void decodeAndSetImage(String base64Image) {
        // Декодируем Base64 строку в байтовый массив
        byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);

        // Создаем Bitmap из байтового массива
        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

        // Устанавливаем Bitmap в ImageView
        binding.productImage.setImageBitmap(bitmap);
    }

    private void submitComment() {
        EditText commentEditText = binding.commentEditText;
        String comment = commentEditText.getText().toString();
        RatingBar ratingBar = binding.commentRatingBar;
        float rating = ratingBar.getRating();

        if (!comment.isEmpty()) {
            // Save comment and rating to Firebase
            String commentId = databaseReference.child("comments").push().getKey();
            if (commentId != null) {
                databaseReference.child("comments").child(commentId).child("text").setValue(comment);
                databaseReference.child("comments").child(commentId).child("rating").setValue(rating);
            }
            commentEditText.setText("");
            ratingBar.setRating(0);
        }
    }

    public static ProductDetailsFragment newInstance(Product product) {
        ProductDetailsFragment fragment = new ProductDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable("product", (Parcelable) product);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}