//Kushwanth23
package com.example.foodappclient;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodappclient.databinding.ActivityVegetableBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class VegetableActivity extends AppCompatActivity {


    ActivityVegetableBinding binding;

    String category;

    StorageReference storageReference;
    DatabaseReference reference;
    FirebaseUser user;

    private Uri imageUri;

    Boolean inStock = true;


    private static final int PICK_IMG = 1;
//    private ArrayList<Uri> ImageList = new ArrayList<Uri>();
//    private int uploads = 0;
//    int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVegetableBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        category = getIntent().getStringExtra("category");


        reference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference().child("Products");
        user = FirebaseAuth.getInstance().getCurrentUser();


        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.txtSelectedFiles.setOnClickListener(v -> {
//            if (ImageList.size() > 0){
//                ImageList.clear();
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("image/*");
//                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//                startActivityForResult(intent, PICK_IMG);
//            }

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMG);


        });



//        binding.imageSwitcher.setFactory(() -> {
//            ImageView imageView = new ImageView(VegetableActivity.this);
//            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//            imageView.setLayoutParams(new ImageSwitcher.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
//                    ActionBar.LayoutParams.WRAP_CONTENT));
//            return imageView;
//        });

        binding.btnSubmit.setOnClickListener(v -> {
            String pName = binding.inputProductName.getText().toString();
            String pDescription = binding.inputDescription.getText().toString();
            String pQuantity = binding.inputQuantity.getText().toString();
            String pPrice = binding.inputPrice.getText().toString();

            if (pName.isEmpty() || pDescription.isEmpty() || pQuantity.isEmpty() || pPrice.isEmpty()) {
                Toast.makeText(VegetableActivity.this, "All fields required!", Toast.LENGTH_SHORT).show();
            } else if (imageUri == null) {
                Toast.makeText(VegetableActivity.this, "Please select image!", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage(pName, pDescription, pQuantity, pPrice);
            }
        });


        binding.inputStock.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(VegetableActivity.this);
            builder.setTitle("Select");
            builder.setCancelable(false);

            String[] items = {"Available", "Not"};
            builder.setItems(items, (dialog, which) -> {
                dialog.dismiss();
                switch (which) {
                    case 0:
                        inStock = true;
                        binding.inputStock.setText("Available");
                        break;

                    case 1:
                        inStock = false;
                        binding.inputStock.setText("Not");
                        break;


                }
            });
            builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

            builder.create().show();

        });

//        binding.imgPrevious.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (position >0){
//                    position--;
//                    binding.imageSwitcher.setImageURI(ImageList.get(position));
//                }else {
//                    Toast.makeText(VegetableActivity.this, "No previous images", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//
//        binding.imgNext.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (position < ImageList.size()-1){
//                    position++;
//                    binding.imageSwitcher.setImageURI(ImageList.get(position));
//
//                }else {
//                    Toast.makeText(VegetableActivity.this, "No more images", Toast.LENGTH_SHORT).show();
//
//                }
//            }
//        });
    }

    private void uploadImage(String pName, String pDescription, String pQuantity, String pPrice) {
        ProgressDialog progressDialog = new ProgressDialog(VegetableActivity.this);
        progressDialog.setMessage("Uploading....");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String id = reference.push().getKey();

        final StorageReference sRef = storageReference.child(System.currentTimeMillis()+".jpg");
        sRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                sRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String link= uri.toString();

                    Products products = new Products();
                    products.setImage(id);
                    products.setName(pName);
                    products.setDescription(pDescription);
                    products.setCategory(category);
                    products.setId(id);
                    products.setImage(link);
                    products.setStock(inStock);
                    products.setPrice(Integer.parseInt(pPrice));
                    products.setQuantity(pQuantity);
                    products.setPublisher(user.getUid());

                    assert id != null;
                    reference.child("Products").child(id).setValue(products).addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            progressDialog.dismiss();
                            Toast.makeText(VegetableActivity.this, "Product uploaded!", Toast.LENGTH_SHORT).show();
                            resetPereference();

                        }else {
                            progressDialog.dismiss();
                            Toast.makeText(VegetableActivity.this, "Error: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(VegetableActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }));

//        for (uploads = 0; uploads < ImageList.size(); uploads++) {
//            Uri Image = ImageList.get(uploads);
//            final StorageReference sRef = storageReference.child("image/" + Image.getLastPathSegment());
//
//            sRef.putFile(ImageList.get(uploads)).addOnSuccessListener(taskSnapshot ->
//                    sRef.getDownloadUrl().addOnSuccessListener(uri -> {
//                        String imageUrl = uri.toString();
//
//                        HashMap<String, Object> map = new HashMap<>();
//                        map.put("links",imageUrl);
//
//                        reference.child("Images").child(id).push().setValue(map).addOnCompleteListener(task -> {
//                            if (task.isSuccessful()) {
//                                progressDialog.dismiss();
//                                ImageList.clear();
//                                Toast.makeText(VegetableActivity.this, "Product uploaded!", Toast.LENGTH_SHORT).show();
//
//                            } else {
//                                progressDialog.dismiss();
//
//                                Toast.makeText(VegetableActivity.this, "Failed: " +
//                                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                            }
//                        });
//
//
//                    }).addOnFailureListener(e -> {
//                        progressDialog.dismiss();
//                        Toast.makeText(VegetableActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }));
//
//
//        }



    }

    private void resetPereference() {
        binding.inputDescription.setText("");
        binding.inputPrice.setText("");
        binding.inputProductName.setText("");
        binding.inputStock.setHint("Available");
        binding.inputQuantity.setText("");
        imageUri = null;
        binding.imageview.setImageResource(R.drawable.placeholder);


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMG) {
            if (resultCode == RESULT_OK) {
                imageUri = data.getData();
                binding.imageview.setImageURI(imageUri);


            }


        }else {
            Toast.makeText(this, "Please select image!", Toast.LENGTH_SHORT).show();
        }

    }


//    private void SendLink(String url) {
//        HashMap<String, String> hashMap = new HashMap<>();
//        hashMap.put("link", url);
//
//
//
//    }

}