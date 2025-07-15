package com.asbestosstar.fci_gen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.benf.cfr.reader.util.annotation.Nullable;

import com.asbestosstar.assistremapper.Mappings;
import com.asbestosstar.minecraftmappingsobtainer.MappingsObtainer;

import javassist.CtClass;
import javassist.bytecode.Descriptor;

public class CSVGen {

	// public static String nl = System.getProperty("line.separator");

	public MappingsObtainer obtainer;
	public FCIUpdater cliente;
	public FCIUpdater servidor;

	/**
	 * Para generar el archivo CSV
	 * 
	 * @param obtainer Obtainer de Mappings
	 * @param cliente  Cliente FCIUpdater o Commun FCIUpdater para 1.3+ y snapshots
	 *                 despues de la union de mappings
	 * @param servidor FCIUpdater de Servidor, null en 1.3+ y snapshots despues de
	 *                 la union de mappings
	 */
	public CSVGen(MappingsObtainer obtainer, FCIUpdater cliente, @Nullable FCIUpdater servidor) {
		this.obtainer = obtainer;
		this.cliente = cliente;
		this.servidor = servidor;
	}

	public void makeCSV() {
		File csv = new File(App.config.get("output").asString() + "/output-" + obtainer.version + ".csv");
		csv.getParentFile().mkdirs();

		if (App.is_pre_1_3) {
			// HACER
		} else {
			paraComun(csv);
		}

		// String output = builder.toString();

	}

	public void paraComun(File csv) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(csv.getCanonicalFile()))) {

			// writer.write(content);

			writer.write("obf,fci");

			for (Map.Entry<String, Mappings> entry : obtainer.input.entrySet()) {
				writer.write("," + entry.getKey());
			}
			writer.newLine();

			App.logger.info("Updating Classes CSV");
			for (CtClass clazz : cliente.sl.clazzes) {
				String cname = clazz.getName();
				if (!cliente.isDenyListed(cname)) {
					writer.write(cname + "," + unparseSubClass(cliente.main.getClassMappedName(cname)));

					for (Map.Entry<String, Mappings> entry : obtainer.input.entrySet()) {
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

			for (String var : cliente.sl.getOldestDeclaredFields()) {

				if (!cliente.isDenyListed(var)) {
					writer.write(var + "," + cliente.main.getVarMappedName(var));

					for (Map.Entry<String, Mappings> entry : obtainer.input.entrySet()) {
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

			for (String def : cliente.sl.getOldestDeclaredMethods()) {
				if (!cliente.isDenyListed(def)) {
					writer.write(def + "," + cliente.main.getDefMappedName(def));

					for (Map.Entry<String, Mappings> entry : obtainer.input.entrySet()) {
						String out = ",";
						if (entry.getValue().getDefs().containsKey(def)) {
							out = "," + entry.getValue().getDefMappedName(def);
						}
						writer.write(out);

					}
					writer.newLine();
					getParams(def, writer, cliente);
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
	}

	// We could also make this do LocalVars
	public void getParams(String def, BufferedWriter writer, FCIUpdater actualizor) throws IOException {
		// TODO Auto-generated method stub
		String desc = "(" + def.split("\\(")[1];
		int locals = Descriptor.numOfParameters(desc);

		for (int i = 1; i < locals; i++) {
			String full = def + "_" + i;
			writer.write(full + "," + actualizor.main.getParamMappedName(def, i));

			for (Map.Entry<String, Mappings> entry : obtainer.input.entrySet()) {
				String out = ",";
				if (entry.getValue().getParams().containsKey(full)) {
					out = "," + entry.getValue().getParams().get(full);
				}
				writer.write(out);

			}
			writer.newLine();

		}

	}

	public String unparseSubClass(String clazz) {
		if (!clazz.contains("$")) {
			return clazz;
		}
		String[] split = clazz.split("\\$");
		return split[split.length - 1];
	}

}
