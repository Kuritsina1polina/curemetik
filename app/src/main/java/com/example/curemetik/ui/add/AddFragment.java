package com.example.curemetik.ui.add;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

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
    private static final int REQUEST_PERMISSIONS = 100;
    private Spinner spinnerCategory;
    private String selectedCategory;

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
        spinnerCategory = binding.spinnerCategory;

        // Список категорий
        String[] categories = {
                "Макияж",
                "Лицо",
                "Глаза",
                "Губы",
                "Брови",
                "Уход",
                "Волосы"
        };

        // Настройка адаптера для Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // Обработка выбора категории
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categories[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategory = null;
            }
        });

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
        requestPermissions();

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
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "temp_image.jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        // values.put(MediaStore.Images.Media.DESCRIPTION, "Снимок продукта");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        ContentResolver resolver = requireContext().getContentResolver();
        imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            // TODO Old Way
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_PICK:
                    if (data != null) {
                        imageUri = data.getData();
                        imageView.setImageURI(imageUri);
                    }
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    if (imageUri != null) {
                        imageView.setImageURI(imageUri);
                    }
                    break;
            }
        }
    }

    private void requestPermissions() {
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        requestPermissions(permissions, REQUEST_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешения предоставлены
            } else {
                Toast.makeText(requireContext(), "Разрешения не предоставлены", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void saveProductToDatabase() {
        // Название продукта
        EditText productNameEditText = binding.editText;
        String productName = productNameEditText.getText().toString().trim();
        // Рейтинг
        RatingBar ratingBar = binding.ratingBar;
        float rating = ratingBar.getRating();
        if (productName.isEmpty()) {
            Toast.makeText(getContext(), "Введите название продукта", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedCategory == null || selectedCategory.isEmpty()) {
            Toast.makeText(getContext(), "Выберите категорию", Toast.LENGTH_SHORT).show();
            return;
        }
        // TODO пока комментируем работу с изображением

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
        // TODO пока комментируем работу с изображением

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("product_images");
        StorageReference imageRef;

        // TODO не работет пока код upload
        if (imageUri != null) {
            imageRef = storageReference.child(imageUri.getLastPathSegment());
            String imageUrl = imageRef.getDownloadUrl().toString();

            UploadTask uploadTask = imageRef.putFile(imageUri);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    saveProductDetailsToDatabase(productName, rating, selectedComponents, imageUrl, selectedCategory);
                }
            });
            /*
            uploadTask.addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                saveProductDetailsToDatabase(productName, rating, selectedComponents, imageUrl, selectedCategory);
            }));
             */
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            imageRef = storageReference.child(System.currentTimeMillis() + ".jpg");
            UploadTask uploadTask = imageRef.putBytes(data);
            uploadTask.addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                saveProductDetailsToDatabase(productName, rating, selectedComponents, imageUrl, selectedCategory);
            }));
        }

    }

    private void saveProductDetailsToDatabase(String productName, float rating, List<String> selectedComponents, String imageUrl, String selectedCategory) {
        /*
        DatabaseReference productRef = databaseReference.push();

        Product product = new Product(productName, rating, selectedComponents, imageUrl);

        productRef.setValue(product).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Продукт успешно добавлен", Toast.LENGTH_SHORT).show();
            clearForm();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Ошибка при добавлении продукта", Toast.LENGTH_SHORT).show();
        });
        */

        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("products");

        Product product = new Product(productName, rating, selectedComponents, imageUrl, selectedCategory);
        productRef.push().setValue(product)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getContext(), "Продукт успешно добавлен", Toast.LENGTH_SHORT).show();
                    clearForm();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Ошибка при добавлении продукта", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void clearForm() {
        binding.editText.setText("");
        binding.ratingBar.setRating(0);
        imageView.setImageDrawable(null);
        selectedImageBitmap = null;
        imageUri = null;
        spinnerCategory.setSelection(0);
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
