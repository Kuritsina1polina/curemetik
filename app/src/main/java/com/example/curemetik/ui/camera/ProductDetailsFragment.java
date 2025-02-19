package com.example.curemetik.ui.camera;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.curemetik.databinding.FragmentProductDetailsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProductDetailsFragment extends Fragment {

    private FragmentProductDetailsBinding binding;
    private DatabaseReference databaseReference;
    private String productId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProductDetailsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

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
                    String name = snapshot.child("name").getValue(String.class);
                    int rating = snapshot.child("rating").getValue(int.class);
                    String comments = snapshot.child("comments").getValue(String.class);

                    binding.productName.setText(name);
                    binding.productComments.setText(comments);
                    binding.productRating.setRating(rating);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
