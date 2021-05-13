package com.svalero.beers.domain;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class Beer {

    private int id; // Id
    private String name; // Nombre
    private String description; // Descripción

    @SerializedName(value = "first_brewed") private String firstBrewed; // Fecha primera elaboración
    @SerializedName(value = "abv")          private float degrees; // Graduación
    @SerializedName(value = "tagline")      private String tagLine; // Eslogan
    @SerializedName(value = "image_url")    private String imageUrl; // imagen

    private Volume volume; // lista de volumen

    @Override
    public String toString() {
        return "CERVEZA -> Nombre: " + name + " // Grados: " + degrees;
    }
}
