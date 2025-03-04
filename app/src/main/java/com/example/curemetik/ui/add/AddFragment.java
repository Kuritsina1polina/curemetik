package com.example.curemetik.ui.add;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
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
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
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
    private String encodedImage;
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
        databaseReference = FirebaseDatabase.getInstance().getReference("products");

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
        // TODO пока меняем на новый, чтобы много не ломать
        // buttonAdd.setOnClickListener(v -> saveProductToDatabase());
        //buttonAdd.setOnClickListener(v -> saveProductToDatabaseNew());
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
    private String encodeImage(Bitmap bmp)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG,100, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }
    // TODO Возможно пригодится
    /*
    private String encodeImage(String path)
    {
        /*File imagefile = new File(path);
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(imagefile);
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
        Bitmap bm = BitmapFactory.decodeStream(fis);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);
        //Base64.de
        return encImage;
    }
    */
    // TODO Переделать deprecated методы
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_PICK:
                    if (data != null) {
                        imageUri = data.getData();
                        imageView.setImageURI(imageUri);
                        // Шифруем в BASE64, потому что google - жадины
                        final InputStream imageStream;
                        try {
                            imageStream = requireActivity().getContentResolver().openInputStream(imageUri);
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        encodedImage = encodeImage(selectedImage);
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


    /*private void saveProductToDatabaseNew() {
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

        // Проверка на null перед вызовом isEmpty()
        if (encodedImage == null || encodedImage.isEmpty()) {
            Toast.makeText(getContext(), "Выберите изображение продукта", Toast.LENGTH_SHORT).show();
            return;
        }

        saveProductDetailsToDatabase(productName, rating, selectedComponents, encodedImage, selectedCategory);
    }*/

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

        // Кодирование изображения в Base64
        if (selectedImageBitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            String base64EncodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            // Сохранение закодированного изображения в Firebase Realtime Database
            saveProductDetailsToDatabase(productName, rating, selectedComponents, base64EncodedImage, selectedCategory);
        } /*else if (imageUri != null) { НЕ РАБОТАЕТ
            // Обработка загрузки изображения из Uri в Firebase Storage
            final StorageReference storageReference = FirebaseStorage.getInstance().getReference("products"); // Путь к папке 'products'
            final StorageReference imageRef = storageReference.child(imageUri.getLastPathSegment()); // Имя файла (можно заменить на любое уникальное имя)

            UploadTask uploadTask = imageRef.putFile(imageUri);
            uploadTask.addOnFailureListener(exception -> {
                // Handle unsuccessful uploads
                Toast.makeText(getContext(), "Ошибка при загрузке изображения", Toast.LENGTH_SHORT).show();
            }).addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    saveProductDetailsToDatabase(productName, rating, selectedComponents, imageUrl, selectedCategory);
                });
            });*/
    }

    /** * Функция для сохранения данных продукта в Firebase Realtime Database */
    private void saveProductDetailsToDatabase(
            String productName,
            float rating,
            List<String> selectedComponents,
            String imageUrlOrBase64,
            String category
    ) {
        // Создание нового ключа для продукта
        String productKey = databaseReference.push().getKey();

        // Данные продукта
        HashMap<String, Object> productData = new HashMap<>();
        productData.put("name", productName);
        productData.put("rating", rating);
        productData.put("category", category);
        productData.put("imageUrl", imageUrlOrBase64); // Поле imageUrl для хранения ссылки на изображение
        productData.put("components", selectedComponents);

        // Сохранение данных в Firebase
        databaseReference.child(productKey).updateChildren(productData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Продукт сохранен", Toast.LENGTH_SHORT).show();
                        clearForm();
                    } else {
                        Toast.makeText(getContext(), "Ошибка при сохранении продукта", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = requireActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    /*private void saveProductDetailsToDatabase(String productName, float rating, List<String> selectedComponents, String imageUrl, String selectedCategory) {

        DatabaseReference productRef = databaseReference.push();

        Product product = new Product(productName, rating, selectedComponents, imageUrl, selectedCategory);

        productRef.setValue(product).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Продукт успешно добавлен", Toast.LENGTH_SHORT).show();
            clearForm();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Ошибка при добавлении продукта", Toast.LENGTH_SHORT).show();
        });
    }*/

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
