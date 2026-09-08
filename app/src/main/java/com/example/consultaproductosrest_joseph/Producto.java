package com.example.consultaproductosrest_joseph;

import com.google.gson.annotations.SerializedName;

public class Producto {
    private int id;

    @SerializedName("title")
    private String nombre;

    @SerializedName("price")
    private double precio;

    @SerializedName("category")
    private String categoria;

    public Producto() {}

    public Producto(int id, String nombre, double precio, String categoria) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.categoria = categoria;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
}
