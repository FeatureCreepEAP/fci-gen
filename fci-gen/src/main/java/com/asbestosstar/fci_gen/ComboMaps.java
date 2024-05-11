package com.asbestosstar.fci_gen;

import com.asbestosstar.assistremapper.Mappings;
import com.asbestosstar.assistremapper.mappings.PDMEMappings;

public class ComboMaps {

	public static void generateCombos() {
	
		if(MappingsObtainer.input.containsKey("srg")&&MappingsObtainer.input.containsKey("sugarcane")) {
			if(MappingsObtainer.is_snapshot) {
				generateParchSRG();
			}else if(!MappingsObtainer.isNewerThanVersion("1.16.4") && MappingsObtainer.isNewerThanVersion("1.20.5")) {
				generateParchSRG();
			}
			
		}
		
		
		
		if(MappingsObtainer.input.containsKey("spigot")&&!MappingsObtainer.isNewerThanVersion("1.18")) {//I dont think they have for snapshots very much
			generateSpigotMoj();
		}
		
	}

	public static void generateSpigotMoj() {
		// TODO Auto-generated method stub
		App.logger.info("Getting SpigotMoj");
		Mappings spigot = MappingsObtainer.input.get("spigot");
		Mappings sugarcane = MappingsObtainer.input.get("sugarcane");
		Mappings spigotmoj = new PDMEMappings();
		spigotmoj.getClasses().putAll(spigot.getClasses());
		spigotmoj.getDefs().putAll(sugarcane.getDefs());
		spigotmoj.getVars().putAll(sugarcane.getVars());
		spigotmoj.getParams().putAll(sugarcane.getParams());//I dont think this is really needed but its fine i guesss
		spigotmoj.parseSubClasses();
		spigotmoj.refreshReverse();
		MappingsObtainer.input.put("spigotmoj", spigotmoj);
	}

	public static void generateParchSRG() {
		// TODO Auto-generated method stub
		App.logger.info("Getting ParchSRG");
		Mappings srg = MappingsObtainer.input.get("srg");
		Mappings sugarcane = MappingsObtainer.input.get("sugarcane");
		Mappings parchsrg = new PDMEMappings();
		parchsrg.getClasses().putAll(sugarcane.getClasses());
		parchsrg.getDefs().putAll(srg.getDefs());
		parchsrg.getVars().putAll(srg.getVars());
		parchsrg.getParams().putAll(sugarcane.getParams());//I dont think this is really needed but its fine i guesss
		parchsrg.parseSubClasses();
		parchsrg.refreshReverse();
		MappingsObtainer.input.put("parchsrg", parchsrg);
	}
	
	
	
	
	
	
	
	
	
}
