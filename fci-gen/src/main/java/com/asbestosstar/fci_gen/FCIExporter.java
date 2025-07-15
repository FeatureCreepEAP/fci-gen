package com.asbestosstar.fci_gen;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import com.asbestosstar.assistremapper.Mappings;
import com.asbestosstar.assistremapper.mappings.PDMEMappings;
import com.asbestosstar.minecraftmappingsobtainer.MappingsObtainer;
import com.asbestosstar.minecraftmappingsobtainer.MinecraftSide;

public class FCIExporter {

	public MappingsObtainer obtainer;
	public FCIUpdater updater;
	
	public FCIExporter(MappingsObtainer obtainer,FCIUpdater updater) {
		this.obtainer=obtainer;
		this.updater=updater;
	}
	
	
	public void export() {
		App.logger.info("Exporting");
		try {
			String main_name;
			if(App.is_pre_1_3) {
				if(obtainer.side.equals(MinecraftSide.SERVER)) {
				main_name = App.config.get("output").asString()+"/"+obtainer.version+"/" +"featurecreep-intermediary-"+obtainer.version+"-server.pdme";
				}else {
					main_name = App.config.get("output").asString()+"/"+obtainer.version+"/" +"featurecreep-intermediary-"+obtainer.version+"-client.pdme";
				}
			}else {
				main_name = App.config.get("output").asString()+"/"+obtainer.version+"/" +"featurecreep-intermediary-"+obtainer.version+".pdme";
			}
			
			File example = new File(main_name);
			example.getParentFile().mkdirs();
			example.createNewFile();
			try (DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(main_name))) {
				updater.main.write(dataOutputStream);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		//Most mappings are the same on both server and client but not all
		for (Map.Entry<String, Mappings> entry : obtainer.input.entrySet()) {
			String name = entry.getKey();
			Mappings to_conv = entry.getValue();
			Mappings ret = new PDMEMappings();
			Mappings.convert(updater.main, to_conv, ret);
			
			String main_name = App.config.get("output").asString()+"/"+obtainer.version+"-"+name+"/" +"featurecreep-intermediary-"+obtainer.version+"-"+name+".pdme";

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
