package etn.app.danghoc.shoppingclient.Retrofit;

import java.util.Date;

import etn.app.danghoc.shoppingclient.Model.AddCartModel;
import etn.app.danghoc.shoppingclient.Model.CartModel;
import etn.app.danghoc.shoppingclient.Model.CreateOrderModel;
import etn.app.danghoc.shoppingclient.Model.DanhMucModel;
import etn.app.danghoc.shoppingclient.Model.DeleteProductModel;
import etn.app.danghoc.shoppingclient.Model.DistrictModel;
import etn.app.danghoc.shoppingclient.Model.OrdersModel;
import etn.app.danghoc.shoppingclient.Model.SanPhamModel;
import etn.app.danghoc.shoppingclient.Model.TinhModel;
import etn.app.danghoc.shoppingclient.Model.UpdateModel;
import etn.app.danghoc.shoppingclient.Model.UpdateStatusModel;
import etn.app.danghoc.shoppingclient.Model.UpdateUserModel;
import etn.app.danghoc.shoppingclient.Model.UploadSanPhamModel;
import etn.app.danghoc.shoppingclient.Model.UserModel;
import etn.app.danghoc.shoppingclient.Model.WardModel;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface IMyShoppingAPI {
    @GET("user")
    Observable<UserModel> getUser(@Query("key") String apiKey,
                                  @Query("idUser") String idUser); //cai fbid chu la cai dien thoai

    //
    @POST("user")
    @FormUrlEncoded
    Observable<UpdateUserModel> updateUserInfo(@Field("key") String apiKey,
                                               @Field("userPhone") String userPhone,
                                               @Field("userAddress") String userAddress,
                                               @Field("userName") String userName,
                                               @Field("idUser") String fbid);

    @GET("sanPham")
    Observable<SanPhamModel> getSanPham(@Query("key") String apiKey,
                                        @Query("IdUser")String iduser);

    @GET("sanPhamByIdUser")
    Observable<SanPhamModel> getSanPhamByUser(@Query("key") String apiKey,
                                              @Query("IdUser") String IdUser);

    @GET("searchSanPham")
    Observable<SanPhamModel> searchSanPham(@Query("key") String apiKey,
                                              @Query("NameSanPham") String NameSanPham);
    @GET("sanPhamByIdDanhMuc")
    Observable<SanPhamModel>getSanPhamByIdDanhMuc(
                                            @Query("key")String key,
                                            @Query("IdDanhMuc") int IdDanhMuc );

    @DELETE("sanpham")
    Observable<DeleteProductModel> deleteProduct(@Query("key") String apiKey,
                                                 @Query("IdSP") int IdSP);




    @POST("sanpham")
    @FormUrlEncoded
    Observable<UploadSanPhamModel> uploadSanPham(
                        @Field("key") String key,
                        @Field("IdUser") String IdUser,
                        @Field("TenSP") String TenSP,
                        @Field("GiaSP") float GiaSP,
                        @Field("Mota") String Mota,
                        @Field("IdDanhMuc") int IdDanhMuc,
                        @Field("hinh") String hinh);


    @POST("updatesanpham")
    @FormUrlEncoded
    Observable<UploadSanPhamModel> updateSanPham(
            @Field("key") String key,
            @Field("IdSP")int IdSP,
            @Field("TenSP") String TenSP,
            @Field("GiaSP") float GiaSP,
            @Field("Mota") String Mota,
            @Field("IdDanhMuc") int IdDanhMuc,
            @Field("hinh") String hinh);

//=============
    // gio hang
    //=========
    @GET("giohang")
    Observable<CartModel> getCart(@Query("key") String apiKey,
                                  @Query("IdUser") String idUser);

    @DELETE("giohang")
    Observable<CartModel> deleteCart(@Query("key") String apiKey,
                                     @Query("IdUser") String idUser,
                                     @Query("IdSP") int IdSP);

    @POST("giohang")
    @FormUrlEncoded
    Observable<AddCartModel> addCart(@Field("key") String apiKey,
                                     @Field("IdSP") int intSP,
                                     @Field("Gia") float gia,
                                     @Field("TenSP") String tensp,
                                     @Field("IdUser") String iduser,
                                     @Field("IdSeller")String idSeller);




    //get province , district, word
    //==================
    @GET("shiip/public-api/master-data/province")
    Observable<TinhModel> getProvince(
            @Header("token") String token,
            @Header("Content-Type") String content
    );

    @GET("shiip/public-api/master-data/district")
    Observable<DistrictModel> getDistrict(
            @Header("token") String token,
            @Header("Content-Type") String content,
            @Query("province_id") int province_id
    );

    @GET("shiip/public-api/master-data/ward")
    Observable<WardModel> getWord(
            @Header("token") String token,
            @Header("Content-Type") String content,
            @Query("district_id") int province_id
    );

//================
    /// don hang
    @POST("donhang")
    @FormUrlEncoded
    Observable<CreateOrderModel> createOrder(@Field("key") String apiKey,
                                             @Field("DiaChi") String diachi,
                                             @Field("NgayDat") String NgayDat,
                                             @Field("gia") float  gia,
                                             @Field("IdUser") String iduser,
                                             @Field("TrangThai") int TrangThai,
                                             @Field("sdt") String sdt,
                                             @Field("IdSP") int IdSP,
                                             @Field("IdSeller") String IdSeller,
                                             @Field("TenUser")String TenUser);

    @GET("donhangByBuyer")
    Observable<OrdersModel> getOrdersByBuyer  (@Query("key") String apiKey,
                                              @Query("IdUser") String idUser);

    @GET("donhangBySeller")
    Observable<OrdersModel> getOrdersBySeller  (@Query("key") String apiKey,
                                               @Query("IdUser") String idUser);

    @POST("updatestatusdonhang")
    @FormUrlEncoded
    Observable<UpdateStatusModel> updateStatusOrder(@Field("key") String apiKey,
                                             @Field("IdDonHang")int IdDonHang,
                                               @Field("TrangThai") int TrangThai
                                             );

    @Multipart
    @POST("/uploadfile")
    Call<UpdateModel> postImage2(@Part MultipartBody.Part image, @Part("myFile") RequestBody name);

    @GET("danhmuc")
    Observable<DanhMucModel> getDanhMuc(@Query("key") String apiKey);
}
