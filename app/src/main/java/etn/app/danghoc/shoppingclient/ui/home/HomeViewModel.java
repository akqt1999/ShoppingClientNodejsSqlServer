package etn.app.danghoc.shoppingclient.ui.home;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import etn.app.danghoc.shoppingclient.Callback.ISanPhamCallbackListener;
import etn.app.danghoc.shoppingclient.Common.Common;
import etn.app.danghoc.shoppingclient.Model.SanPham;
import etn.app.danghoc.shoppingclient.Retrofit.IMyShoppingAPI;
import etn.app.danghoc.shoppingclient.Retrofit.RetrofitClient;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class HomeViewModel extends ViewModel implements ISanPhamCallbackListener {

    private MutableLiveData<String> messageError;
    private MutableLiveData<List<SanPham>> listSanPham;

    private IMyShoppingAPI shoppingAPI;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private ISanPhamCallbackListener restaurantCallbackListener;

    public HomeViewModel() {

        shoppingAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyShoppingAPI.class);

        restaurantCallbackListener=this;
    }

    public MutableLiveData<List<SanPham>> getListSanPham() {
        if(listSanPham==null)
        {
            listSanPham=new MutableLiveData<>();
            messageError=new MutableLiveData<>();
            loadSanPham();
        }
        return listSanPham;
    }

    public MutableLiveData <String>getMessageError(){
        if(messageError==null)
            messageError=new MutableLiveData<>();
        return messageError;
    }

    private void loadSanPham() {
        compositeDisposable.
                add(shoppingAPI.getSanPham(Common.API_KEY,Common.currentUser.getIdUser())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(sanPhamModel -> {
                            if(sanPhamModel.isSuccess())
                            {
                                restaurantCallbackListener.onSanPhamLoadSuccess(sanPhamModel.getResult());
                            }

                        },throwable -> {
                            restaurantCallbackListener.onSanPhamLoadFail(throwable.getMessage());
                        })
                );

    }

    @Override
    public void onSanPhamLoadSuccess(List<SanPham> sanPhamModelList) {
        listSanPham.setValue(sanPhamModelList);

    }

    @Override
    public void onSanPhamLoadFail(String message) {
        messageError.setValue(message);

    }
}