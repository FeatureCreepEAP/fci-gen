package com.asbestosstar.fci_gen;

import java.io.File;
import java.io.IOException;

import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

public class App {

	public static Logger logger = Logger.getLogger("fci-gen", "FCIGenerator");
	public static ModelNode config = new ModelNode();
	public static int config_format = 0;
	public static ModelNode input_mappings = new ModelNode();
	
	public static void main(String[] args) {
		File config_file = new File("config.dmr");
		if(!config_file.exists()) {
			config.get("fci-gen-config-format").set(0);
			config.get("game-version").set("1.20.5");
			config.get("side").set("client");
			config.get("unknowns_minimum").get("class").set(0);
			config.get("unknowns_minimum").get("def").set(0);
			config.get("unknowns_minimum").get("var").set(0);
			config.get("unknowns_minimum").add("params").set(0); //Yes we are doing param_unknown now 
			config.get("side").set("output/");
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
		
		MappingsObtainer.getInput();
		MappingsObtainer.getFCIInput();
		FCIUpdater.onUpdate();
		CSVGen.makeCSV();
		ComboMaps.generateCombos();
		FCIExporter.export();
		
		String output = config.toString();
		FCIGenUtils.writeStringToFile(output, config_file);
		
	}
	
	
}
