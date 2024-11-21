package com.asbestosstar.fci_gen;

import com.asbestosstar.assistremapper.Mappings;
import com.asbestosstar.assistremapper.mappings.PDMEMappings;

public class ComboMaps {

	public static void generateCombos() {
	
		if(App.obtainer.input.containsKey("srg")&&App.obtainer.input.containsKey("sugarcane")) {
			if(App.obtainer.is_snapshot) {
				generateParchSRG();
			}else if(!App.obtainer.isNewerThanVersion("1.16.4") && App.obtainer.isNewerThanVersion("1.20.5")) {
				generateParchSRG();
			}
			
		}
		
		
		
		if(App.obtainer.input.containsKey("spigot")&&!App.obtainer.isNewerThanVersion("1.18")) {//I dont think they have for snapshots very much
			generateSpigotMoj();
		}
		
	}

	public static void generateSpigotMoj() {
		// TODO Auto-generated method stub
		App.logger.info("Getting SpigotMoj");
		Mappings spigot = App.obtainer.input.get("spigot");
		Mappings sugarcane = App.obtainer.input.get("sugarcane");
		Mappings spigotmoj = new PDMEMappings();
		spigotmoj.getClasses().putAll(spigot.getClasses());
		spigotmoj.getDefs().putAll(sugarcane.getDefs());
		spigotmoj.getVars().putAll(sugarcane.getVars());
		spigotmoj.getParams().putAll(sugarcane.getParams());//I dont think this is really needed but its fine i guesss
		spigotmoj.parseSubClasses();
		spigotmoj.refreshReverse();
		App.obtainer.input.put("spigotmoj", spigotmoj);
	}

	public static void generateParchSRG() {
		// TODO Auto-generated method stub
		App.logger.info("Getting ParchSRG");
		Mappings srg = App.obtainer.input.get("srg");
		Mappings sugarcane = App.obtainer.input.get("sugarcane");
		Mappings parchsrg = new PDMEMappings();
		parchsrg.getClasses().putAll(sugarcane.getClasses());
		parchsrg.getDefs().putAll(srg.getDefs());
		parchsrg.getVars().putAll(srg.getVars());
		parchsrg.getParams().putAll(sugarcane.getParams());//I dont think this is really needed but its fine i guesss
		parchsrg.parseSubClasses();
		parchsrg.refreshReverse();
		App.obtainer.input.put("parchsrg", parchsrg);
	}
	
	
	
	
	
	
	
	
	
}
