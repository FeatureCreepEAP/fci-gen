package com.asbestosstar.fci_gen;

import java.util.List;
import java.util.Map;

import com.asbestosstar.assistremapper.Mappings;
import com.asbestosstar.assistremapper.mappings.PDMEMappings;
import com.asbestosstar.structurelib.StructureLib;

import javassist.CtClass;
import javassist.bytecode.Descriptor;

public class FCIUpdater {

	public static String[] denylisted_prefixes = new String[] { "io.", "net.minecraftforge", "net.fabricmc", "org.",
			"sun.", "com.", "ow2.", "ca.", "it.", "net.java.", "gnu"};
	public static Mappings main = new PDMEMappings();
	public static StructureLib sl = new StructureLib();
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
				main.addClass(clazz_name, getClass(clazz_name));
				List<String> incls = getIncludes(clazz_name);
				if (!incls.isEmpty()) {
					String incl = String.join(",", incls);
					main.getIncludes().put(clazz_name, incl);
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
		return false;

	}

	public static String getVar(String var) {
		// TODO Auto-generated method stub

		boolean is_in_input = false;
		String[] old_class_arr = java.util.Arrays.copyOfRange(var.split("\\."), 0, var.split("\\.").length - 1);
		String old_classname = String.join(".", old_class_arr);
		String old_desc = var.split(":")[1];
		String old_name = var.split(":")[0].split("\\.")[var.split("\\.").length - 1];

		for (Map.Entry<String, Mappings> entry : MappingsObtainer.input_fcis.entrySet()) {
			String type = entry.getKey();
			Mappings input_fci = entry.getValue();
			Mappings maps = MappingsObtainer.input.get(type);

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
						return ret;
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

	}

	public static String getDef(String def) {
		// TODO Auto-generated method stub

		boolean is_in_input = false;
		String[] old_class_arr = java.util.Arrays.copyOfRange(def.split("\\."), 0, def.split("\\.").length - 1);
		String old_classname = String.join(".", old_class_arr);
		String old_desc = "(" + def.split("\\(")[1];
		String old_name = def.split("\\(")[0].split("\\.")[def.split("\\.").length - 1];

		for (Map.Entry<String, Mappings> entry : MappingsObtainer.input_fcis.entrySet()) {
			String type = entry.getKey();
			Mappings input_fci = entry.getValue();
			Mappings maps = MappingsObtainer.input.get(type);

			if (maps != null) {
				if (maps.getDefs().containsKey(def)) {
					is_in_input = true;
					String int_value = maps.getDefs().get(def);

					String new_classname = maps.getClassMappedName(old_classname);

					String desc = maps.renameClassesInMethodDescriptor(old_desc);

					String new_key = new_classname + "." + int_value + desc;

					if (input_fci.getDefs().containsKey(new_key)) {
						String ret = input_fci.getDefs().get(new_key);
						if (ret.equals(old_name) && ret.equals(int_value)) {
							return null;
						}
						return ret;
					}

					if (int_value.equals(old_name)) {
						return null;
					}

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

		for (Map.Entry<String, Mappings> entry : MappingsObtainer.input_fcis.entrySet()) {
			String type = entry.getKey();
			Mappings input_fci = entry.getValue();
			Mappings maps = MappingsObtainer.input.get(type);

			if (maps != null) {
				if (maps.getClasses().containsKey(clazz)) {
					String new_classname = maps.getClassMappedName(clazz);

					if (input_fci.getClasses().containsKey(new_classname)) {
						return input_fci.getClassMappedName(new_classname);
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
	}

	public static List<String> getIncludes(String clazz) {

		return sl.getImmediateDerivativeClasses(clazz);

	}
	

}
