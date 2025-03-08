package com.asbestosstar.fci_gen;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.util.getopt.OptionsImpl;

public class JarDescompilador {

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Uso: java JarDescompilador <ruta-jar-entrada> <directorio-salida> [<rutas-bibliotecas>...]");
            return;
        }

        String rutaJarEntrada = args[0];
        String directorioSalida = args[1];

        // Recopilar rutas de bibliotecas
        List<String> rutasBibliotecas = Collections.emptyList();
        if (args.length > 2) {
            rutasBibliotecas = Arrays.asList(Arrays.copyOfRange(args, 2, args.length));
        }

        // Asegurarse de que el directorio de salida exista
        File directorioSalidaFile = new File(directorioSalida);
        if (!directorioSalidaFile.exists()) {
            directorioSalidaFile.mkdirs();
        }

        try {
            descompilarJar(rutaJarEntrada, directorioSalida, rutasBibliotecas);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void descompilarJar(String rutaJarEntrada, String directorioSalida, List<String> rutasBibliotecas) throws IOException {
       
    	File simbolico = new File(rutaJarEntrada+"/simbolico");
    	if(simbolico.createNewFile()) {//No Necesitemos Clobber
    	Map<String, String> args = new HashMap<String, String>();
       args.put(OptionsImpl.EXTRA_CLASS_PATH.getName(), String.join(File.pathSeparator, rutasBibliotecas));//Desde MinecraftDecompiler
       args.put(OptionsImpl.OUTPUT_PATH.getName(), directorioSalida);
       CfrDriver driver = new CfrDriver.Builder().build();
       driver.analyse(Collections.singletonList(rutaJarEntrada));
    	}else {
    		System.out.println("Ya tienes el codio del juego");
    	}

    }

}




