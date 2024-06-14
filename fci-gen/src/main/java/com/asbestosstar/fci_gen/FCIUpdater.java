package com.asbestosstar.fci_gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.asbestosstar.assistremapper.Mappings;
import com.asbestosstar.assistremapper.mappings.PDMEMappings;
import com.asbestosstar.structurelib.StructureLib;

import javassist.CtClass;
import javassist.bytecode.Descriptor;

public class FCIUpdater {

	public static String[] denylisted_prefixes = new String[] { "io.netty", "net.minecraftforge", "net.fabricmc", "org.apache",
			"com.sun", "com.mojang","com.google","tv.twitch", "ow2.", "ca.weblite", "it.unimi", "net.java.", "gnu.trove.", "javax.","jopt.", "oshi.","joptsimple","commons-io","org.joml","org.slf4j","com.github"};
	public static Mappings main = new PDMEMappings();
	public static StructureLib sl = new StructureLib();
	public static ArrayList<String> done_classes = new ArrayList<String>();
	public static ArrayList<String> vars_y_defs_completa = new ArrayList<String>();
	public static int class_index = 0;
	public static int def_index = 0;
	public static int var_index = 0;
	public static int arg_index = 0;

	public static void onUpdate() {
		sl.addJar(MappingsObtainer.game_jar);
		class_index = App.config.get("unknowns_minimum").get("class").asInt();
		def_index = App.config.get("unknowns_minimum").get("def").asInt();
		var_index = App.config.get("unknowns_minimum").get("var").asInt();
		arg_index = App.config.get("unknowns_minimum").get("params").asInt();

		App.logger.info("Updating Vars");

		for (String var : sl.getOldestDeclaredFields()) {

			if (!isDenyListed(var)) {
				String ret_var = getVar(var);
				if (ret_var != null) {
					main.addVar(var, ret_var);
				}
			}
		}

		App.logger.info("Updating Defs");

		for (String def : sl.getOldestDeclaredMethods()) {
			
			if(def.contains("aed.a(Lvg;)V")) {
				System.out.println(def);
			}
			
			if (!isDenyListed(def)) {
				
				String ret_def = getDef(def);
				if (ret_def != null) { // Return null for suspected not obfuscated
					main.addDef(def, ret_def);
				}
				String[] params = getParams(def);
				int par = 0;
				for (String para : params) {
					par++;
					main.addParam(def + "@" + par, para);
				}

			}
		}

		App.logger.info("Updating Classes");
		for (CtClass clazz : sl.clazzes) {
			String clazz_name = clazz.getName();
			if (!isDenyListed(clazz_name)) {
				String actualizado = getClass(clazz_name);
				if(actualizado!=null) {
				main.addClass(clazz_name, actualizado);
				}
				List<String> incls = getIncludes(clazz_name);
				if (!incls.isEmpty()) {
					String incl = String.join(",", incls);
					main.getIncludes().put(clazz_name, incl.split(","));//todo, just make normal arrlist to arr converter
				}
			}
		}
		
		App.config.get("unknowns_minimum").get("class").set(class_index);
		App.config.get("unknowns_minimum").get("def").set(def_index);
		App.config.get("unknowns_minimum").get("var").set(var_index);
		App.config.get("unknowns_minimum").get("params").set(arg_index);
		
		
	}

	public static boolean isDenyListed(String test) {
		for (String preix : denylisted_prefixes) {
			if (test.startsWith(preix)) {			
				return true;
			}

		}

		
		String lower = test.toLowerCase();
		if(lower.contains("compare(")||lower.contains("compareto(ljava/lang/object;)i")||lower.contains("notify(")||lower.contains("getClass()")||lower.contains("finalize()")||lower.contains("wait()")||lower.contains("tostring()")||lower.contains("equals(ljava/lang/object;)")||lower.contains("call()ljava/lang/object;")||lower.contains("hashcode()")||lower.contains("clear()V")||lower.contains("close()V")||lower.contains("value(")||lower.contains("valueof(")||lower.contains("values(")) {
			return true;
		}
		
		
		return false;

	}

	public static String getVar(String var) {
		// TODO Auto-generated method stub

		if(varHasMaps(var)) {
			
			
		
		boolean is_in_input = false;
		String[] old_class_arr = java.util.Arrays.copyOfRange(var.split("\\."), 0, var.split("\\.").length - 1);
		String old_classname = String.join(".", old_class_arr);
		String old_desc = var.split(":")[1];
		String old_name = var.split(":")[0].split("\\.")[var.split("\\.").length - 1];

		if(old_name.length()>2) {
			return old_name;
		}
		
		
		for (Map.Entry<String, Mappings> entry : MappingsObtainer.input_fcis.entrySet()) {
			String type = entry.getKey();
			Mappings input_fci = entry.getValue();
			Mappings maps = MappingsObtainer.input.get(type);
			if(type.equals("obf")) {//Need to adjust this for servers and clients
				if(input_fci.getVars().containsKey(var)) {
					return input_fci.getVars().get(var);
				}
			}
			
			if (maps != null) {
				if (maps.getVars().containsKey(var)) {
					is_in_input = true;
					String int_value = maps.getVars().get(var);

					String new_classname = maps.getClassMappedName(old_classname);

					String desc = maps.renameClassesInFieldDescriptor(old_desc);

					String new_key = new_classname + "." + int_value + ":" + desc;

					if (input_fci.getVars().containsKey(new_key)) {
						String ret = input_fci.getVars().get(new_key);
						if (ret.equals(int_value) && ret.equals(old_name)) {
							return null;
						}

						if (notConflicting(old_classname+":"+old_desc+"@"+ret)) {
							return ret;
						}
						
					}

					if (int_value.equals(old_name)) {
						return null;
					}

				}

			}

		}

	//	if (!is_in_input ){//|| MappingsObtainer.input.size() == 0) {// We need to make this number configable and rework this in general
			String out = "var_unknown_" + var_index + "_";// We did not have the leading _ before but we do now because
															// we use find and replace and without it will cut longer
															// ones when you do a shorter one
			var_index++;
			return out;
	//	} else {
	//		return null;// Likely not obfuscated
	//	} This has been causing issues, we will reenable it later

		}else {
				return null;
			}
		
		
		}

	public static String getDef(String def) {
		// TODO Auto-generated method stub

		if(def.contains("aed.a(Lvg;)V")) {
			System.out.println(def);
		}
		
		if(defHasMaps(def)) {
		boolean is_in_input = false;
		String[] old_class_arr = java.util.Arrays.copyOfRange(def.split("\\."), 0, def.split("\\.").length - 1);
		String old_classname = String.join(".", old_class_arr);
		String old_desc = "(" + def.split("\\(")[1];
		String old_name = def.split("\\(")[0].split("\\.")[def.split("\\.").length - 1];

		if(old_name.length()>2&&!old_name.endsWith("_")) {
			return old_name;
		}
		
		
		for (Map.Entry<String, Mappings> entry : MappingsObtainer.input_fcis.entrySet()) {
			String type = entry.getKey();
			Mappings input_fci = entry.getValue();
			Mappings maps = MappingsObtainer.input.get(type);

			if(type.equals("obf")) {//Need to adjust this for servers and clients
				if(input_fci.getDefs().containsKey(def)) {
					return input_fci.getDefs().get(def);
				}
			}
			
			
			if (maps != null) {
				if (maps.getDefs().containsKey(def)) {
					is_in_input = true;
					String int_value = maps.getDefs().get(def);

					String new_classname = maps.getClassMappedName(old_classname);

					String desc = maps.renameClassesInMethodDescriptor(old_desc);

					String new_key = new_classname + "." + int_value + desc;

					if (input_fci.getDefs().containsKey(new_key)) {
						String ret = input_fci.getDefs().get(new_key);
//						if (ret.equals(old_name) && ret.equals(int_value)) {
//							return null;
//						}
					if (notConflicting(old_classname+old_desc+"@"+ret)) {
						return ret;
					}
					
					}

//					if (int_value.equals(old_name)) {
//						return null;
//					}

				}

			}

		}

	//	if (!is_in_input || MappingsObtainer.input.size() == 0) {// We need to make this number configable
			String out = "def_unknown_" + def_index + "_";// We did not have the leading _ before but we do now because
															// we use find and replace and without it will cut longer
															// ones when you do a shorter one
			def_index++;
			return out;
	//	} else {
	//		return null;// Likely not obfuscated
	//	} This has been causing issues, we will turn it on again later

		}else {
			return null;
		}
			
			
			
	}

	public static boolean notConflicting(String string) {
		// TODO Auto-generated method stub
		if(vars_y_defs_completa.contains(string)) {
			return false;
		}
		vars_y_defs_completa.add(string);
		return true;
	}

	// We could also make this do LocalVars
	public static String[] getParams(String def) {
		// TODO Auto-generated method stub

		String[] old_class_arr = java.util.Arrays.copyOfRange(def.split("\\."), 0, def.split("\\.").length - 1);
		String old_classname = String.join(".", old_class_arr);
		String old_desc = "(" + def.split("\\(")[1];

		int locals = Descriptor.numOfParameters(old_desc);
		int comp = 0;
		String[] ret = new String[locals];

		for (int i = 0; i < locals; i++) {
			comp++;
			for (Map.Entry<String, Mappings> entry : MappingsObtainer.input_fcis.entrySet()) {
				String type = entry.getKey();
				Mappings input_fci = entry.getValue();
				Mappings maps = MappingsObtainer.input.get(type);

				if(type.equals("obf")) {//Need to adjust this for servers and clients
					 maps=input_fci;
				}
				
				if (maps != null) {
					if (maps.getDefs().containsKey(def)) {
						String int_value = maps.getDefs().get(def);

						String new_classname = maps.getClassMappedName(old_classname);

						String desc = maps.renameClassesInMethodDescriptor(old_desc);

						String new_key = new_classname + "." + int_value + desc + "@" + comp;

						if (input_fci.getParams().containsKey(new_key)) {
							ret[comp - 1] = input_fci.getParams().get(new_key);
							break;
						}

					}

				}

			}
			if (ret[comp - 1] == null) {
				String out = "param_unknown_" + arg_index + "_";// We did not have the leading _ before but we do now
																// because we use find and replace and without it will
																// cut longer ones when you do a shorter one
				arg_index++;

				ret[comp - 1] = out;
			}

		}

		return ret;
	}

	public static String getClass(String clazz) {
		// TODO Auto-generated method stub

//Subclasses should already be mostly parsed

		
		if(classHasMaps(clazz)) {
		for (Map.Entry<String, Mappings> entry : MappingsObtainer.input_fcis.entrySet()) {
			String type = entry.getKey();
			Mappings input_fci = entry.getValue();
			Mappings maps = MappingsObtainer.input.get(type);

			if(type.equals("obf")) {//Need to adjust this for servers and clients
				if(input_fci.getClasses().containsKey(clazz)) {
					return input_fci.getClasses().get(clazz);
				}
			}
			
			if (maps != null) {
				if (maps.getClasses().containsKey(clazz)) {
					String new_classname = maps.getClassMappedName(clazz);

					if (input_fci.getClasses().containsKey(new_classname)) {
					String nuevo = input_fci.getClassMappedName(new_classname);
					if(!done_classes.contains(nuevo)) {
						done_classes.add(nuevo);
						return nuevo;
					}
					
					}

				}

			}

		}

		String out;
		if (clazz.contains("$")) {
			out = "class_unknown_" + class_index + "_";// We did not have the leading _ before but we do now because we
														// use find and replace and without it will cut longer ones when
														// you do a shorter one
		} else {
			out = "obf.class_unknown_" + class_index + "_";
		}

		class_index++;
		return out;
		
		
		
		}else {
			return null;
		}
		
	}

	public static List<String> getIncludes(String clazz) {

		return sl.getImmediateInheritedClasses(clazz);

	}
	

	//To check if any of the existing mappings have a mapping for a given def
	public static boolean defHasMaps(String def) {
		
		if(MappingsObtainer.input.size()==0) {
			return true; // TO be reevaluated but if empty we would not want a conflict with this method
		}
		
		for (Map.Entry<String, Mappings> entry : MappingsObtainer.input.entrySet()) {
			
			if(entry.getValue().getDefs().containsKey(def)) {
				return true;
			}
			
			
		}
	
		return false;
		
	}
	
	
	//To check if any of the existing mappings have a mapping for a given def
	public static boolean varHasMaps(String var) {
		
		if(MappingsObtainer.input.size()==0) {
			return true; // TO be reevaluated but if empty we would not want a conflict with this method
		}
		
		for (Map.Entry<String, Mappings> entry : MappingsObtainer.input.entrySet()) {
			
			if(entry.getValue().getVars().containsKey(var)) {
				return true;
			}
			
			
		}
	
		return false;
		
	}
	
	
	//To check if any of the existing mappings have a mapping for a given def
	public static boolean classHasMaps(String clazz) {
		
		if(MappingsObtainer.input.size()==0) {
			return true; // TO be reevaluated but if empty we would not want a conflict with this method
		}
		
		for (Map.Entry<String, Mappings> entry : MappingsObtainer.input.entrySet()) {
			
			if(entry.getValue().getClasses().containsKey(clazz)) {
				return true;
			}
			
			
		}
	
		return false;
		
	}
	
	
	
	
}
