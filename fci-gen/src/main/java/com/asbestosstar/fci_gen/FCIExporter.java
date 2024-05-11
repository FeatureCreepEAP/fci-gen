package com.asbestosstar.fci_gen;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import com.asbestosstar.assistremapper.Mappings;
import com.asbestosstar.assistremapper.mappings.PDMEMappings;

public class FCIExporter {

	public static void export() {
		App.logger.info("Exporting");
		try {
			String main_name;
			if(MappingsObtainer.isPre1() || MappingsObtainer.isNewerThanVersion("1.2.6")) {
				main_name = App.config.get("output").asString()+"/"+MappingsObtainer.version+"/" +"featurecreep-intermediary-"+MappingsObtainer.version+"-"+App.config.get("side").asString()+".pdme";
			}else {
				main_name = App.config.get("output").asString()+"/"+MappingsObtainer.version+"/" +"featurecreep-intermediary-"+MappingsObtainer.version+".pdme";
			}
			
			File example = new File(main_name);
			example.getParentFile().mkdirs();
			example.createNewFile();
			try (DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(main_name))) {
				FCIUpdater.main.write(dataOutputStream);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		//Most mappings are the same on both server and client but not all
		for (Map.Entry<String, Mappings> entry : MappingsObtainer.input.entrySet()) {
			String name = entry.getKey();
			Mappings to_conv = entry.getValue();
			Mappings ret = new PDMEMappings();
			Mappings.convert(FCIUpdater.main, to_conv, ret);
			
			String main_name = App.config.get("output").asString()+"/"+MappingsObtainer.version+"-"+name+"/" +"featurecreep-intermediary-"+MappingsObtainer.version+"-"+name+".pdme";

			try {
				File example = new File(main_name);
				example.getParentFile().mkdirs();
				example.createNewFile();
				try (DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(main_name))) {
					ret.write(dataOutputStream);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
		
		
	}
	
	
}
