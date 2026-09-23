package com.sv.fincore.util;

import java.io.*;

public class ArchivoUtil {

    private static final String CARPETA = "datos";

    public static void guardar(String ruta, Object objeto) {
        File dir = new File(CARPETA);
        if (!dir.exists()) dir.mkdirs();

        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(ruta))) {
            oos.writeObject(objeto);
        } catch (IOException e) {
            System.err.println("Error al guardar " + ruta + ": " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T cargar(String ruta, T porDefecto) {
        File f = new File(ruta);
        if (!f.exists()) return porDefecto;

        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(f))) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al cargar " + ruta + ": " + e.getMessage());
            return porDefecto;
        }
    }
}
