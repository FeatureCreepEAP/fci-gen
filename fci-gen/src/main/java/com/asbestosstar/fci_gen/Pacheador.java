package com.asbestosstar.fci_gen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.cadixdev.mercury.Mercury;
import org.cadixdev.mercury.SourceRewriter;
import org.cadixdev.mercury.remapper.MercuryRemapper;

import com.asbestosstar.assistremapper.Mappings;
import com.asbestosstar.assistremapper.mappings.PDMEMappings;
import com.asbestosstar.assistremapper.remapper.JarRemapper;
import com.asbestosstar.minecraftmappingsobtainer.MappingsObtainerUtils;
import com.asbestosstar.minecraftmappingsobtainer.McPom;
import com.asbestosstar.minecraftmappingsobtainer.McPom.Dep;

import io.codechicken.diffpatch.cli.DiffOperation;
import io.codechicken.diffpatch.cli.PatchOperation;
import io.codechicken.diffpatch.util.Input;
import io.codechicken.diffpatch.util.Input.FolderMultiInput;
import io.codechicken.diffpatch.util.Input.MultiInput;
import io.codechicken.diffpatch.util.Output;
import io.codechicken.diffpatch.util.Output.FolderMultiOutput;
import io.codechicken.diffpatch.util.Output.MultiOutput;
import io.codechicken.diffpatch.util.PatchMode;
import net.fabricmc.mappingio.MappingUtil;
import net.fabricmc.tinyremapper.IMappingProvider;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;
import net.fabricmc.tinyremapper.api.TrEnvironment;

public class Pacheador {

	// 12w30a Pre Release for 1.3 which combined server and client
	public static File carpeta_de_parches_de_ultima_version = new File("fci_input/parches/");
	public static File carpeta_de_parches_de_ultima_version_cliente = new File("fci_input/parches/cliente/");
	public static File carpeta_de_parches_de_ultima_version_servidor = new File("fci_input/parches/servidor/");
	public static File carpeta_de_parches_cliente = new File(
			App.config.get("output").asString() + "/" + App.version_de_juego + "/parches/espanol/cliente/");
	public static File carpeta_de_parches_servidor = new File(
			App.config.get("output").asString() + "/" + App.version_de_juego + "/parches/espanol/servidor/");// TODO
																												// config
																												// idioma

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

		// TODO hacer sin banderas acessos

		List<String> deps = descargarDeps();
		InputStream jar_del_cliente_obf = App.cliente.getGameInputStream();
		InputStream jar_del_servidor_obf = App.servidor.getGameInputStream();

		String classpath = construirClasspath(deps);
		File clientDir = obtainerCarpetaParaProyectosJavaClienteVainilla();
		File servidorDir = obtainerCarpetaParaProyectosJavaServidorVainilla();

		Options options_cliente = obtainerCFROptions(clientDir, classpath);
		Options options_servidor = obtainerCFROptions(servidorDir, classpath);

		try {

			File tempJarDeCliente = new File(System.getProperty("user.home") + "/.tmp/fci_gen/clienteTmpObf.jar");
			File tempJarDeServidor = new File(System.getProperty("user.home") + "/.tmp/fci_gen/servidorTmpObf.jar");

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
				while ((bytesRead = jar_del_cliente_obf.read(buffer)) != -1) {
					fos.write(buffer, 0, bytesRead);
				}
			}

			// Guardar el InputStream en el archivo temporal
			try (FileOutputStream fos = new FileOutputStream(tempJarDeServidor)) {
				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = jar_del_servidor_obf.read(buffer)) != -1) {
					fos.write(buffer, 0, bytesRead);
				}
			}

			File tempCarpetaDeRemapperDeCliente = new File(
					System.getProperty("user.home") + "/.tmp/fci_gen/remap/cliente/");
			File tempCarpetaDeRemapperDeServidor = new File(
					System.getProperty("user.home") + "/.tmp/fci_gen/remap/servidor/");
			JarRemapper remapper_cliente = new JarRemapper(mappings_sin_banderas_accesos_cliente,
					tempCarpetaDeRemapperDeCliente.getCanonicalPath());
			JarRemapper remapper_servidor = new JarRemapper(mappings_sin_banderas_accesos_servidor,
					tempCarpetaDeRemapperDeServidor.getCanonicalPath());

			remapper_cliente.remapJar(new JarFile(tempJarDeCliente));
			remapper_servidor.remapJar(new JarFile(tempJarDeServidor));

			File fci_jar_cliente = new File(System.getProperty("user.home") + "/.tmp/fci_gen/clienteTmp.jar");
			File fci_jar_servidor = new File(System.getProperty("user.home") + "/.tmp/fci_gen/servidorTmp.jar");

			// Necesita zip todos los archivos en tempCarpetaDeRemapperDeCliente a
			// fci_jar_client y fci_jar_servidor a fci_jar_servidor y eliminar las carpetas

			// Comprimir la carpeta del cliente en el archivo JAR correspondiente
			comprimirCarpeta(tempCarpetaDeRemapperDeCliente, fci_jar_cliente);
			// Comprimir la carpeta del servidor en el archivo JAR correspondiente
			comprimirCarpeta(tempCarpetaDeRemapperDeServidor, fci_jar_servidor);

			// Eliminar las carpetas temporales después de comprimirlas
			eliminarCarpeta(tempCarpetaDeRemapperDeCliente);
			eliminarCarpeta(tempCarpetaDeRemapperDeServidor);

			cfr(fci_jar_cliente, options_cliente);
			cfr(fci_jar_servidor, options_servidor);

			File dirConParchesCliente = obtainerCarpetaParaProyectosJavaClienteConParchesDeUltimaVersion();
			File dirProyectoCliente = obtainerCarpetaParaProyectosJavaClienteProyecto();
			eliminarCarpeta(dirConParchesCliente); // Eliminar contenido existente
			eliminarCarpeta(dirProyectoCliente); // Eliminar contenido existente
			dirConParchesCliente.mkdirs();
			dirProyectoCliente.mkdirs();
			File dirConParchesServidor = obtainerCarpetaParaProyectosJavaServidorConParchesDeUltimaVersion();
			File dirProyectoServidor = obtainerCarpetaParaProyectosJavaServidorProyecto();
			eliminarCarpeta(dirConParchesServidor); // Eliminar contenido existente
			eliminarCarpeta(dirProyectoServidor); // Eliminar contenido existente
			dirConParchesServidor.mkdirs();
			dirProyectoServidor.mkdirs();

			procesarParchesParaProyecto(clientDir, carpeta_de_parches_de_ultima_version_cliente, dirConParchesCliente);

			procesarParchesParaProyecto(servidorDir, carpeta_de_parches_de_ultima_version_servidor,
					obtainerCarpetaParaProyectosJavaServidorConParchesDeUltimaVersion());

			copiarDirectorio(dirConParchesCliente, dirProyectoCliente);
			copiarDirectorio(dirConParchesServidor, dirProyectoServidor);

			String pom_cliente = App.cliente.getPom().toString();
			String pom_servidor = App.servidor.getPom().toString();
			FileWriter escribir_pom_cliente = new FileWriter(dirProyectoCliente.getCanonicalPath()+"/../../../pom.xml");
			FileWriter escribir_pom_servidor = new FileWriter(dirProyectoServidor.getCanonicalPath()+"/../../../pom.xml");
			escribir_pom_cliente.write(pom_cliente);
			escribir_pom_servidor.write(pom_servidor);
			escribir_pom_cliente.close();
			escribir_pom_servidor.close();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Remap las parches a otras Mappings.
	 * 
	 * @return Mapa: Vanilla En Mappings, Parches con Mappings, Carpeta para
	 *         escribir los parches
	 */
	public static BiMap<File, File, File> remapParchesCliente() {
		BiMap<File, File, File> resultdos = new BiMap<>();
		try {
			File resultdosCarp = new File(App.config.get("output").asString() + "/");
			String ver = App.version_de_juego;
			for (File arc : resultdosCarp.listFiles()) {
				if (arc.isDirectory() && arc.getName().startsWith(ver + "-")) {

					for (File pdme : arc.listFiles()) {
						if (pdme.getName().endsWith(".pdme")) {
							String nombre = pdme.getName().replace(".pdme", "").split(Pattern.quote(ver + "-"))[1];
							BufferedReader leer = new BufferedReader(new FileReader(pdme));
							IMappingProvider prov = TinyUtils.createTinyMappingProvider(leer,
									MappingUtil.NS_TARGET_FALLBACK,  MappingUtil.NS_SOURCE_FALLBACK);
//							IMappingProvider prov = TinyUtils.createTinyMappingProvider(leer,
//									MappingUtil.NS_SOURCE_FALLBACK, MappingUtil.NS_TARGET_FALLBACK);
							TinyRemapper tiny = TinyRemapper.newRemapper().threads(8)// TODO Config
									.withMappings(prov).build();

							TrEnvironment env = tiny.getEnvironment();
							SourceRewriter proc = MercuryRemapper.create(env);

							File original = new File(
									obtainerCarpetaParaProyectosJavaClienteVainilla().getCanonicalPath() + "-" + nombre
											+ "/");
							File parchado = new File(
									obtainerCarpetaParaProyectosJavaClienteProyecto().getCanonicalPath() + "-" + nombre
											+ "/");
							eliminarCarpeta(parchado);
							Mercury mercury = new Mercury();
							mercury.getProcessors().add(proc);
							mercury.rewrite(original.toPath(), parchado.toPath());

							File ub_parches;
							if (App.idiomas.contains(arc.getName())) {
								ub_parches = new File(App.config.get("output").asString() + "/" + App.version_de_juego
										+ "/parches/" + arc.getName() + "/cliente/");
							} else {
								ub_parches = new File(arc.getCanonicalPath() + "/parches/cliente/");
							}

							resultdos.poner(original, parchado, ub_parches);
						}
					}

				}

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return resultdos;

	}

	/**
	 * Remap las parches a otras Mappings.
	 * 
	 * @return Mapa: Vanilla En Mappings, Parches con Mappings, Carpeta para
	 *         escribir los parches
	 */
	public static BiMap<File, File, File> remapParchesServidor() {
		BiMap<File, File, File> resultdos = new BiMap<>();
		try {
			File resultdosCarp = new File(App.config.get("output").asString() + "/");
			String ver = App.version_de_juego;
			for (File arc : resultdosCarp.listFiles()) {
				if (arc.isDirectory() && arc.getName().startsWith(ver + "-")) {

					for (File pdme : arc.listFiles()) {
						if (pdme.getName().endsWith(".pdme")) {
							String nombre = pdme.getName().replace(".pdme", "").split(Pattern.quote(ver + "-"))[1];
							BufferedReader leer = new BufferedReader(new FileReader(pdme));
							IMappingProvider prov = TinyUtils.createTinyMappingProvider(leer,
									MappingUtil.NS_SOURCE_FALLBACK, MappingUtil.NS_TARGET_FALLBACK);
							TinyRemapper tiny = TinyRemapper.newRemapper().threads(8)// TODO Config
									.withMappings(prov).build();

							TrEnvironment env = tiny.getEnvironment();
							SourceRewriter proc = MercuryRemapper.create(env);

							File original = new File(
									obtainerCarpetaParaProyectosJavaServidorVainilla().getCanonicalPath() + "-" + nombre
											+ "/");
							File parchado = new File(
									obtainerCarpetaParaProyectosJavaServidorProyecto().getCanonicalPath() + "-" + nombre
											+ "/");
							eliminarCarpeta(parchado);
							Mercury mercury = new Mercury();
							mercury.getProcessors().add(proc);
							mercury.rewrite(original.toPath(), parchado.toPath());

							File ub_parches;
							if (App.idiomas.contains(arc.getName())) {
								ub_parches = new File(App.config.get("output").asString() + "/" + App.version_de_juego
										+ "/parches/" + arc.getName() + "/servidor/");
							} else {
								ub_parches = new File(arc.getCanonicalPath() + "/parches/servidor/");
							}

							resultdos.poner(original, parchado, ub_parches);
						}
					}

				}

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return resultdos;

	}

	public static void generarParches() {
		// Cliente
		File vainillaCliente = obtainerCarpetaParaProyectosJavaClienteVainilla();
		File proyectoCliente = obtainerCarpetaParaProyectosJavaClienteProyecto();

		// Servidor
		File vainillaServidor = obtainerCarpetaParaProyectosJavaServidorVainilla();
		File proyectoServidor = obtainerCarpetaParaProyectosJavaServidorProyecto();

		// Generar parches
		generarParchesDesdeDirectorios(vainillaCliente, proyectoCliente, carpeta_de_parches_cliente);
		generarParchesDesdeDirectorios(vainillaServidor, proyectoServidor, carpeta_de_parches_servidor);

		remapParchesServidor().iterator().forEachRemaining((entry) -> {
			generarParchesDesdeDirectorios(entry.getClave(), entry.getValores().getValor1(),
					entry.getValores().getValor2());
		});

		remapParchesCliente().iterator().forEachRemaining((entry) -> {
			generarParchesDesdeDirectorios(entry.getClave(), entry.getValores().getValor1(),
					entry.getValores().getValor2());
		});

	}

	public static void generarParchesDesdeDirectorios(File original, File modificado, File salida) {
		try (MultiInput originalInput = new FolderMultiInput(original.toPath());
				MultiInput modifiedInput = new FolderMultiInput(modificado.toPath())) {
			MultiOutput output = new FolderMultiOutput(salida.toPath());
			DiffOperation diff = DiffOperation.builder().summary(true).baseInput(originalInput)
					.changedInput(modifiedInput).patchesOutput(output).build();

			diff.operate();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String construirClasspath(List<String> deps) {
		return String.join(File.pathSeparator, deps);
	}

	public static Options obtainerCFROptions(File resulta_carpeta, String classpath) {
		Map<String, String> optionMap = new HashMap<>();
		optionMap.put("outputpath", resulta_carpeta.getAbsolutePath() + "/");
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
		if (App.is_pre_1_3) {
			pom = App.cliente.getPom();
		} else {
			pom = App.obtainer.getPom();
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
					+ "servidor_proyecto/src/main/java/";
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
					+ "servidor_con_parches_de_ultima_version/src/main/java/";
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
			String ubicacion_para_proyectos_java_servidor = ubicacion_para_proyectos_java_str + "servidor/src/main/java/";
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
					+ "cliente_proyecto/src/main/java/";
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
					+ "cliente_con_parches_de_ultima_version/src/main/java/";
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
			String ubicacion_para_proyectos_java_cliente = ubicacion_para_proyectos_java_str + "cliente/src/main/java/";
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
			File ubicacion_para_proyectos_java = new File(
					System.getProperty("user.home") + "/fcigen/proyectos/" + esta_ubicacion_str);
			return ubicacion_para_proyectos_java;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	// Método auxiliar para procesar archivos y subcarpetas recursivamente
	private static void procesarCarpeta(File carpetaRaiz, File carpetaActual, ZipOutputStream zos) throws IOException {
		for (File archivo : carpetaActual.listFiles()) {
			if (archivo.isDirectory()) {
				// Si es una subcarpeta, procesarla recursivamente
				procesarCarpeta(carpetaRaiz, archivo, zos);
			} else {
				// Si es un archivo, agregarlo al archivo ZIP/JAR
				try (FileInputStream fis = new FileInputStream(archivo)) {
					String rutaRelativa = obtenerRutaRelativa(carpetaRaiz, archivo);
					ZipEntry entradaZip = new ZipEntry(rutaRelativa);
					zos.putNextEntry(entradaZip);

					byte[] buffer = new byte[4096];
					int bytesLeidos;
					while ((bytesLeidos = fis.read(buffer)) != -1) {
						zos.write(buffer, 0, bytesLeidos);
					}

					zos.closeEntry();
				}
			}
		}
	}

	// Método para obtener la ruta relativa de un archivo respecto a una carpeta
	// raíz
	private static String obtenerRutaRelativa(File carpetaRaiz, File archivo) {
		return carpetaRaiz.toURI().relativize(archivo.toURI()).getPath();
	}

	// Método para eliminar una carpeta y su contenido
	private static void eliminarCarpeta(File carpeta) {
		if (carpeta.isDirectory()) {
			for (File archivo : carpeta.listFiles()) {
				eliminarCarpeta(archivo); // Eliminar archivos o subcarpetas recursivamente
			}
		}
		carpeta.delete(); // Eliminar el archivo o carpeta actual
	}

	// Método para comprimir una carpeta en un archivo ZIP/JAR
	private static void comprimirCarpeta(File carpeta, File archivoZipDestino) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(archivoZipDestino);
				ZipOutputStream zos = new ZipOutputStream(fos)) {

			// Procesar todos los archivos y subcarpetas dentro de la carpeta
			procesarCarpeta(carpeta, carpeta, zos);
		}
	}

	// Método para procesar parches en un proyecto
	private static void procesarParchesParaProyecto(File dirVanilla, File parchesDir, File dirConParches) {
		try {
			// Eliminar directorio de parches anterior si existe
			if (dirConParches.exists()) {
				eliminarCarpeta(dirConParches);
			}

			// Verificar si hay parches
			if (tieneParches(parchesDir)) {
				// Aplicar parches usando DiffPatch
				aplicarParches(dirVanilla, parchesDir, dirConParches);
			} else {
				// Si no hay parches, crear enlace simbólico o copiar
				crearEnlaceSimbolico(dirVanilla, dirConParches);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Verificar si hay parches en el directorio
	private static boolean tieneParches(File parchesDir) {
		if (!parchesDir.exists())
			return false;
		File[] parches = parchesDir.listFiles((d, n) -> n.endsWith(".patch"));
		return parches != null && parches.length > 0;
	}

	// Método para aplicar parches usando DiffPatch
	private static void aplicarParches(File dirOrigen, File parchesDir, File dirDestino) throws IOException {
		dirDestino.mkdirs();

		// Configurar entradas/salidas según tipo de datos
		Input baseInput = new FolderMultiInput(dirOrigen.toPath());
		Input patchesInput = new FolderMultiInput(parchesDir.toPath());
		Output patchedOutput = new FolderMultiOutput(dirDestino.toPath());

		// Crear operación de parcheo
		PatchOperation operation = PatchOperation.builder().baseInput(baseInput).patchesInput(patchesInput)
				.patchedOutput(patchedOutput).minFuzz(0.5f) // Coincidencia mínima
				.maxOffset(100) // Máximo desplazamiento
				.mode(PatchMode.FUZZY).build();

		// Ejecutar parcheo
		operation.operate().summary.print(System.out, false);
	}

	// Crear enlace simbólico o copiar si falla
	private static void crearEnlaceSimbolico(File origen, File destino) throws IOException {
		try {
			Files.createSymbolicLink(destino.toPath(), origen.toPath());
		} catch (UnsupportedOperationException | FileSystemException e) {
			// Fallback a copia si no se pueden crear enlaces
			copiarDirectorio(origen, destino);
		}
	}

	// Copiar directorio recursivamente
	private static void copiarDirectorio(File origen, File destino) {
		if (origen.isDirectory()) {
			if (!destino.exists())
				destino.mkdirs();
			for (String child : origen.list()) {
				copiarDirectorio(new File(origen, child), new File(destino, child));
			}
		} else {
			try (FileInputStream in = new FileInputStream(origen);
					FileOutputStream out = new FileOutputStream(destino)) {
				byte[] buffer = new byte[4096];
				int length;
				while ((length = in.read(buffer)) > 0) {
					out.write(buffer, 0, length);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
