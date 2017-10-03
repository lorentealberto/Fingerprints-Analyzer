/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lorentealberto.biometria;

/**
 *
 * @author Alberto Escribano Lorente
 */
public class Fingerprint {
    
    private short[][] data;
    private int width, height;
    
    /** Constructor parametrizado
     @param width Anchura de la imagen
     @param height Altura de la imagen*/
    public Fingerprint(int width, int height) {
        data = new short[width][height];
        this.width = width;
        this.height = height;
    }
    
    /** Devuelve 'true' o 'false' si el pixel en la posición dada por x e y
     es arista.
     @param x Coordenada X de la posición que se comprobará
     @param y Coordenada Y de la posición que se comprobará*/
    public boolean esArista(int x, int y) {
        return getPixel(x, y) == 0;
    }
    
    /** Devuelve el valor del pixel en la posición x e y
     @param x Coordenada X
     @param y Coordenada Y*/
    public short getPixel(int x, int y) {
        return data[x][y];
    }
    
    /** Modifica el valor del pixel en la posición x, y
     @param x Coordenada X
     @param y Coordenada Y
     @param valor Nuevo valor*/
    public void setPixel(int x, int y, int valor) {
        data[x][y] = (short) valor;
    }
    
    /** Hace una copia de la imagen
     @param dest Imagen auxiliar donde se copiará la imagen*/
    public void copy(Fingerprint dest) {
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                dest.setPixel(i, j, getPixel(i, j));
    }
    
    // GETTERS
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
}
