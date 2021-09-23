package etn.app.danghoc.shoppingclient;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;
import etn.app.danghoc.shoppingclient.Common.Common;
import etn.app.danghoc.shoppingclient.Retrofit.IMyShoppingAPI;
import etn.app.danghoc.shoppingclient.Retrofit.RetrofitClient;
import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final int APP_REQUEST_CODE = 1121;
    private   FirebaseAuth.AuthStateListener listener;
    private   FirebaseAuth firebaseAuth ;
    private List<AuthUI.IdpConfig> providers;

    AlertDialog dialog;

    IMyShoppingAPI myRestaurantAPI;
    CompositeDisposable compositeDisposable=new CompositeDisposable();

    @BindView(R.id.btn_sign_in)
    Button btn_sign_in;

    @OnClick(R.id.btn_sign_in)
    void loginUser()
    {
        //Intent intent=
        Toast.makeText(this, "di vao login", Toast.LENGTH_SHORT).show();
//        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
//                .setAvailableProviders(providers).build(),APP_REQUEST_CODE);


        FirebaseAuth  mAuth = FirebaseAuth.getInstance();
// set this to remove reCaptcha web // xoa cai captcha
        mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);

        someActivityResultLauncher.launch(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build());

    }




    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {

                        // There are no request codes
                        Intent data = result.getData();
                        Toast.makeText(MainActivity.this, "ket cai phone number", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(MainActivity.this, "this is manin", Toast.LENGTH_SHORT);
        initt();
    }

    private void initt() {
        providers= Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());

        firebaseAuth= FirebaseAuth.getInstance();

        ButterKnife.bind(this);


        listener=firebaseAuth1 -> { // cai nay la lang nghe su kien login
            FirebaseUser user=firebaseAuth1.getCurrentUser();

            if(user!=null) //user really login
            {
                compositeDisposable.add(myRestaurantAPI.getUser(Common.API_KEY,user.getUid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(userModel -> {
                            if(userModel.isSuccess())
                            {
                                Common.currentUser=userModel.getResult().get(0);
                                startActivity(new Intent(MainActivity.this,HomeActivity.class));
                                //  startActivity(new Intent(MainActivity.this,ActivityTest.class));
                                Toast.makeText(this, "ket thuc o hang trn", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                            else
                            {
                                Toast.makeText(this, "ket thuc o hang duoi", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(MainActivity.this,UpdateInfoActivity.class));
                                finish();
                            }
                        }));
            }
            else
            {
                //   loginUser(); //
                //khi vua vao nó sẽ vào đây , có nghĩa là nó đang kiểm tra cái này đã có user hay chwua
                // chwua có thì tự động vào lun
            }
        };

        Paper.init(this);
        dialog= new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI= RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyShoppingAPI.class);


    }

    @Override
    protected void onStart() {
        super.onStart();
        if(listener!=null && firebaseAuth!=null)
            firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {

        if(listener!=null&&firebaseAuth!=null)
            firebaseAuth.removeAuthStateListener(listener);

        super.onStop();

    }


}
