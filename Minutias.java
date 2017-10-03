/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lorentealberto.biometria;

import java.util.ArrayList;

/**
 *
 * @author Alberto Escribano Lorente
 */
public class Minutias {
    
    private final ArrayList<Minutia> datos;
    private Fingerprint img;
    
    /** Constructor por defecto*/
    public Minutias() {
        datos = new ArrayList<>();
    }
    
    /** Añade una minutia
     @param m Minutia que se añadirá*/
    public void addMinutia(Minutia m) {
        datos.add(m);
    }
    
    /** Calcula los ángulos de todas las minutias que hay almacenadas en la lista*/
    public void calcularAngulos() {
        double numerador, denominador;
        double resultado;
        numerador = denominador = 0;
        
        for (Minutia m : datos) {
            
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 16; j++) {
                    int gx = gx(m.getX() + (i - 8), m.getY() + (j - 8));
                    int gy = gy(m.getX() + (i - 8), m.getY() + (j - 8));
                    
                    if (gx != 0 && gy != 0) {
                        numerador += 2 * gx * gy;
                        denominador += (gx*gx) - (gy*gy);
                    }
                }
            }
            
            resultado = numerador / denominador;
            resultado = Math.atan(resultado) / 2;
            
            m.setAngulo(Math.toDegrees(resultado));
        }
    }
    
    /** Calcula el gradiente x de la posición XY
     @param x Coordenada X
     @param y Coordenada Y*/
    private int gx(int x, int y) {
        int z1, z2, z3, z7, z8, z9;
        int res;
        
        z1 = img.getPixel(x - 1, y - 1);
        z2 = img.getPixel(x, y - 1);
        z3 = img.getPixel(x + 1, y + 1);
        
        z7 = img.getPixel(x - 1, y + 1);
        z8 = img.getPixel(x, y + 1);
        z9 = img.getPixel(x + 1, y + 1);
        
        res = (z7 + 2*z8 + z9) - (z1 + 2*z2 + z3);
        
        return res;
    }
    
    /** Calcula el gradiente y de la posición XY
     @param x Coordenada X
     @param y Coordenada Y*/
    private int gy(int x, int y) {
        int z3, z6, z9, z1, z4, z7;
        int res;
        
        z3 = img.getPixel(x + 1, y - 1);
        z6 = img.getPixel(x + 1, y);
        z9 = img.getPixel(x + 1, y + 1);
        
        z1 = img.getPixel(x - 1, y - 1);
        z4 = img.getPixel(x - 1, y);
        z7 = img.getPixel(x - 1, y + 1);
        
        res = (z3 + 2*z6 + z9) - (z1 + 2*z4 + z7);
        
        return res;
    }
    
    /** Devuelve los ángulos formateados de todas las minutias para una correcta
     visualización por pantalla*/
    public String getAngulos() {
        String angulos = "";
        for (Minutia m : datos) {
            angulos += String.format("%.2f", m.getAngulo()) + "\n";
        }
        return angulos;
    }
    
    /** Establece la imagen sobre la cual se calcularán los gradientes
     @param img Imagen que se establecerá*/
    public void setImg(Fingerprint img) {
        this.img = img;
    }
}
