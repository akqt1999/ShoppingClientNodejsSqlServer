package etn.app.danghoc.shoppingclient;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import etn.app.danghoc.shoppingclient.Adapter.CategoryProductAdapter;
import etn.app.danghoc.shoppingclient.Common.Common;
import etn.app.danghoc.shoppingclient.Model.CategoryProduct;
import etn.app.danghoc.shoppingclient.Model.UpdateModel;
import etn.app.danghoc.shoppingclient.Retrofit.IMyShoppingAPI;
import etn.app.danghoc.shoppingclient.Retrofit.RetrofitClient;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class UpdateProductActivity extends AppCompatActivity implements View.OnClickListener {


    CompositeDisposable compositeDisposable = new CompositeDisposable();
    Spinner spinner;
    CategoryProductAdapter adapter;
    List<CategoryProduct> categoryProducts;
    //ApiService apiService;
    IMyShoppingAPI shoppingAPI;
    Uri picUri;
    ImageButton btn_choose_img;
    Button btn_add_pd,btn_cancel;

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    private final static int ALL_PERMISSIONS_RESULT = 107;
    private final static int IMAGE_RESULT = 200;
    FloatingActionButton fabCamera, fabUpload;
    Bitmap mBitmap;
    ImageView image_pd;

    @BindView(R.id.edt_description_pd)
    EditText edt_description_pd;
    @BindView(R.id.edt_name_pd)
    EditText edt_name_pd;
    @BindView(R.id.edt_price_pd)
    EditText edt_price_pd;

    int idDanhMuc = -99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_product);

        ButterKnife.bind(this);

        btn_add_pd = findViewById(R.id.btn_add_pd);
        btn_add_pd.setOnClickListener(this);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(this);

        btn_choose_img = findViewById(R.id.btn_choose_img);
        btn_choose_img.setOnClickListener(this);

        image_pd = findViewById(R.id.image_pd);


        askPermissions();
        initRetrofitClient();

        loadSpinner();

        compositeDisposable = new CompositeDisposable();

    }
    private void initRetrofitClient() {
        OkHttpClient client = new OkHttpClient.Builder().build();


        shoppingAPI = new RetrofitClient().getInstance(Common.API_RESTAURANT_ENDPOINT)
                .create(IMyShoppingAPI.class);


    }

    private void loadSpinner() {
        spinner = findViewById(R.id.spinner_category);

        compositeDisposable.add(shoppingAPI.getDanhMuc("1234")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(danhMucModel -> {
                    if (danhMucModel.isSuccess()) {
                        categoryProducts = danhMucModel.getResult();
                        adapter = new CategoryProductAdapter(this,
                                R.layout.item_select_product, categoryProducts);
                        spinner.setAdapter(adapter);
                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                idDanhMuc = categoryProducts.get(i).getIdDanhMuc();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });

                    } else
                        Toast.makeText(this, "load category fail", Toast.LENGTH_SHORT).show();

                }, throwable -> {
                    Toast.makeText(this, "[LOAD CATEGORY PRODUCT]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));

    }


    private void askPermissions() {
        permissions.add(CAMERA);
        permissions.add(WRITE_EXTERNAL_STORAGE);
        permissions.add(READ_EXTERNAL_STORAGE);
        permissionsToRequest = findUnAskedPermissions(permissions);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }
    }

    public Intent getPickImageChooserIntent() {

        Uri outputFileUri = getCaptureImageOutputUri();

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getPackageManager();

        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }

    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getExternalFilesDir("");
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "profile.png"));
        }
        return outputFileUri;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {


            if (requestCode == IMAGE_RESULT) {


                String filePath = getImageFilePath(data);
                if (filePath != null) {
                    mBitmap = BitmapFactory.decodeFile(filePath);
                    image_pd.setImageBitmap(mBitmap);
                }
            }

        }

    }

    private String getImageFromFilePath(Intent data) {
        boolean isCamera = data == null || data.getData() == null;

        if (isCamera) return getCaptureImageOutputUri().getPath();
        else return getPathFromURI(data.getData());

    }

    public String getImageFilePath(Intent data) {
        return getImageFromFilePath(data);
    }

    private String getPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("pic_uri", picUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        picUri = savedInstanceState.getParcelable("pic_uri");
    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }
    }

    private void multipartImageUpload() {

        if (edt_description_pd.getText().toString().trim().length() == 0
                || edt_name_pd.getText().toString().trim().length() == 0
                || edt_price_pd.getText().toString().trim().length() == 0) {
            Toast.makeText(this, "chưa nhập đầy đủ thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }


        try {
            File filesDir = getApplicationContext().getFilesDir();
            File file = new File(filesDir, "image" + ".png");

            OutputStream os;
            try {
                os = new FileOutputStream(file);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                os.flush();
                os.close();
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            mBitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
            byte[] bitmapdata = bos.toByteArray();


            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();


            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("myFile", file.getName(), reqFile);
            RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "myFile");


            Call<UpdateModel> req = shoppingAPI.postImage2(body, name);
            req.enqueue(new Callback<UpdateModel>() {
                @Override
                public void onResponse(Call<UpdateModel> call, Response<UpdateModel> response) {

                    String tenSp = edt_name_pd.getText().toString();
                    float giaSp = Float.parseFloat(edt_price_pd.getText().toString());
                    String mota = edt_description_pd.getText().toString();

                    if (response.code() == 200) {
                        compositeDisposable.add(shoppingAPI.updateSanPham(
                                Common.API_KEY,
                                Common.productSelectEdit.getIdSP(),
                                tenSp, giaSp, mota, idDanhMuc,
                                new StringBuilder(Common.API_RESTAURANT_ENDPOINT)
                                        .append(response.body().getMessage()).toString()//hinh

                        )
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(uploadSanPhamModel -> {
                                    Toast.makeText(UpdateProductActivity.this, " cap nhap pham thanh cong", Toast.LENGTH_SHORT).show();
                                }, throwable -> {
                                    Toast.makeText(UpdateProductActivity.this, "[UPDATE PRODUCT]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                }));
                        Toast.makeText(UpdateProductActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(Call<UpdateModel> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Request failed", Toast.LENGTH_SHORT).show();
                    t.printStackTrace();
                    Log.e("ERROR", t.toString());
                }
            });


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.btn_cancel)
    void cancel(){
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_cancel:
                finish();
                break;
            case R.id.btn_add_pd:
                if (mBitmap != null) {
                    multipartImageUpload();
                } else {
                    Toast.makeText(getApplicationContext(), "Bitmap is null. Try again", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_choose_img:
                startActivityForResult(getPickImageChooserIntent(), IMAGE_RESULT);
                break;

        }
    }

}