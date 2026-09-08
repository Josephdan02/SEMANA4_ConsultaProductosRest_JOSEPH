package com.example.consultaproductosrest_joseph;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @GET("products")
    Call<List<Producto>> obtenerProductos();

    @POST("products")
    Call<Producto> crearProducto(@Body Producto producto);

    @PUT("products/{id}")
    Call<Producto> editarProducto(@Path("id") int id, @Body Producto producto);

    @DELETE("products/{id}")
    Call<Void> eliminarProducto(@Path("id") int id);
}
