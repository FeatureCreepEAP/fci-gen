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

import com.asbestosstar.minecraftmappingsobtainer.MappingsObtainer;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.ClassFile;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;

public class DupRemover {

	public ClassPool classpool = new ClassPool();
	public ArrayList<CtClass> ct_clazzes = new ArrayList<CtClass>();
	public ArrayList<ClassFile> clazzes = new ArrayList<ClassFile>();

	public MappingsObtainer obtainer;
	public FCIUpdater updater;
	
	public DupRemover(MappingsObtainer obtainer,FCIUpdater updater) {
	this.obtainer = obtainer;	
	this.updater = updater;
	}
	
	
	public void removeDupes() {
		try {
			obtainer.game_jar.reset();
			addToClasspathJarFromStream(obtainer.game_jar);
			obtainer.game_jar.reset();
			rename_all();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	public void addToClasspathJarFromStream(InputStream jarInputStream) {  
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
	
	
	
	
	
	
	
	
	public void rename_all() {
		// TODO Auto-generated method stub

		for(ClassFile file: clazzes) {
		
		// Params soon
		for (MethodInfo def : file.getMethods()) {
			putMethod(def,file);
		}

		for (FieldInfo var : file.getFields()) { // I soon need to find a way to do undeclared
			putField(var,file);
		}
		}
	}
	
	
	
	
	
	

	
	
	
	
	public void putMethod(MethodInfo def, ClassFile file) {

		ArrayList<String> recersive_names = new ArrayList<String>();

		String old_nombre = def.getName();
		String key = file.getName().replace("/", ".") + "." + def.getName() + def.getDescriptor();
		String nombre = updater.main.getDefs().get(key);;

		if (nombre != null) {
			if (classFileHasMethod(file.getName(), nombre, def.getDescriptor()) && !old_nombre.equals(nombre)) {
				System.out.println("Dupe" + file.getName() + nombre + def.getDescriptor());
			} else if (!old_nombre.equals(nombre)) {
				def.setName(nombre);
			}

		}

	}
	
	public boolean classFileHasMethod(String clazz, String method_name, String desc) {

		for (CtMethod method : getClassFromPool(clazz).getMethods()) {
			if (method.getName().equals(method_name) && method.getSignature().equals(desc)) {
				return true;
			}

		}

		return false;
	}
	
	
	
	
	
	
	public void putField(FieldInfo var, ClassFile file) {

		ArrayList<String> recersive_names = new ArrayList<String>();

		String old_nombre = var.getName();
		String key = file.getName().replace("/", ".") + "." + var.getName() + var.getDescriptor();
		String nombre = updater.main.getVars().get(key);; // just use get so we only get the oldest

		if (nombre != null) {
			if (classFileHasField(file.getName(), nombre, var.getDescriptor()) && !old_nombre.equals(nombre)) {
				System.out.println("Dupe" + file.getName() + nombre + var.getDescriptor());
			} else if (!old_nombre.equals(nombre)) {
				var.setName(nombre);
			}

		}

	}
	
	public boolean classFileHasField(String clazz, String field_name, String desc) {

		for (CtField field : getClassFromPool(clazz).getFields()) {
			if (field.getName().equals(field_name) && field.getSignature().equals(desc)) {
				return true;
			}

		}

		return false;
	}
	
	
	
	
	
	
	
	
	
	
	public CtClass getClassFromPool(String name) {
		try {// I also need a delete classes array
			return updater.sl.pool.get(name);
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
				return updater.sl.pool.makeClass(name);
		}
	}
	
	

	}
	
	
	
