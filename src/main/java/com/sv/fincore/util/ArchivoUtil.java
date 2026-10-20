package com.sv.fincore.util;

import java.io.*;

public class ArchivoUtil {

    private static final String CARPETA = "datos";

    public static void guardar(String ruta, Object objeto) throws IOException {
        File dir = new File(CARPETA);
        if (!dir.exists()) dir.mkdirs();

        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(ruta))) {
            oos.writeObject(objeto);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T cargar(String ruta, T porDefecto) throws IOException, ClassNotFoundException {
        File f = new File(ruta);
        if (!f.exists()) return porDefecto;

        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(f))) {
            return (T) ois.readObject();
        }
    }
}
