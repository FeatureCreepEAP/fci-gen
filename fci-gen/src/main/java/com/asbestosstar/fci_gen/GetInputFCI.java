package com.asbestosstar.fci_gen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;

import com.asbestosstar.assistremapper.Mappings;
import com.asbestosstar.assistremapper.mappings.PDMEMappings;

public class GetInputFCI {

	public static LinkedHashMap<String, Mappings> input_fcis = new LinkedHashMap<String, Mappings>();
	
	/**
	 * Do not Call. Gets the FCIs from the PDME Files and puts them info fci_inputs the field
	 */
	public static void getFCIInput() {
		App.logger.info("Getting Input FCIs");
		File fci_input = new File("fci_input");
		if (!fci_input.exists()) {
			fci_input.mkdirs();
			App.logger.warn(
					"Missing fci_input folder. Absolutly new mappings with no influence to old will be generated. This will also hurt the ability to match new mappings");
		}

		File[] files = fci_input.listFiles((dir, name) -> name.endsWith(".pdme"));

		// 如果找到匹配的文件，则为其创建输入流
		if (files != null) {
			
			try {
				for (File prefile : files) {
					String filename = prefile.getName();
					try (FileInputStream preinputStream = new FileInputStream(prefile)) {
						if (filename.contains("legacy-fabric-intermediary")) {
							input_fcis.put("legacy-fabric-intermediary", new PDMEMappings(preinputStream));
							App.logger.info("Found Legacy Fain");

						} else if (filename.contains("fabric-intermediary")) {
							Mappings fain = new PDMEMappings(preinputStream);
							input_fcis.put("fabric-intermediary", fain);
							if(input_fcis.containsKey("ref")) {
								Mappings ref = input_fcis.get("ref");
								fain.getClasses().putAll(ref.getClasses());
								fain.getDefs().putAll(ref.getDefs());
								fain.getVars().putAll(ref.getVars());
								fain.getParams().putAll(ref.getParams());
								input_fcis.remove("ref");
								App.logger.info("Added REF");
							}
							
							App.logger.info("Found FAIN");

						}
					}
				
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for (File file : files) {
				try (FileInputStream inputStream = new FileInputStream(file)) {
					// 这里你可以处理输入流，比如读取文件内容
					// 例如，将输入流的内容转换为字符串
					// byte[] fileContent = Files.readAllBytes(file.toPath());
					// String content = new String(fileContent);
					// System.out.println("文件内容: " + content);

					String filename = file.getName();
					if (filename.contains("sugercane")) { // includes parchment and mojmap
						input_fcis.put("sugercane", new PDMEMappings(inputStream));
						App.logger.info("Found SugarCane");
					} else if (filename.contains("legacy-fabric-intermediary")) {//Ya tenemos estos
					} else if (filename.contains("fabric-intermediary")) {
					} else if (filename.contains("babric-intermediary")) {
						App.logger.info("Adding Babric-Intermediary");
						input_fcis.put("babric-intermediary", new PDMEMappings(inputStream));
					}
					else if (filename.contains("srg") && !filename.contains("parchsrg")) {
						App.logger.info("Adding SRG");
						input_fcis.put("srg", new PDMEMappings(inputStream));
						App.logger.info("Found SRG");

					} else if (filename.contains("mcp")) {
						App.logger.info("Adding MCP");
						input_fcis.put("mcp", new PDMEMappings(inputStream));
					} else if (filename.contains("hashed-mojmap")) {
						App.logger.info("Adding Hashed Mojmap");
						input_fcis.put("hashed-mojmap", new PDMEMappings(inputStream));
					} else if (filename.contains("spigot")) {
						App.logger.info("Adding Spigot");
						input_fcis.put("spigot", new PDMEMappings(inputStream));
					} else if (filename.contains("dynamic-mappings")) {
						App.logger.info("Adding DynamicMappings");
						input_fcis.put("dynamic-mappings", new PDMEMappings(inputStream));
					} else if (filename.contains("m3l-mappings")) {
						App.logger.info("Adding m3l");
						input_fcis.put("m3l-mappings", new PDMEMappings(inputStream));
					} else if (filename.contains("yarn")&&!filename.contains("blayyke")) { // Since POMF was never used at runtime it is ok to use the
															// updated yarn mappings from LegacyFabric
						input_fcis.put("yarn", new PDMEMappings(inputStream));
						App.logger.info("Found Yarn");
					} else if (filename.contains("barn")) {
						App.logger.info("Adding Babric-Intermediary");
						input_fcis.put("barn", new PDMEMappings(inputStream));
					} else if (filename.contains("blayyke_yarn")) {
						App.logger.info("Adding Blayyke");
						input_fcis.put("blayyke_yarn", new PDMEMappings(inputStream));
					} else if (filename.contains("plasma")) {
						App.logger.info("Adding Plasma");
						input_fcis.put("plasma", new PDMEMappings(inputStream));
					} else if (filename.contains("duvet-intermediary")) {
						App.logger.info("Adding Duvet");
						input_fcis.put("duvet-intermediary", new PDMEMappings(inputStream));
						App.logger.info("Found Duevet");
					} else if (filename.contains("chaos")) {
						App.logger.info("Adding Chaos");
						input_fcis.put("chaos", new PDMEMappings(inputStream));
					} else if (filename.contains("pigeon")) {
						App.logger.info("Adding pigeon");
						input_fcis.put("pigeon", new PDMEMappings(inputStream));
					} else if (filename.contains("calamus")) {
						App.logger.info("Adding calamus");
						input_fcis.put("calamus", new PDMEMappings(inputStream));
					} else if (filename.contains("feather")) {
						App.logger.info("Adding feather");
						input_fcis.put("feather", new PDMEMappings(inputStream));
					} else if (filename.contains("quilt")) {
						App.logger.info("Adding quilt");
						input_fcis.put("quilt", new PDMEMappings(inputStream));
					} else if (filename.contains("client")) {
						App.logger.info("Adding client");
						input_fcis.put("client", new PDMEMappings(inputStream));
					} else if (filename.contains("server")) {
						App.logger.info("Adding server");
						input_fcis.put("server", new PDMEMappings(inputStream));
					}
					
					else if (filename.contains("duvet-ref")) { // Remove this after done
						Mappings ref = new PDMEMappings(inputStream);
						if(!input_fcis.containsKey("fabric-intermediary")) {
						if(input_fcis.containsKey("duvet-intermediary")) {
							Mappings duvetint = input_fcis.get("duvet-intermediary");
							duvetint.getClasses().putAll(ref.getClasses());
							duvetint.getDefs().putAll(ref.getDefs());
							duvetint.getVars().putAll(ref.getVars());
							duvetint.getParams().putAll(ref.getParams());
						}else {
							App.logger.info("No DuvetIntermediary");						
							}
							input_fcis.put("duvet-ref", ref);
					} 
					
					
					
					}
					
					
					
					else if (filename.contains("ref")) { // Remove this after done
						Mappings ref = new PDMEMappings(inputStream);
						if(input_fcis.containsKey("fabric-intermediary")) {
							Mappings fain = input_fcis.get("fabric-intermediary");
							fain.getClasses().putAll(ref.getClasses());
							fain.getDefs().putAll(ref.getDefs());
							fain.getVars().putAll(ref.getVars());
							fain.getParams().putAll(ref.getParams());
						}else {
							App.logger.info("No FAIN");						}
							input_fcis.put("ref", ref);
					} 
					
					else { // obf common
						input_fcis.put("obf", new PDMEMappings(inputStream));
					}

					// 需要注意的是，上述的转换整个文件内容到字符串的方法可能不适合大文件
					// 对于大文件，你可能需要按块读取或使用其他方法处理

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	
}
