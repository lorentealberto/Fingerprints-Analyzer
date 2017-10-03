/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lorentealberto.biometria;


import java.awt.Point;
import java.util.ArrayList;

/**
 *
 * @author Alberto Escribano Lorente
 */
public class ZhangSuen {
    
    private final int[][] nbrs = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}};
 
    private final int[][][] nbrGroups = {{{0, 2, 4}, {2, 4, 6}}, {{0, 2, 6}, {0, 4, 6}}};
    
    private static ArrayList<Point> toWhite = new ArrayList<>();
    
    public Fingerprint adelgazarImagen(Fingerprint img) {
        Fingerprint aux = new Fingerprint(img.getWidth(), img.getHeight());
        img.copy(aux);
        
        boolean firstStep = false;
        boolean hasChanged;
        
        
        do {
            // Reinicia la bandera
            hasChanged = false;
            
            // Cambia de paso
            firstStep = !firstStep;
            
            // Recorre la imagen
            for (int i = Settings.PERIMETRO; i < aux.getWidth() - Settings.PERIMETRO; i++) {
                for (int j = Settings.PERIMETRO; j < aux.getHeight() - Settings.PERIMETRO; j++) {
                    
                    // Comprueba si el pixel actual es negro
                    if (aux.getPixel(i, j) != 0)
                        continue;
                    
                    // Comprueba el numero de vecinos del pixel actual
                    int nn = numNeighbors(aux, i, j);
                    if (nn < 2 || nn > 6)
                        continue;
                    
                    if (numTransitions(aux, i, j) != 1)
                        continue;
                    
                    if (!atLeastOneIsWhite(aux, i, j, firstStep ? 0 : 1))
                        continue;
                    
                    toWhite.add(new Point(i, j));
                    hasChanged = true;
                }
            }
            
            for (Point p : toWhite)
                aux.setPixel(p.x, p.y, 1);
            toWhite.clear();
        } while (firstStep || hasChanged);
        return aux;
    }
    
    private int numNeighbors(Fingerprint img, int i, int j) {
        int count = 0;
        for (int k = 0; k < nbrs.length - 1; k++) {
            if (img.getPixel(i + nbrs[k][1], j + nbrs[k][0]) == 1)
                count ++;
        }
        return count;
    }
    
    private int numTransitions(Fingerprint img, int r, int c) {
        int count = 0;
        for (int i = 0; i < nbrs.length - 1; i++)
            if (img.getPixel(r + nbrs[i][1], c + nbrs[i][0]) == 0)
                if (img.getPixel(r + nbrs[i + 1][1], c + nbrs[i + 1][0]) == 1)
                    count++;
        return count;
    }
    
    private boolean atLeastOneIsWhite(Fingerprint img, int r, int c, int step) {
        int count = 0;
        int[][] group = nbrGroups[step];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < group[i].length; j++) {
                int[] nbr = nbrs[group[i][j]];
                if (img.getPixel(r + nbr[1], c + nbr[0]) == 1) {
                    count++;
                    break;
                }
            }
        }
        return count > 1;
    }
}