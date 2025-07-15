package com.asbestosstar.fci_gen;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import com.asbestosstar.assistremapper.Mappings;
import com.asbestosstar.assistremapper.mappings.PDMEMappings;

/**
 * Esta clase solo actualizar los archivos de los otras idiomas. No se traduce
 * actualmente ninguin
 */
public class Traductor {

	public static void actualizeIdiomas() {
		// TODO Auto-generated method stub
		if(App.is_pre_1_3) {
			//TODO
		}else {
		
		App.actualizidor_1_3.main.parseSubClasses();
		App.actualizidor_1_3.main.refreshReverse();
		Mappings rev = App.actualizidor_1_3.main.getReverse(); //TODO actualizar para servidor y cliente
		for (String idioma : App.idiomas) {
			String main_name = App.config.get("output").asString() + "/" + idioma + "/" + idioma + ".pdme";

			try {
				File example = new File(main_name);
				Mappings mappings;
				if(example.exists()) {
					mappings = new PDMEMappings(new FileInputStream(example));
				}else {
				example.getParentFile().mkdirs();
				example.createNewFile();
				mappings = new PDMEMappings();
				}
				
				for(String clase:rev.getClasses().keySet()) {
					if(!mappings.getClasses().containsKey(clase)) {
						String sin_peso;
						if(clase.contains("$")) {
							String[] clz = clase.split("\\$");
							sin_peso = clz[clz.length-1];
						}else {
							sin_peso = clase;
						}
						mappings.getClasses().put(sin_peso, sin_peso);
					}
				}
				for(String var:rev.getVars().keySet()) {
					if(!mappings.getVars().containsKey(var)) {
						String pre = var.split("\\.")[var.split("\\.").length - 1].split(":")[0];
					if(!pre.contains("$")) {	//TODO, anadir mas excepciones
						mappings.getVars().put(var, pre);
					}
					}
				}
				
				for(String def:rev.getDefs().keySet()) {
					if(!mappings.getDefs().containsKey(def)) {
						String pre = def.split("\\.")[def.split("\\.").length - 1].split("\\(")[0];
						if(!pre.contains("$")&&!pre.equals("values")&&!pre.equals("valueOf")&&!pre.equals("<init>")&&!pre.equals("<cinit>")) {	//TODO, anadir mas excepciones
						mappings.getDefs().put(def, pre);
						}
						}
				}
				for(Map.Entry<String, String> param:rev.getParams().entrySet()) {
					if(!mappings.getParams().containsKey(param.getKey())) {
						mappings.getParams().put(param.getKey(), param.getValue());
					}
				}
				
				
				DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(main_name));
				mappings.write(dataOutputStream);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		}
	}

}
