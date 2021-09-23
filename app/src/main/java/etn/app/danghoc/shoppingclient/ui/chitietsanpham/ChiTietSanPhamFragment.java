package etn.app.danghoc.shoppingclient.ui.chitietsanpham;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.andremion.counterfab.CounterFab;
import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import etn.app.danghoc.shoppingclient.Common.Common;
import etn.app.danghoc.shoppingclient.Model.SanPham;
import etn.app.danghoc.shoppingclient.R;
import etn.app.danghoc.shoppingclient.Retrofit.IMyShoppingAPI;
import etn.app.danghoc.shoppingclient.Retrofit.RetrofitClient;
import etn.app.danghoc.shoppingclient.databinding.FragmentDetailSanphamBinding;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ChiTietSanPhamFragment extends Fragment {


    private IMyShoppingAPI myRestaurantAPI;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private ChiTietSanPhamViewModel slideshowViewModel;
    private FragmentDetailSanphamBinding binding;

    Unbinder unbinder;
    @BindView(R.id.imgFood)
    ImageView imgFood;
    @BindView(R.id.txtFoodName)
    TextView txtFoodName;
    @BindView(R.id.txtFoodPrice)
    TextView txtFoodPrice;
    @BindView(R.id.txtFoodDescription)
    TextView txtFoodDescription;
    @BindView(R.id.txt_phone)
    TextView txt_phone;

    @BindView(R.id.btn_cart)
    CounterFab btn_cart;

    @OnClick(R.id.btn_cart)
    public void addCart ()
    {
            compositeDisposable.add(myRestaurantAPI.addCart(
                    Common.API_KEY,
                    Common.selectSanPham.getIdSP(),
                    Common.selectSanPham.getGiaSP(),
                    Common.selectSanPham.getTenSP(),
                    Common.currentUser.getIdUser(),
                    Common.selectSanPham.getIdUser()//id of seller
                    )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(cartModel -> {
                        if(cartModel.isSuccess())
                        {
                            Toast.makeText(getContext(), "add cart success", Toast.LENGTH_SHORT).show();
                        }

                    },throwable -> {
                        Toast.makeText(getContext(), "[ADD CART]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }));
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        slideshowViewModel =
                new ViewModelProvider(this).get(ChiTietSanPhamViewModel.class);

        binding = FragmentDetailSanphamBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        slideshowViewModel.getMutableLiveDataSanPham().observe(this,sanPham -> {
                if(sanPham!=null)
                    displayDetal(sanPham);
        });


        unbinder= ButterKnife.bind(this,root);

        init();

        return root;
    }

    private void init() {
        myRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyShoppingAPI.class);
    }

    private void displayDetal(SanPham sanPham) {
        Glide.with(getContext()).load(sanPham.getHinh()).into(imgFood);
        txtFoodName.setText(sanPham.getTenSP());
        txtFoodPrice.setText(Common.formatPrice(sanPham.getGiaSP()));
        txtFoodDescription.setText(sanPham.getMoTa());
        txt_phone.setText(Common.currentUser.getPhoneUser());

        ((AppCompatActivity) getActivity())
                .getSupportActionBar().setTitle(sanPham.getTenSP());


    }

    private void calculateTotalPrice() {
        double totalPrice = Double.parseDouble(Common.selectSanPham.getGiaSP() + ""), displayPrice = 0.0;
        displayPrice=totalPrice;
        displayPrice = Math.round(displayPrice * 100.0 / 100.0);
        txtFoodPrice.setText(new StringBuilder().append(Common.formatPrice(displayPrice)));

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}