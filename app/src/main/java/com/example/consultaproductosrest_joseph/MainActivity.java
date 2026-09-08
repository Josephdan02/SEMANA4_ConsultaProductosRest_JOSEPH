package com.example.consultaproductosrest_joseph;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private Button btnConsultar, btnAgregar;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private LinearLayout layoutProductos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View mainRoot = findViewById(R.id.main_root);
        ViewCompat.setOnApplyWindowInsetsListener(mainRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnConsultar = findViewById(R.id.btnConsultar);
        btnAgregar = findViewById(R.id.btnAgregar);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);
        layoutProductos = findViewById(R.id.layoutProductos);

        btnConsultar.setOnClickListener(v -> consultarProductos());
        btnAgregar.setOnClickListener(v -> mostrarFormulario(null));
    }

    private void consultarProductos() {
        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText("Consultando productos...");
        layoutProductos.removeAllViews();

        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Producto>> call = apiService.obtenerProductos();

        call.enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Producto> productos = response.body();
                    tvStatus.setText(String.format(Locale.getDefault(), "Productos encontrados: %d", productos.size()));
                    mostrarProductos(productos);
                } else {
                    tvStatus.setText("Error en la respuesta del servidor.");
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvStatus.setText("No se pudo obtener la información.\nVerifica tu conexión a Internet.");
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarProductos(List<Producto> productos) {
        int count = 1;
        for (Producto p : productos) {
            View itemView = getLayoutInflater().inflate(R.layout.item_producto, layoutProductos, false);
            
            TextView tvHeader = itemView.findViewById(R.id.tvHeader);
            TextView tvId = itemView.findViewById(R.id.tvId);
            TextView tvNombre = itemView.findViewById(R.id.tvNombre);
            TextView tvPrecio = itemView.findViewById(R.id.tvPrecio);
            TextView tvCategoria = itemView.findViewById(R.id.tvCategoria);

            tvHeader.setText(String.format(Locale.getDefault(), "Producto #%d", count++));
            tvId.setText(String.valueOf(p.getId()));
            tvNombre.setText(traducirNombre(p.getNombre()));
            tvPrecio.setText(String.format(Locale.getDefault(), "S/ %.2f", p.getPrecio()));
            tvCategoria.setText(traducirCategoria(p.getCategoria()));

            itemView.findViewById(R.id.btnEditar).setOnClickListener(v -> mostrarFormulario(p));
            itemView.findViewById(R.id.btnEliminar).setOnClickListener(v -> confirmarEliminacion(p));

            layoutProductos.addView(itemView);
        }
    }

    private void mostrarFormulario(Producto p) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_producto, null);
        TextInputEditText etNombre = dialogView.findViewById(R.id.etNombre);
        TextInputEditText etPrecio = dialogView.findViewById(R.id.etPrecio);
        TextInputEditText etCategoria = dialogView.findViewById(R.id.etCategoria);
        TextInputEditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        TextInputEditText etImagen = dialogView.findViewById(R.id.etImagen);

        boolean esEdicion = (p != null);
        if (esEdicion) {
            etNombre.setText(p.getNombre());
            etPrecio.setText(String.valueOf(p.getPrecio()));
            etCategoria.setText(p.getCategoria());
            etDescripcion.setText(p.getDescription());
            etImagen.setText(p.getImage());
        }

        new AlertDialog.Builder(this)
                .setTitle(esEdicion ? "Editar Producto" : "Nuevo Producto")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = etNombre.getText().toString();
                    double precio = Double.parseDouble(etPrecio.getText().toString().isEmpty() ? "0" : etPrecio.getText().toString());
                    String categoria = etCategoria.getText().toString();
                    String descripcion = etDescripcion.getText().toString();
                    String imagen = etImagen.getText().toString();

                    if (esEdicion) {
                        p.setNombre(nombre);
                        p.setPrecio(precio);
                        p.setCategoria(categoria);
                        p.setDescription(descripcion);
                        p.setImage(imagen);
                        editarProducto(p);
                    } else {
                        Producto nuevo = new Producto(0, nombre, precio, categoria, descripcion, imagen);
                        crearProducto(nuevo);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void crearProducto(Producto p) {
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService().crearProducto(p).enqueue(new Callback<Producto>() {
            @Override
            public void onResponse(Call<Producto> call, Response<Producto> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Producto creado exitosamente (Simulado)", Toast.LENGTH_SHORT).show();
                    consultarProductos();
                }
            }

            @Override
            public void onFailure(Call<Producto> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Error al crear: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void editarProducto(Producto p) {
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService().editarProducto(p.getId(), p).enqueue(new Callback<Producto>() {
            @Override
            public void onResponse(Call<Producto> call, Response<Producto> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Producto actualizado exitosamente (Simulado)", Toast.LENGTH_SHORT).show();
                    consultarProductos();
                }
            }

            @Override
            public void onFailure(Call<Producto> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Error al actualizar: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmarEliminacion(Producto p) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Producto")
                .setMessage("¿Estás seguro de eliminar " + p.getNombre() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarProducto(p.getId()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarProducto(int id) {
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService().eliminarProducto(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Producto eliminado exitosamente (Simulado)", Toast.LENGTH_SHORT).show();
                    consultarProductos();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Error al eliminar: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String traducirNombre(String nombreOriginal) {
        if (nombreOriginal == null) return "";
        
        // Traducciones razonables para los productos de FakeStoreAPI
        if (nombreOriginal.contains("Fjallraven - Foldsack No. 1 Backpack")) return "Mochila Fjallraven - Foldsack No. 1";
        if (nombreOriginal.contains("Mens Casual Premium Slim Fit T-Shirts")) return "Camiseta Mens Casual Premium Slim Fit";
        if (nombreOriginal.contains("Mens Cotton Jacket")) return "Chaqueta de Algodón para Hombre";
        if (nombreOriginal.contains("Mens Casual Slim Fit")) return "Camisa Mens Casual Slim Fit";
        if (nombreOriginal.contains("John Hardy Women's Legends Naga Gold")) return "Pulsera John Hardy de Oro y Plata";
        if (nombreOriginal.contains("Solid Gold Petite Micropave")) return "Anillo de Oro Sólido Petite Micropave";
        if (nombreOriginal.contains("White Gold Plated Princess")) return "Anillo Princesa Bañado en Oro Blanco";
        if (nombreOriginal.contains("Pierced Owl Rose Gold Plated")) return "Pendientes Pierced Owl Bañados en Oro Rosa";
        if (nombreOriginal.contains("WD 2TB Elements Portable External Hard Drive")) return "Disco Duro Externo WD 2TB";
        if (nombreOriginal.contains("SanDisk SSD PLUS 1TB Internal SSD")) return "Disco SSD Interno SanDisk 1TB";
        if (nombreOriginal.contains("Silicon Power 256GB SSD")) return "Disco SSD Silicon Power 256GB";
        if (nombreOriginal.contains("WD 4TB Gaming Drive Works with Playstation 4")) return "Disco Duro WD 4TB para PS4";
        if (nombreOriginal.contains("Acer SB220Q bi 21.5 inches Full HD")) return "Monitor Acer SB220Q 21.5\" Full HD";
        if (nombreOriginal.contains("Samsung 49-Inch CHG90 144Hz Curved Gaming Monitor")) return "Monitor Gaming Curvo Samsung 49\" 144Hz";
        if (nombreOriginal.contains("BIYLACLESEN Women's 3-in-1 Snowboard Jacket")) return "Chaqueta de Snowboard 3 en 1 para Mujer";
        if (nombreOriginal.contains("Lock and Love Women's Removable Hooded Jacket")) return "Chaqueta con Capucha Desmontable para Mujer";
        if (nombreOriginal.contains("Rain Jacket Women Windbreaker Striped Climbing Coat")) return "Chaqueta Impermeable Cortavientos para Mujer";
        if (nombreOriginal.contains("MBJ Women's Solid Short Sleeve Boat Neck V")) return "Camiseta MBJ Cuello Bote Manga Corta";
        if (nombreOriginal.contains("Opna Women's Short Sleeve Moisture")) return "Camiseta Opna Manga Corta";
        if (nombreOriginal.contains("DANVOUY Womens T Shirt Casual Cotton Short")) return "Camiseta DANVOUY Casual de Algodón";

        return nombreOriginal; // Retornar el original si no hay coincidencia
    }

    private String traducirCategoria(String categoriaOriginal) {
        if (categoriaOriginal == null) return "";
        
        switch (categoriaOriginal.toLowerCase()) {
            case "electronics":
                return "Electrónica";
            case "jewelery":
                return "Joyería";
            case "men's clothing":
                return "Ropa para hombre";
            case "women's clothing":
                return "Ropa para mujer";
            default:
                return categoriaOriginal;
        }
    }
}