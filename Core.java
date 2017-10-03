/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lorentealberto.biometria;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 *
 * @author Alberto Escribano Lorente
 */
public class Core {
    
    private final ZhangSuen thinner;
    
    private Fingerprint aux, gris;
    
    private ArrayList<Fingerprint> pasos;
    private ArrayList<Point> puntos;
    
    private Minutias minutias;
    
    private int umbral;
    private int paso_actual, pasos_totales;
    private int modo;
    
    private boolean mostrarMinutias;
    private boolean equalizada;
    private boolean cargada;

    /** Constructor por defecto*/
    public Core() {
        thinner = new ZhangSuen();
        paso_actual = 0;
        modo = 1;
        umbral = 128;
        mostrarMinutias = false;
        cargada = false;
    }
    
    /** Procesa la imagen
     @param img Imagen que se desea procesar*/
    public boolean cargar(BufferedImage img) {
        equalizada = true;
        pasos = new ArrayList<>();
        // Convierte la imagen en una matriz de numeros
        aux = convertirAHuella(img);
        pasos.add(aux);
        
        // Convierte la imagen a escala de grises
        gris = new Fingerprint(aux.getWidth(), aux.getHeight());
        aux.copy(gris);
        
        // Equaliza la imagen
        Fingerprint a = aux;
        aux = ecualizar(aux);
        pasos.add(aux);
        
        if (a == aux) equalizada = false;

        // Pasa la imagen a blanco y negro
        aux = blancoNegro(aux, umbral);
        pasos.add(aux);
        
        // Aplica filtros para eliminar el ruido
        aux = filtros(aux);
        pasos.add(aux);
        
        // Adelgaza la imagen
        aux = thinner.adelgazarImagen(aux);
        pasos.add(aux);
        
        // Calcula el numero total de pasos
        pasos_totales = pasos.size();
        
        minutias = new Minutias();
        puntos = crossingNumber(aux);
        
        
        calcularAngulos();
        cargada = true;
        return equalizada;
    }
    
    /** Gestiona el umbral de blanco y negro y vuelve a procesar la imagen
     @param umbral Umbral que se aplicara*/
    public boolean gestionarUmbral(int umbral) {
        pasos.remove(2);
        pasos.add(2, blancoNegro(gris, umbral));
        
        pasos.remove(3);
        pasos.add(3, filtros(pasos.get(2)));
        
        pasos.remove(4);
        pasos.add(4, thinner.adelgazarImagen(pasos.get(3)));
        puntos = crossingNumber(pasos.get(4));
        
        switch (paso_actual) {
            case 2:
            case 3:
            case 4:
                return true;
            default:
                return false;
        }
    }
    
    /** Avanza un paso*/
    public void pasoSiguiente() {
        paso_actual++;
        
        if (paso_actual > pasos_totales - 1)
            paso_actual = pasos_totales - 1;
    }
    
    /** Retrocede un paso*/
    public void pasoAnterior() {
        paso_actual--;
        
        if (paso_actual < 0)
            paso_actual = 0;
    }
    
    /** Gestiona el modo en el que se dibujara la imagen*/
    public void gestionarModo() {
        switch (paso_actual) {
            case 2:
            case 3:
            case 4:
                modo = 0;
                break;
            default:
                modo = 1;
                break;
        }
    }
    
    /** Convierte una huella a una imagen usando un determinado modo de colores
     @param img Huella a convertir
     @param mode Modo de colores que se usara (0 ByN | 1 Gris)*/
    private BufferedImage convertirAImagen(Fingerprint img, int mode) {
        BufferedImage aux = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                int color = img.getPixel(i, j);
                
                if (mode == 0)
                    color *= 255;
                int rgb = (255<<24 | color << 16 | color << 8 | color);
                aux.setRGB(i, j, rgb);
            }
        }
        
        if (paso_actual == 4 && mostrarMinutias) {
            for (Point m : puntos) {
                aux.setRGB(m.x, m.y, (255 << 24 | 255 << 16 | 0 << 8));
            }
        }
        
        return aux;
    }
    
    /** Aplica el filtro de eliminacion de huecos
     @param img Imagen original*/
    private Fingerprint filtro_1(Fingerprint img) {
        int width = img.getWidth();
        int height = img.getHeight();
        
        Fingerprint aux = new Fingerprint(width, height);
        
        for (int i = Settings.PERIMETRO; i < width - Settings.PERIMETRO; i++) {
            for (int j = Settings.PERIMETRO; j < height - Settings.PERIMETRO; j++) {
                int b, d, e, g, p;
                
                p = img.getPixel(i, j);
                b = img.getPixel(i, j - 1);
                d = img.getPixel(i - 1, j);
                e = img.getPixel(i + 1, j);
                g = img.getPixel(i, j + 1);

                aux.setPixel(i, j, (p | ((b & g) & (d | e)) & d & e & (b | g)));
            }
        }
        
        return aux;
    }
    
    /** Aplica el filtro de suavizado de lineas
     @param img Imagen original*/
    private Fingerprint filtro_2(Fingerprint img) {
        int width = img.getWidth();
        int height = img.getHeight();
        
        Fingerprint aux = new Fingerprint(width, height);
        
        for (int i = Settings.PERIMETRO; i < width - Settings.PERIMETRO; i++) {
            for (int j = Settings.PERIMETRO; j < height - Settings.PERIMETRO; j++) {
                int a, b, c, d, e, f, g, h, p;
                
                p = img.getPixel(i, j);
                a = img.getPixel(i - 1, j - 1);
                b = img.getPixel(i, j - 1);
                c = img.getPixel(i + 1, j - 1);
                d = img.getPixel(i - 1, j);
                e = img.getPixel(i + 1, j);
                f = img.getPixel(i - 1, j + 1);                        
                g = img.getPixel(i, j + 1);
                h = img.getPixel(i + 1, j + 1);
                
                aux.setPixel(i, j, (p & ((a | b | d) & (e | g | h)|(b | c | e) & (d | f | g))));
            }
        }
        return aux;
    }
    
    /** Calcula los angulos de las minutias*/
    private void calcularAngulos() {
        minutias.setImg(aux);
        minutias.calcularAngulos();
        
    }
    
    /** Aplica el algoritmo 'CrossingNumber' para calcular las minutias de la
     imagen
     @param img Imagen a la que se la aplicará el algoritmo*/
    private ArrayList<Point> crossingNumber(Fingerprint img) {
        int width = img.getWidth();
        int height = img.getHeight();
        
        Fingerprint aux = new Fingerprint(width, height);
        ArrayList<Point> puntos = new ArrayList<>();
        
        for (int i = Settings.PERIMETRO; i < width - Settings.PERIMETRO; i++) {
            for (int j = Settings.PERIMETRO; j < height - Settings.PERIMETRO; j++) {
                if (img.getPixel(i, j) == 1) continue;
                
                ArrayList<Integer> pts = new ArrayList<>();
                int p1, p2, p3, p4, p5, p6, p7, p8;
                int suma = 0;
                
                p1 = img.getPixel(i + 1, j);
                p2 = img.getPixel(i + 1, j - 1);
                p3 = img.getPixel(i, j - 1);
                p4 = img.getPixel(i - 1, j - 1);
                p5 = img.getPixel(i - 1, j);
                p6 = img.getPixel(i - 1, j + 1);                        
                p7 = img.getPixel(i, j + 1);
                p8 = img.getPixel(i + 1, j + 1);
                
                pts.add(p1);
                pts.add(p2);
                pts.add(p3);
                pts.add(p4);
                pts.add(p5);
                pts.add(p6);
                pts.add(p7);
                pts.add(p8);
                pts.add(p1);
                
                for (int k = 0; k < pts.size() - 1; k++)
                    suma += Math.abs(pts.get(k) - pts.get(k + 1));
                
                suma /= 2;
                
                if(suma == 1 || suma == 3) {
                    Minutia m = new Minutia(i, j);

                    
                    if (i - 8 < 0 || i + 8 > img.getWidth() - 1
                            || j - 8 < 0 || j + 8 > img.getHeight() - 1)
                        break;

                    minutias.addMinutia(m);
                    
                    puntos.add(new Point(i + 1, j));
                    puntos.add(new Point(i - 1, j)); 
                    puntos.add(new Point(i, j - 1));
                    puntos.add(new Point(i, j + 1));
                }
            }
        }
        return puntos;
    }
    
    /** Convierte una huella a blanco y negro usando un umbral determinado
     @param img Huella en escala de grises
     @param umbral Umbral para distinguir entre blanco y negro*/
    private Fingerprint blancoNegro(Fingerprint img, int umbral) {
        Fingerprint aux = new Fingerprint(img.getWidth(), img.getHeight());
        
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                int valor = img.getPixel(i, j);
                
                if (valor < umbral)
                    aux.setPixel(i, j, 0);
                else
                    aux.setPixel(i, j, 1);
            }
        }
        return aux;
    }
    
    /** Equaliza el color de una huella
     @param img Huella en escala de grises*/
    private Fingerprint ecualizar(Fingerprint img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int tam_pixel = width * height;
        int sum = 0;
        
        int media;
        double varianza = 0;
        double desviacion;

        int[] histograma = new int[256];

        float[] lut = new float[256];
        
        Fingerprint aux = new Fingerprint(width, height);
        
        for (int i = 1; i < width; i++) {
            for (int j = 1; j < height; j++) {
                int valor = img.getPixel(i, j);
                histograma[valor]++;
            }
        }
        
        for (int i = 0; i < 256; ++i) {
            sum += histograma[i];
            lut[i] = sum * 255 / tam_pixel;
        }
        
        for (int x = 1; x < width; x++) {
            for (int y = 1; y < height; y++) {
                int valor = img.getPixel(x, y);
                int nuevo_valor = (int) lut[valor];
                aux.setPixel(x, y, nuevo_valor);
            }
        }
        
        media = sum / histograma.length;
        for (int i = 0; i < histograma.length; i++) {
            double rango;
            rango = Math.pow(histograma[i] - media, 2f);
            varianza += rango;
        }
        
        varianza /= histograma.length;
        desviacion = Math.sqrt(varianza);
        
        umbral = media;
        
        return (desviacion <= Settings.UMBRAL_EQUALIZACION ? aux : img);
    }
    
    /** Convierte una imagen a una huella
     @param img Imagen real*/
    private Fingerprint convertirAHuella(BufferedImage img) {
        Fingerprint aux = new Fingerprint(img.getWidth(), img.getHeight());
        for (int x = 0; x < img.getWidth(); x++)
            for (int y = 0; y < img.getHeight(); y++) {
                int rgb;
                int r, g, b;
                short gray;
                
                rgb = img.getRGB(x, y);
                r = (rgb >> 16) & 0xFF;
                g = (rgb >> 8) & 0xFF;
                b = (rgb & 0xFF);
                
                gray = (short)(0.2126 * r + 0.7152 * g + 0.0722 * b);
                
                aux.setPixel(x, y, gray);
            }
        return aux;
    }
    
    /**Muestra las minutias
     @param b True para mostrarlas | False para no mostrarlas*/
    public void mostrarMinutias(boolean b) {
        mostrarMinutias = b;
    }
    
    /**Devuelve el paso actual*/
    public int getPaso() {
        return paso_actual;
    }
    
    /** Devuelve el umbral de la imagen*/
    public int getUmbral() {
        return umbral;
    }
    
    /** Devuelve los ángulos de todas las minutias formateados para su correcta
     visualización*/
    public String getAngulos() {
        return minutias.getAngulos();
    }
    
    /** Devuelve la imagen del paso actual*/
    public BufferedImage imagenActual() {
        return convertirAImagen(pasos.get(paso_actual), modo);
    }
      
    /** Devuelve una huella con dos filtros aplicados
     @param img Huella original*/
    private Fingerprint filtros(Fingerprint img) {
        return filtro_2(filtro_1(img));
    }
    
    public boolean isCargada() {
        return cargada;
    }
}