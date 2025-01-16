package com.example.curemetik.ui.add;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.curemetik.R;
import com.example.curemetik.databinding.FragmentAddBinding;
import com.example.curemetik.models.CosmeticItem;
import com.example.curemetik.models.CosmeticsAdapter;
import com.example.curemetik.models.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class AddFragment extends Fragment {

    private FragmentAddBinding binding;
    private RecyclerView recyclerView;
    private CosmeticsAdapter cosmeticsAdapter;
    private List<CosmeticItem> cosmeticItems;
    private List<CosmeticItem> filteredCosmeticItems;
    private DatabaseReference databaseReference;
    private EditText searchEditText;
    private ImageView imageView;
    private Uri imageUri;
    private Bitmap selectedImageBitmap;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AddViewModel addViewModel = new ViewModelProvider(this).get(AddViewModel.class);

        binding = FragmentAddBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //final TextView textView = binding.textAdd;
        //addViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        recyclerView = binding.recyclerView;
        searchEditText = binding.searchEditText;
        imageView = binding.imageView;
        RatingBar ratingBar = binding.ratingBar;
        Button buttonGallery = binding.buttonGallery;
        Button buttonCamera = binding.buttonCamera;
        Button buttonAdd = binding.buttonAdd;

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        cosmeticItems = new ArrayList<>();
        filteredCosmeticItems = new ArrayList<>();
        cosmeticsAdapter = new CosmeticsAdapter(getContext(), filteredCosmeticItems);
        recyclerView.setAdapter(cosmeticsAdapter);

        // Инициализация Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("cosmetics");

        // Извлечение данных из Firebase
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cosmeticItems.clear();
                filteredCosmeticItems.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CosmeticItem cosmeticItem = snapshot.getValue(CosmeticItem.class);
                    cosmeticItems.add(cosmeticItem);
                    filteredCosmeticItems.add(cosmeticItem);
                }
                cosmeticsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибок
            }
        });

        // Настройка поиска
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        // Обработка нажатия на кнопку "Загрузить из галереи"
        buttonGallery.setOnClickListener(v -> openGallery());

        // Обработка нажатия на кнопку "Сфотографировать"
        buttonCamera.setOnClickListener(v -> openCamera());

        // Обработка нажатия на кнопку "Добавить"
        buttonAdd.setOnClickListener(v -> saveProductToDatabase());

        return root;
    }

    private void filter(String text) {
        filteredCosmeticItems.clear();
        for (CosmeticItem item : cosmeticItems) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredCosmeticItems.add(item);
            }
        }
        cosmeticsAdapter.notifyDataSetChanged();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_PICK:
                    if (data != null) {
                        imageUri = data.getData();
                        imageView.setImageURI(imageUri);
                    }
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    if (data != null) {
                        Bundle extras = data.getExtras();
                        if (extras != null) {
                            selectedImageBitmap = (Bitmap) extras.get("data");
                            imageView.setImageBitmap(selectedImageBitmap);
                        }
                    }
                    break;
            }
        }
    }

    private void saveProductToDatabase() {
        EditText productNameEditText = binding.editText;
        String productName = productNameEditText.getText().toString().trim();
        RatingBar ratingBar = binding.ratingBar;
        float rating = ratingBar.getRating();

        if (productName.isEmpty()) {
            Toast.makeText(getContext(), "Введите название продукта", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageBitmap == null && imageUri == null) {
            Toast.makeText(getContext(), "Выберите или сделайте фото продукта", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> selectedComponents = new ArrayList<>();
        for (CosmeticItem item : filteredCosmeticItems) {
            if (item.isSelected()) {
                selectedComponents.add(item.getName());
            }
        }

        if (selectedComponents.isEmpty()) {
            Toast.makeText(getContext(), "Выберите компоненты продукта", Toast.LENGTH_SHORT).show();
            return;
        }

        // Сохранение изображения в Firebase Storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("product_images");
        StorageReference imageRef;
        if (imageUri != null) {
            imageRef = storageReference.child(imageUri.getLastPathSegment());
            UploadTask uploadTask = imageRef.putFile(imageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                saveProductDetailsToDatabase(productName, rating, selectedComponents, imageUrl);
            }));
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            imageRef = storageReference.child(System.currentTimeMillis() + ".jpg");
            UploadTask uploadTask = imageRef.putBytes(data);
            uploadTask.addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                saveProductDetailsToDatabase(productName, rating, selectedComponents, imageUrl);
            }));
        }
    }

    private void saveProductDetailsToDatabase(String productName, float rating, List<String> selectedComponents, String imageUrl) {
        DatabaseReference productRef = databaseReference.push();
        Product product = new Product(productName, rating, selectedComponents, imageUrl);
        productRef.setValue(product).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Продукт успешно добавлен", Toast.LENGTH_SHORT).show();
            clearForm();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Ошибка при добавлении продукта", Toast.LENGTH_SHORT).show();
        });
    }

    private void clearForm() {
        binding.editText.setText("");
        binding.ratingBar.setRating(0);
        imageView.setImageDrawable(null);
        selectedImageBitmap = null;
        imageUri = null;
        for (CosmeticItem item : filteredCosmeticItems) {
            item.setSelected(false);
        }
        cosmeticsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
