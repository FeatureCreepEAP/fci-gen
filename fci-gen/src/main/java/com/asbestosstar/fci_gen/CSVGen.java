package com.asbestosstar.fci_gen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import com.asbestosstar.assistremapper.Mappings;

import javassist.CtClass;
import javassist.bytecode.Descriptor;

public class CSVGen {

	// public static String nl = System.getProperty("line.separator");

	public static void makeCSV() {
		File csv = new File(App.config.get("output").asString() + "/output-" + MappingsObtainer.version + ".csv");
		csv.getParentFile().mkdirs();

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(csv.getCanonicalFile()))) {

			// writer.write(content);

			writer.write("obf,fci");

			for (Map.Entry<String, Mappings> entry : MappingsObtainer.input.entrySet()) {
				writer.write("," + entry.getKey());
			}
			writer.newLine();

			App.logger.info("Updating Classes CSV");
			for (CtClass clazz : FCIUpdater.sl.clazzes) {
				String cname = clazz.getName();
				if (!FCIUpdater.isDenyListed(cname)) {
					writer.write(cname + "," + unparseSubClass(FCIUpdater.main.getClassMappedName(cname)));

					for (Map.Entry<String, Mappings> entry : MappingsObtainer.input.entrySet()) {
						String out = ",";
						if (entry.getValue().getClasses().containsKey(cname)) {
							out = "," + unparseSubClass(entry.getValue().getClassMappedName(cname));
						}
						writer.write(out);

					}
					writer.newLine();

				}

			}

			App.logger.info("Updating Vars CSV");

			for (String var : FCIUpdater.sl.getOldestDeclaredFields()) {

				if (!FCIUpdater.isDenyListed(var)) {
					writer.write(var + "," + FCIUpdater.main.getVarMappedName(var));

					for (Map.Entry<String, Mappings> entry : MappingsObtainer.input.entrySet()) {
						String out = ",";
						if (entry.getValue().getVars().containsKey(var)) {
							out = "," + entry.getValue().getVarMappedName(var);
						}
						writer.write(out);

					}
					writer.newLine();
				}

			}

			App.logger.info("Updating Defs CSV");

			for (String def : FCIUpdater.sl.getOldestDeclaredMethods()) {
				if (!FCIUpdater.isDenyListed(def)) {
					writer.write(def + "," + FCIUpdater.main.getDefMappedName(def));

					for (Map.Entry<String, Mappings> entry : MappingsObtainer.input.entrySet()) {
						String out = ",";
						if (entry.getValue().getDefs().containsKey(def)) {
							out = "," + entry.getValue().getDefMappedName(def);
						}
						writer.write(out);

					}
					writer.newLine();
					getParams(def,writer);
				}
			}

			// 如果需要换行，可以添加如下代码

			// writer.newLine();

			// System.out.println("字符串已成功写入到文件: " + fileName);

		} catch (IOException e) {

			e.printStackTrace();

			// System.err.println("写入文件时发生错误: " + e.getMessage());

		}
		App.logger.info("CSV Completa");

		// String output = builder.toString();

	}
	
	
	
	
	
	
	
	
	// We could also make this do LocalVars
		public static void getParams(String def,BufferedWriter writer) throws IOException {
			// TODO Auto-generated method stub
			String desc = "(" + def.split("\\(")[1];
			int locals = Descriptor.numOfParameters(desc);

			for (int i = 1; i < locals; i++) {
				String full =  def+"_"+i;
				writer.write(full + "," + FCIUpdater.main.getParamMappedName(def,i));

				for (Map.Entry<String, Mappings> entry : MappingsObtainer.input.entrySet()) {
					String out = ",";
					if (entry.getValue().getParams().containsKey(full)) {
						out = "," + entry.getValue().getParams().get(full);
					}
					writer.write(out);

				}
				writer.newLine();

						
			}

		}
	
		
		
		public static String unparseSubClass(String clazz) {
			if(!clazz.contains("$")) {
				return clazz;
			}	
			String[] split = clazz.split("\\$");
			return split[split.length-1];
		}
	
	

}
