package com.asbestosstar.fci_gen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
			App.config.get("output").asString() + "/" + App.config.get("version") + "/parches/cliente/");
	public static File carpeta_de_parches_servidor = new File(
			App.config.get("output").asString() + "/" + App.config.get("version") + "/parches/servidor/");

	/**
	 * Esta genera el proyecto para cliente y servidor.
	 */
	public static void generarProyectosJava() {

		Mappings fci = FCIUpdater.main;
		PDMEMappings mappings_sin_banderas_accesos = new PDMEMappings();
		PDMEMappings banderas_accessos = new PDMEMappings();
		mappings_sin_banderas_accesos.classes = fci.getClasses();
		mappings_sin_banderas_accesos.defs = fci.getDefs();
		mappings_sin_banderas_accesos.vars = fci.getVars();
		mappings_sin_banderas_accesos.includes = fci.getIncludes();
		mappings_sin_banderas_accesos.refreshJVMClasses();
		mappings_sin_banderas_accesos.refreshReverse();
		banderas_accessos.access_flags = fci.getAccessFlags();
		banderas_accessos.refreshJVMClasses();
		banderas_accessos.refreshReverse();
		List<String> deps = descargarDeps();
		
		
		
		
	}

	public static List<String> descargarDeps() {
		// TODO Auto-generated method stub
		McPom pom = App.obtainer.getPom();
		List<String> jars = new ArrayList<String>();
		for (Dep dep : pom.deps) {
			String url = dep.url;
			String ubicacion = dep.path;
			if (url != null && ubicacion != null) {
				try {
					String ubicacion_completa = new File(McPom.getDefaultMavenDependenciesDirectory().getCanonicalPath(),
							ubicacion).getCanonicalPath();
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
		File esta_ubicacion = new File(App.config.get("version") + "/");
		try {
			String esta_ubicacion_str = esta_ubicacion.getCanonicalPath();
			File ubicacion_para_proyectos_java = new File("~/fcigen/proyectos/" + esta_ubicacion_str);
			return ubicacion_para_proyectos_java;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
