package com.asbestosstar.fci_gen;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.ClassFile;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;

public class DupRemover {

	public static ClassPool classpool = new ClassPool();
	public static ArrayList<CtClass> ct_clazzes = new ArrayList<CtClass>();
	public static ArrayList<ClassFile> clazzes = new ArrayList<ClassFile>();

	
	
	public static void removeDupes() {
		try {
			MappingsObtainer.game_jar.reset();
			addToClasspathJarFromStream(MappingsObtainer.game_jar);
			MappingsObtainer.game_jar.reset();
			rename_all();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	public static void addToClasspathJarFromStream(InputStream jarInputStream) {  
	    try (JarInputStream jarInput = new JarInputStream(new BufferedInputStream(jarInputStream))) {  
	        JarEntry entry;  
	        while ((entry = jarInput.getNextJarEntry()) != null) {  
	            if (entry.getName().endsWith(".class")) {  
	            	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	                byte[] buffer = new byte[1024];
	                int bytesRead;
	                while ((bytesRead = jarInput.read(buffer)) != -1) {
	                    bos.write(buffer, 0, bytesRead);
	                }
	                byte[] entryBytes = bos.toByteArray();
	                ClassFile clazz_fil = new ClassFile(new DataInputStream(new ByteArrayInputStream(entryBytes)));
                    // Convert the class bytes to a CtClass
	                CtClass ctClass = classpool.makeClass(clazz_fil, false);
	                  
	                // If to_remap is true, add the CtClass to some collection or process it  
	                    // TODO: Add the CtClass to a collection or perform some other operation  
	                    // For example:  
	                     ct_clazzes.add(ctClass);  
	                
	                     try {
	                     ctClass.defrost();
	                     clazzes.add(ctClass.getClassFile());
	                     }catch(Exception e) {
	                    	 e.printStackTrace();
	                    	 //this is not likely to go well
	                    	 }
	                     
	                  
	                  
	                // Defrost the CtClass if needed  
	                // ctClass.defrost();  
	            }  
	        }  
	    } catch (IOException e) {  
	            e.printStackTrace();    
	    }  
	}
	
	
	
	
	
	
	
	
	public static void rename_all() {
		// TODO Auto-generated method stub

		for(ClassFile file: clazzes) {
		
		// Params soon
		for (MethodInfo def : file.getMethods()) {
			putMethod(def,file);
		}

		for (FieldInfo var : file.getFields()) { // I soon need to find a way to do undeclared
		//	putField(var,file);
		}
		}
	}
	
	
	
	
	
	

	
	
	
	
	public static void putMethod(MethodInfo def, ClassFile file) {
//
//		ArrayList<String> recersive_names = new ArrayList<String>();
//
//		String old_nombre = def.getName();
//		String key = file.getName().replace("/", ".") + "." + def.getName() + def.getDescriptor();
//		String nombre = FCIUpdater.main.getDefMappedName(key);;
//
//		if (nombre != null) {
//			if (classFileHasMethod(file.getName(), nombre, def.getDescriptor()) && !old_nombre.equals(nombre)) {
//				System.out.println("Dupe" + file.getName() + nombre + def.getDescriptor());
//			} else if (!old_nombre.equals(nombre)) {
//				def.setName(nombre);
//			}
//
//		}

	}
	
	
	
	
	
	
	
	
	
	

	}
	
	
	
