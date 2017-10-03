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
public class Minutia {
    
    private int x, y;
    private double angulo;
   
    /** Constructor parametrizado
     @param Coordenada X
     @param Coordenada Y*/
    public Minutia(int x, int y) {
        this.x = x;
        this.y = y;
        angulo = 0;
    }
    
    // GETTERS & SETTERS
    
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
    public double getAngulo() {
        return angulo;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
    
    public void setAngulo(double d) {
        angulo = d;
    }
}
