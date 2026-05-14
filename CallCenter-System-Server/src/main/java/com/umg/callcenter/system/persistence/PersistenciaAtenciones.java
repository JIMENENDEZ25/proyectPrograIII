/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umg.callcenter.system.persistence;

/**
 *
 * @authors mk, natr, olga, jimem
 */

import com.umg.callcenter.system.model.Atencion;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PersistenciaAtenciones {
    
    private static final String ARCHIVO = "atenciones.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static synchronized void guardar(Atencion a) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO, true))) {
            String json = gson.toJson(a);
            bw.write(json);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error guardando: " + e.getMessage());
        }
    }

    public static void cargar(HashTableAtenciones tabla) {
        File archivo = new File(ARCHIVO);
        if (!archivo.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                
                try {
                    Atencion a = gson.fromJson(linea, Atencion.class);
                    if (a != null) {
                        tabla.insertar(a);
                    }
                } catch (Exception e) {
                    System.out.println("Error parseando línea: " + e.getMessage());
                }
            }
            System.out.println("[PERSISTENCIA] Cargadas atenciones previas");
        } catch (IOException e) {
            System.out.println("Error cargando: " + e.getMessage());
        }
    }
}
