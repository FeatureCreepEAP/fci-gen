package com.asbestosstar.fci_gen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;

import com.asbestosstar.assistremapper.Mappings;
import com.asbestosstar.assistremapper.mappings.PDMEMappings;
import com.asbestosstar.minecraftmappingsobtainer.MappingsObtainerUtils;
import com.asbestosstar.minecraftmappingsobtainer.McPom;
import com.asbestosstar.minecraftmappingsobtainer.McPom.Dep;

public class Pacheador {

	// 12w18a Pre Release for 1.3 which combined server and client
	public static File carpeta_de_parches_de_ultima_version = new File("fci_input/parches/");
	public static File carpeta_de_parches_de_ultima_version_cliente = new File("fci_input/parches/cliente/");
	public static File carpeta_de_parches_de_ultima_version_servidor = new File("fci_input/parches/servidor/");
	public static File carpeta_de_parches_cliente = new File(
			App.config.get("output").asString() + "/" + App.version_de_juego + "/parches/cliente/");
	public static File carpeta_de_parches_servidor = new File(
			App.config.get("output").asString() + "/" + App.version_de_juego + "/parches/servidor/");

	/**
	 * Esta genera el proyecto para cliente y servidor.
	 */
	public static void generarProyectosJava() {
		// TODO actualizar para servidor y cliente

		PDMEMappings mappings_sin_banderas_accesos_cliente = new PDMEMappings();
		PDMEMappings banderas_accessos_cliente = new PDMEMappings();
		PDMEMappings mappings_sin_banderas_accesos_servidor = new PDMEMappings();
		PDMEMappings banderas_accessos_servidor = new PDMEMappings();
		if (App.is_pre_1_3) {
			Mappings fci_cliente = App.actualizidor_cliente.main;
			mappings_sin_banderas_accesos_cliente.classes = fci_cliente.getClasses();
			mappings_sin_banderas_accesos_cliente.defs = fci_cliente.getDefs();
			mappings_sin_banderas_accesos_cliente.vars = fci_cliente.getVars();
			mappings_sin_banderas_accesos_cliente.includes = fci_cliente.getIncludes();
			mappings_sin_banderas_accesos_cliente.refreshJVMClasses();
			mappings_sin_banderas_accesos_cliente.refreshReverse();
			banderas_accessos_cliente.access_flags = fci_cliente.getAccessFlags();
			banderas_accessos_cliente.refreshJVMClasses();
			banderas_accessos_cliente.refreshReverse();

			Mappings fci_servidor = App.actualizidor_servidor.main;
			mappings_sin_banderas_accesos_servidor.classes = fci_servidor.getClasses();
			mappings_sin_banderas_accesos_servidor.defs = fci_servidor.getDefs();
			mappings_sin_banderas_accesos_servidor.vars = fci_servidor.getVars();
			mappings_sin_banderas_accesos_servidor.includes = fci_servidor.getIncludes();
			mappings_sin_banderas_accesos_servidor.refreshJVMClasses();
			mappings_sin_banderas_accesos_servidor.refreshReverse();
			banderas_accessos_servidor.access_flags = fci_servidor.getAccessFlags();
			banderas_accessos_servidor.refreshJVMClasses();
			banderas_accessos_servidor.refreshReverse();
		} else {
			Mappings fci = App.actualizidor_1_3.main;
			mappings_sin_banderas_accesos_cliente.classes = fci.getClasses();
			mappings_sin_banderas_accesos_cliente.defs = fci.getDefs();
			mappings_sin_banderas_accesos_cliente.vars = fci.getVars();
			mappings_sin_banderas_accesos_cliente.includes = fci.getIncludes();
			mappings_sin_banderas_accesos_cliente.refreshJVMClasses();
			mappings_sin_banderas_accesos_cliente.refreshReverse();
			banderas_accessos_cliente.access_flags = fci.getAccessFlags();
			banderas_accessos_cliente.refreshJVMClasses();
			banderas_accessos_cliente.refreshReverse();
			mappings_sin_banderas_accesos_servidor = mappings_sin_banderas_accesos_cliente;
			banderas_accessos_servidor = banderas_accessos_cliente;
		}

		List<String> deps = descargarDeps();
		InputStream jar_del_cliente = App.cliente.getGameInputStream();
		InputStream jar_del_servidor = App.servidor.getGameInputStream();

		String classpath = construirClasspath(deps);
		File clientDir = obtainerCarpetaParaProyectosJavaClienteVainilla();
		File servidorDir = obtainerCarpetaParaProyectosJavaServidorVainilla();

		Options options_cliente = obtainerCFROptions(clientDir, classpath);
		Options options_servidor = obtainerCFROptions(servidorDir, classpath);


		try {
			File tempJarDeCliente = new File(System.getProperty("user.home") + "/.tmp/fci_gen/clienteTmp.jar");
			File tempJarDeServidor = new File(System.getProperty("user.home") + "/.tmp/fci_gen/servidorTmp.jar");

			if (tempJarDeCliente.exists()) {
				tempJarDeCliente.delete();
			} else {
				tempJarDeCliente.getParentFile().mkdirs();
				tempJarDeCliente.createNewFile();
			}

			
			
			if (tempJarDeServidor.exists()) {
				tempJarDeServidor.delete();
			} else {
				tempJarDeServidor.getParentFile().mkdirs();
				tempJarDeServidor.createNewFile();
			}

			
			tempJarDeCliente.deleteOnExit();
			tempJarDeServidor.deleteOnExit();

			// Guardar el InputStream en el archivo temporal
			try (FileOutputStream fos = new FileOutputStream(tempJarDeCliente)) {
				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = jar_del_cliente.read(buffer)) != -1) {
					fos.write(buffer, 0, bytesRead);
				}
			}
			
			// Guardar el InputStream en el archivo temporal
			try (FileOutputStream fos = new FileOutputStream(tempJarDeServidor)) {
				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = jar_del_servidor.read(buffer)) != -1) {
					fos.write(buffer, 0, bytesRead);
				}
			}
			

			cfr(tempJarDeCliente, options_cliente);
			cfr(tempJarDeServidor, options_servidor);


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String construirClasspath(List<String> deps) {
		return String.join(File.pathSeparator, deps);
	}

	public static Options obtainerCFROptions(File resulta_carpeta, String classpath) {
		Map<String, String> optionMap = new HashMap<>();
		optionMap.put("outputpath", resulta_carpeta.getAbsolutePath()+"/");
		optionMap.put("extraclasspath", classpath);
		optionMap.put("silent", "false"); // Desactivar salida estándar
		optionMap.put("clobber", "true");
		return new OptionsImpl(optionMap);
	}

	public static void cfr(File jar, Options options) {
		try {
			CfrDriver cfrDriver = new CfrDriver.Builder().withBuiltOptions(options).build();
			List<String> list = new ArrayList<String>();
			list.add(jar.getCanonicalPath());
			cfrDriver.analyse(list);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static List<String> descargarDeps() {
		// TODO Auto-generated method stub
		McPom pom;
		if(App.is_pre_1_3) {
			pom=App.cliente.getPom();
		}else {
			pom=App.obtainer.getPom();
		}
		
		
		List<String> jars = new ArrayList<String>();
		for (Dep dep : pom.deps) {
			String url = dep.url;
			String ubicacion = dep.path;
			if (url != null && ubicacion != null) {
				try {
					String ubicacion_completa = new File(
							McPom.getDefaultMavenDependenciesDirectory().getCanonicalPath(), ubicacion)
							.getCanonicalPath();
					InputStream stream = MappingsObtainerUtils.getFileInputStream(url);
					if (stream != null) {
						// guardar stream a ubicacion_completa

						File archivo_ubicacion_completa = new File(ubicacion_completa);
						archivo_ubicacion_completa.getParentFile().mkdirs();
						try (FileOutputStream fos = new FileOutputStream(archivo_ubicacion_completa)) {
							byte[] buffer = new byte[4096];
							int bytes;
							while ((bytes = stream.read(buffer)) != -1) {
								fos.write(buffer, 0, bytes);
							}
						}

						jars.add(ubicacion_completa);
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

		return jars;
	}

	/**
	 * La carpeta del proyecto para servidor.
	 * 
	 * @return
	 */
	public static File obtainerCarpetaParaProyectosJavaServidorProyecto() {
		try {
			String ubicacion_para_proyectos_java_str = obtainerCarpetaParaProyectosJava().getCanonicalPath();
			String ubicacion_para_proyectos_java_servidor_proyecto = ubicacion_para_proyectos_java_str
					+ "servidor_proyecto/";
			return new File(ubicacion_para_proyectos_java_servidor_proyecto);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * La carpeta de el codio del juego con parches de ultima version y Banderas de
	 * Accesos. Es
	 * obtainerCarpetaParaProyectosJava()+"servidor_con_parches_de_ultima_version/"
	 * 
	 * @return
	 */
	public static File obtainerCarpetaParaProyectosJavaServidorConParchesDeUltimaVersion() {
		try {
			String ubicacion_para_proyectos_java_str = obtainerCarpetaParaProyectosJava().getCanonicalPath();
			String ubicacion_para_proyectos_java_servidor_con_parches_de_ultima_version = ubicacion_para_proyectos_java_str
					+ "servidor_con_parches_de_ultima_version/";
			return new File(ubicacion_para_proyectos_java_servidor_con_parches_de_ultima_version);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * La carpeta de el codio del juego sin parches de ultima version o Banderas de
	 * Accesos. Es obtainerCarpetaParaProyectosJava()+"servidor/"
	 * 
	 * @return
	 */
	public static File obtainerCarpetaParaProyectosJavaServidorVainilla() {
		try {
			String ubicacion_para_proyectos_java_str = obtainerCarpetaParaProyectosJava().getCanonicalPath();
			String ubicacion_para_proyectos_java_servidor = ubicacion_para_proyectos_java_str + "servidor/";
			return new File(ubicacion_para_proyectos_java_servidor);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * La carpeta del proyecto para client.
	 * 
	 * @return
	 */
	public static File obtainerCarpetaParaProyectosJavaClienteProyecto() {
		try {
			String ubicacion_para_proyectos_java_str = obtainerCarpetaParaProyectosJava().getCanonicalPath();
			String ubicacion_para_proyectos_java_cliente_proyecto = ubicacion_para_proyectos_java_str
					+ "cliente_proyecto/";
			return new File(ubicacion_para_proyectos_java_cliente_proyecto);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * La carpeta de el codio del juego con parches de ultima version y Banderas de
	 * Accesos. Es
	 * obtainerCarpetaParaProyectosJava()+"cliente_con_parches_de_ultima_version/"
	 * 
	 * @return
	 */
	public static File obtainerCarpetaParaProyectosJavaClienteConParchesDeUltimaVersion() {
		try {
			String ubicacion_para_proyectos_java_str = obtainerCarpetaParaProyectosJava().getCanonicalPath();
			String ubicacion_para_proyectos_java_cliente_con_parches_de_ultima_version = ubicacion_para_proyectos_java_str
					+ "cliente_con_parches_de_ultima_version/";
			return new File(ubicacion_para_proyectos_java_cliente_con_parches_de_ultima_version);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * La carpeta de el codio del juego sin parches de ultima version o Banderas de
	 * Accesos. Es obtainerCarpetaParaProyectosJava()+"cliente/"
	 * 
	 * @return
	 */
	public static File obtainerCarpetaParaProyectosJavaClienteVainilla() {
		try {
			String ubicacion_para_proyectos_java_str = obtainerCarpetaParaProyectosJava().getCanonicalPath();
			App.logger.info(ubicacion_para_proyectos_java_str);
			String ubicacion_para_proyectos_java_cliente = ubicacion_para_proyectos_java_str + "cliente/";
			return new File(ubicacion_para_proyectos_java_cliente);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Esta es la carpeta donde las carpetas de los proyectos estan. El
	 * predeterminado es ~/fcigen/proyectos/ y la ubicacion completa donde la usador
	 * es cd'd en terminal. Si estoy cd'd en /home/rhel/Descargas/fci/ y mi versión
	 * fue 1.21.4 y homedir es /home/rhel/, la obtainerCarpetaParaProyectosJava es
	 * /home/rhel/fcigen/proyectos/home/rhel/Descargas/fci/1.21.4/
	 * 
	 * @return
	 */
	public static File obtainerCarpetaParaProyectosJava() {
		File esta_ubicacion = new File(App.version_de_juego + "/");
		try {
			String esta_ubicacion_str = esta_ubicacion.getCanonicalPath().replace(":", "");
			File ubicacion_para_proyectos_java = new File(System.getProperty("user.home") +"/fcigen/proyectos/" + esta_ubicacion_str);
			return ubicacion_para_proyectos_java;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
