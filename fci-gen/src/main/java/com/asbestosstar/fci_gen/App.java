package com.asbestosstar.fci_gen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

import com.asbestosstar.minecraftmappingsobtainer.MappingsObtainer;
import com.asbestosstar.minecraftmappingsobtainer.MinecraftSide;

public class App {

	public static Logger logger = Logger.getLogger("fci-gen", "FCIGenerator");
	public static ModelNode config = new ModelNode();
	public static int config_format = 1;
	public static ModelNode input_mappings = new ModelNode();
	public static MinecraftSide side;
	public static MappingsObtainer obtainer;
	public static List<String> idiomas = new ArrayList<String>();
	
	public static void main(String[] args) {
		File config_file = new File("config.dmr");
		if(!config_file.exists()) {
			config.get("fci-gen-config-format").set(config_format);
			config.get("game-version").set("1.21.4");
			config.get("side").set("client");
			config.get("unknowns_minimum").get("class").set(0);
			config.get("unknowns_minimum").get("def").set(0);
			config.get("unknowns_minimum").get("var").set(0);
			config.get("unknowns_minimum").get("params").set(0); //Yes we are doing param_unknown now 
			config.get("output").set("output/");
			config.get("idiomas").add("english");
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
		if(config.get("side").asString().equals("server")) {
		side=MinecraftSide.SERVER;
		}else {
		side=MinecraftSide.CLIENT;
		}
		
		if(config_format>config.get("fci-gen-config-format").asInt()) {
			logger.fatal("Obsolete Config, please backup and delete and restart program to get new config");
		}
		
		for(ModelNode node:config.get("idiomas").asListOrEmpty()) {
			idiomas.add(node.asString());
		}
		
		if(args[0].equals("--actualizarParches")) {
		File resultos =	new File(App.config.get("output").asString()+"/"+config.get("game-version").asString()+"/resultos.dmr");
			
		}else {
		obtainer = new MappingsObtainer(config.get("game-version").asString(),side);
		obtainer.input.remove("featurecreep-intermediary");
		GetInputFCI.getFCIInput();
		FCIUpdater.onUpdate();
		CSVGen.makeCSV();
		FCIExporter.export();
		
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
