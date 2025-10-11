package com.asbestosstar.fci_gen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

import com.asbestosstar.minecraftmappingsobtainer.MappingConverter;
import com.asbestosstar.minecraftmappingsobtainer.MappingsObtainer;
import com.asbestosstar.minecraftmappingsobtainer.MinecraftSide;
import com.asbestosstar.minecraftmappingsobtainer.VersionUtils;

public class App {

	public static Logger logger = Logger.getLogger("fci-gen", "FCIGenerator");
	public static ModelNode config = new ModelNode();
	public static int config_format = 1;
	public static ModelNode input_mappings = new ModelNode();
	//public static MinecraftSide side;
	public static MappingsObtainer cliente;
	public static MappingsObtainer servidor;
	public static MappingsObtainer obtainer;// SOLO PARA 1.3+
	public static FCIUpdater actualizidor_1_3;// SOLO PARA 1.3+
	public static FCIUpdater actualizidor_cliente;// SOLO PARA VERSIONES ANTES DE 1,3
	public static FCIUpdater actualizidor_servidor;// SOLO PARA VERSIONES ANTES DE 1,3

	public static List<String> idiomas = new ArrayList<String>();
	public static String version_de_juego;
	public static boolean is_pre_1_3;
	public static MappingConverter convertidor;
	public Map<String,String> cliente_servidor = new HashMap<>();

	
	public static void main(String[] args) {
		File config_file = new File("config.dmr");
		if(!config_file.exists()) {
			config.get("fci-gen-config-format").set(config_format);
			config.get("game-version").set("1.21.4");
			//config.get("side").set("client");
			config.get("unknowns_minimum").get("class").set(0);
			config.get("unknowns_minimum").get("def").set(0);
			config.get("unknowns_minimum").get("var").set(0);
			config.get("unknowns_minimum").get("params").set(0); //Yes we are doing param_unknown now 
			config.get("output").set("output/");
			config.get("idiomas").add("english");
			config.get("habilitar-generador-de-parches").set("false"); //es WIP y solo para minecraft. Recaf es mas buena
			String output = config.toString();
			FCIGenUtils.writeStringToFile(output, config_file);
			logger.fatal("Config Generated, please update config.dmr");
			System.exit(0);
		}
		
		
		
		try {
			config = ModelNode.fromString(FCIGenUtils.readFileToString(config_file.getCanonicalPath()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		

		
		if(config_format>config.get("fci-gen-config-format").asInt()) {
			logger.fatal("Obsolete Config, please backup and delete and restart program to get new config");
		}
		
		for(ModelNode node:config.get("idiomas").asListOrEmpty()) {
			idiomas.add(node.asString());
		}
		
		
		version_de_juego = config.get("game-version").asString();
		
		if(args.length > 0 && args[0].equals("--actualizarParches")) {
		File resultos =	new File(App.config.get("output").asString()+"/"+config.get("game-version").asString()+"/resultos.dmr");
			Pacheador.generarParches();
		}else {
			is_pre_1_3=VersionUtils.isVersionLessThan1_3_12w30a(version_de_juego);
			
			
			
			if(is_pre_1_3) {
				//TODO
				logger.info("pre 1.3");
				convertidor = new MappingConverter(version_de_juego);
				cliente= convertidor.client;
				servidor= convertidor.server;

				
			}else {
				logger.info("1.3+");
				obtainer = new MappingsObtainer(version_de_juego,MinecraftSide.COMMON_1_3);
				obtainer.input.remove("featurecreep-intermediary");
				cliente = new MappingsObtainer(version_de_juego,MinecraftSide.CLIENT,false);
				servidor = new MappingsObtainer(version_de_juego,MinecraftSide.SERVER,false);
				
				actualizidor_1_3=new FCIUpdater(obtainer);
				GetInputFCI.getFCIInput();
				App.logger.info("Obtener Input Completa");
				actualizidor_1_3.onUpdate();
				App.logger.info("Actualizidor Completa");
				new CSVGen(obtainer, actualizidor_1_3, null).makeCSV();
				new FCIExporter(obtainer, actualizidor_1_3).export();
			if(config.get("habilitar-generador-de-parches").asBoolean())	{
				Pacheador.generarProyectosJava();
			}
			
			}

			
			
		
		String output = config.toString();
		FCIGenUtils.writeStringToFile(output, config_file);
		
		Traductor.actualizeIdiomas();
		
		ModelNode resultos = new ModelNode();
		for(String lleva:obtainer.input.keySet()) {
			resultos.get("mappings").add(lleva);
		}
		for(String idioma:idiomas) {
			resultos.get("idiomas").add(idioma);
		}
		FCIGenUtils.writeStringToFile(resultos.asString(), new File(App.config.get("output").asString()+"/"+App.obtainer.version+"/resultos.dmr"));
		}
		
	}
	
	
}
