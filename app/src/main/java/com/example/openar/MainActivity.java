package com.example.openar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.view.PixelCopy;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    Button btn_logout, btn_addObject, btn_gallery, btn_capture;
    TextView tutorial;
    ProgressBar progressBar;
    Spinner spinner;
    private FirebaseAuth mAuth;
    private ArFragment arFragment;
    private DatabaseReference root;
    String selectedOption;
    // array of Strings to store 3D object names
    String[] objects = {"Hornbill", "Android"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tutorial = findViewById(R.id.btn_tutorial);
        btn_logout = findViewById(R.id.btn_logout);
        btn_addObject = findViewById(R.id.btn_add);
        btn_gallery = findViewById(R.id.btn_gallery);
        btn_capture = findViewById(R.id.btn_capture);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        spinner = findViewById(R.id.objectSpinner);
        spinner.setVisibility(View.INVISIBLE);
        spinner.setEnabled(false);
        avoidSpinnerDropdownFocus(spinner);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);
        Toast.makeText(getApplicationContext(), "Find a plane surface", Toast.LENGTH_LONG).show();
        arFragment.setOnTapArPlaneListener(((hitResult, plane, motionEvent) -> {
            if(selectedOption.equals("Hornbill"))
                selectedOption = "TocoToucan.sfb";
            if(selectedOption.equals("Android"))
                selectedOption = "andy.sfb";

            Anchor anchor = hitResult.createAnchor();
            ModelRenderable.builder()
                    .setSource(this, Uri.parse(selectedOption))
                    .build()
                    .thenAccept(modelRenderable -> {
                        addModelToScene(anchor, modelRenderable);
                    });
        }));

        // Determine which item of spinner is clicked
        spinner.setOnItemSelectedListener(this);
        // Create the instance of ArrayAdapter having the list of objects
        ArrayAdapter arrayAdapter = new ArrayAdapter(this,
                                                    android.R.layout.simple_spinner_item, objects);
        // set simple layout resource file for each item of spinner
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Set the ArrayAdapter data on the Spinner which binds data to spinner
        spinner.setAdapter(arrayAdapter);

        tutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertBox("Press the '+' button to select 3D object and " +
                        "tap on the ground to place them. " +
                        "To capture an image click the circular button, " +
                        "and to view saved images click the image button below.");
            }
        });

        btn_addObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setVisibility(View.VISIBLE);
                spinner.setEnabled(true);
            }
        });
        btn_gallery.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
                startActivity(intent);
            }
        });
        btn_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Warning!")
                        .setMessage("Are you sure you want to sign-out?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Initialize Firebase Auth
                                mAuth = FirebaseAuth.getInstance();
                                mAuth.signOut();
                                startActivity(new Intent(MainActivity.this, SignInActivity.class));
                                finish();
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN);
                        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
                    }
                });
                alertDialog.show();
            }
        });
    }

    private void addModelToScene(Anchor anchor, ModelRenderable modelRenderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
        transformableNode.setParent(anchorNode);
        transformableNode.setRenderable(modelRenderable);

        arFragment.getArSceneView().getScene().addChild(anchorNode);
        transformableNode.select();
    }

    public Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, bytes);
        String path = MediaStore.Images.Media
                .insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    private void takePhoto() {
        progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(getApplicationContext(), "Captured", Toast.LENGTH_SHORT).show();

        ArSceneView view = arFragment.getArSceneView();
        // Create a bitmap the size of the scene view.
        final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                                                    Bitmap.Config.ARGB_8888);
        // Create a handler thread to offload the processing of the image.
        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();
        // Make the request to copy.
        PixelCopy.request(view, bitmap, (copyResult) -> {
            if (copyResult == PixelCopy.SUCCESS) {
                try {
                    //Converting bitmap to Uri
                    Uri imgUri = getImageUri(getApplicationContext(), bitmap);
                    //Uploading to firebase database
                    uploadFile(imgUri);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                Toast.makeText(getApplicationContext(), "Failed to capture, try again", Toast.LENGTH_SHORT).show();
            }
            handlerThread.quitSafely();
        }, new Handler(handlerThread.getLooper()));
    }

    public void uploadFile(Uri imgUri) {
        progressBar.setVisibility(View.INVISIBLE);
        root = FirebaseDatabase.getInstance().getReference().child("image");
        StorageReference sref = FirebaseStorage.getInstance().getReference().child("media");
        if (imgUri != null) {
            //Store file in Firebase Storage
            final StorageReference imageRef = sref.child(System.currentTimeMillis() + "." + getFileExtension(imgUri));
            imageRef.putFile(imgUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot snapshot) {
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String phone_no = SharedPreference.readSharedSetting(getApplicationContext(), "ph_no", "false");
                                    //Store file reference in Firebase Realtime Database - metadata
                                    //String currentUserID = mAuth.getCurrentUser().toString();
                                    Model model = new Model(uri.toString());
                                    root.child(phone_no).push().setValue(model);
                                    //progressBar.setVisibility(View.INVISIBLE);
                                    //Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            //progressBar.setVisibility(View.VISIBLE);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(getApplicationContext(), "Error saving image, try again", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private String getFileExtension(Uri imgUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(imgUri));
    }

    public void avoidSpinnerDropdownFocus(Spinner spinner) {
        try {
            Field listPopupField = Spinner.class.getDeclaredField("mPopup");
            listPopupField.setAccessible(true);
            Object listPopup = listPopupField.get(spinner);
            if (listPopup instanceof ListPopupWindow) {
                Field popupField = ListPopupWindow.class.getDeclaredField("mPopup");
                popupField.setAccessible(true);
                Object popup = popupField.get((ListPopupWindow) listPopup);
                if (popup instanceof PopupWindow) {
                    ((PopupWindow) popup).setFocusable(false);
                }
            }
        } catch (Exception e) { }
    }
    private void alertBox(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Welcome!")
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN);
            }
        });
        alertDialog.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedOption = objects[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { }
}