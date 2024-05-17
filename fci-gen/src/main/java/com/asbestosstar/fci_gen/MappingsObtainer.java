package com.asbestosstar.fci_gen;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.jboss.dmr.ModelNode;

import com.asbestosstar.assistmerger.AssistMerger;
import com.asbestosstar.assistremapper.Mappings;
import com.asbestosstar.assistremapper.mappings.CSRG;
import com.asbestosstar.assistremapper.mappings.ClassesCSRG;
import com.asbestosstar.assistremapper.mappings.DynamicMappingsTXT;
import com.asbestosstar.assistremapper.mappings.M3LMappings;
import com.asbestosstar.assistremapper.mappings.MCPCSVNewer;
import com.asbestosstar.assistremapper.mappings.PDMEMappings;
import com.asbestosstar.assistremapper.mappings.Proguard;
import com.asbestosstar.assistremapper.mappings.SRG;
import com.asbestosstar.assistremapper.mappings.TSRG;
import com.asbestosstar.assistremapper.mappings.TSRG2;
import com.asbestosstar.assistremapper.mappings.Tiny;
import com.asbestosstar.assistremapper.mappings.TinyV2;

public class MappingsObtainer {

	public static Map<String, Mappings> input_fcis = new HashMap<String, Mappings>();
	public static Map<String, Mappings> input = new HashMap<String, Mappings>();
	public static String version = App.config.get("game-version").asString();
	public static boolean is_snapshot = getIsSnapShot();
	public static ModelNode version_json;
	public static InputStream game_jar; // Soon need to make both client and server especially for older versions

	static {

		try {
			ModelNode launcher_manifest = ModelNode.fromJSONStream(
					FCIGenUtils.getFileInputStream("https://launchermeta.mojang.com/mc/game/version_manifest.json"));
			for (ModelNode vers : launcher_manifest.get("versions").asList()) {
				if (vers.get("id").asString().equals(version)) {
					version_json = ModelNode.fromJSONStream(FCIGenUtils.getFileInputStream(vers.get("url").asString()));
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			App.logger.fatal("Failed to download version manifest");
			e.printStackTrace();
		}

		App.logger.info("Downloading jar");
		
		ModelNode subjarnode = version_json.get("downloads");
		String jar_download;
		if(!isPre1() && !isNewerThanVersion("1.2.6") || getIsSnapShot()) {
		String sjars = subjarnode.get("server").get("url").asString();
		String cjars = subjarnode.get("client").get("url").asString();	
		if (sjars != null && cjars!=null) {
			InputStream cjar = FCIGenUtils.getFileInputStream(cjars);
			InputStream sjar = FCIGenUtils.getFileInputStream(sjars);
		App.logger.info("Merging Jars");
			
			if(cjar!=null && sjar!=null) {
				 // Use a ByteArrayOutputStream to capture the output from AssistMerger  
		        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();  
		        new AssistMerger(cjar, sjar, byteArrayOutputStream);
		     // Convert the ByteArrayOutputStream to a ByteArrayInputStream  
		        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());  
		        game_jar=byteArrayInputStream;
		        try {
					byteArrayOutputStream.close();
					cjar.close();
					sjar.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
		}
			
			
		}else {
		
		
		if (App.config.get("side").asString().equals("server")) {
			jar_download = subjarnode.get("server").get("url").asString();
		} else {
			jar_download = subjarnode.get("client").get("url").asString();
		}

		if (jar_download != null) {
			game_jar = FCIGenUtils.getFileInputStream(jar_download);
		} else {
			App.logger.error("Could not find jar download url");
		}
		}
		
	}

	public static boolean getIsSnapShot() { // Not best way to do this but it is fine for what
		if (version.contains(".")) {
			return false;
		}
		return true;
	}

	// only currently works for point versions OR snapshots but not a snapshot
	// compared to a point version
	public static boolean isNewerThanVersion(String versio) {
//Not perfect but its not really like we are usingthis for beta, mainly for just fabric vs legacy fabric intermediary
		String[] verio_split = versio.split("\\.");
		String[] verion_split = version.split("\\.");

		if (versio.contains(".") && version.contains(".")) {
			if (verio_split[0].length() > verion_split[0].length()) {
				return true;
			} else if (verio_split[0].length() < verion_split[0].length()) {
				return false;
			}

			if (verio_split[1].length() > verion_split[1].length()) {
				return true;
			} else if (verio_split[1].length() < verion_split[1].length()) {
				return false;
			}

			if (verio_split.length >= 3) {
				if (verion_split.length >= 3) {
					if (verio_split[2].length() > verion_split[2].length()) {// Technically catches pres but thats fine
						return true;
					} else if (verio_split[2].length() < verion_split[2].length()) {
						return false;
					}
				} else {
					return true;
				}
			}
		} else {
			App.logger.error("Tried to compare a snapshot to release");
		}
		// 使用 String.compare 比较两个版本字符串

		int result = versio.compareTo(version);

		// 如果 version1 大于或等于 version2，则返回 true

		return result >= 0;
	}

	public static void getInput() {
		getDynamicMappings();
		getFabricIntermediary();
		getLegacyFabricIntermediary();
		getSugarCane();
		getM3LMappings();
		// Babric and barn soon but I am struggling to understand their mapping format
		// and its only for 1 version anyhow so i cant use it to cross reference
		getYarn();
		getSRG();
		getMCP();
		getDuvetIntermediary();
		getHashed();
		getCalamus();
		getFeather();
		getQuilt();
		getSpigot();
		// CraftBukkit soon
		// Pigeon Soon
		// blayyke_yarn soon
		// plasma mappings soon
		// chaos mappings soon

	}

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
					} else if (filename.contains("legacy-fabric-intermediary")) {
						input_fcis.put("legacy-fabric-intermediary", new PDMEMappings(inputStream));
						App.logger.info("Found Legacy Fain");

					} else if (filename.contains("fabric-intermediary")) {
						Mappings fain = new PDMEMappings(inputStream);
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

					} else if (filename.contains("babric-intermediary")) {
						input_fcis.put("babric-intermediary", new PDMEMappings(inputStream));
					}
					else if (filename.contains("srg") && !filename.contains("parchsrg")) {
						input_fcis.put("srg", new PDMEMappings(inputStream));
						App.logger.info("Found SRG");

					} else if (filename.contains("mcp")) {
						input_fcis.put("mcp", new PDMEMappings(inputStream));
					} else if (filename.contains("hashed-mojmap")) {
						input_fcis.put("hashed-mojmap", new PDMEMappings(inputStream));
					} else if (filename.contains("spigot")) {
						input_fcis.put("spigot", new PDMEMappings(inputStream));
					} else if (filename.contains("dynamic-mappings")) {
						input_fcis.put("dynamic-mappings", new PDMEMappings(inputStream));
					} else if (filename.contains("m3l-mappings")) {
						input_fcis.put("m3l-mappings", new PDMEMappings(inputStream));
					} else if (filename.contains("yarn")) { // Since POMF was never used at runtime it is ok to use the
															// updated yarn mappings from LegacyFabric
						input_fcis.put("yarn", new PDMEMappings(inputStream));
						App.logger.info("Found Yarn");

					} else if (filename.contains("barn")) {
						input_fcis.put("barn", new PDMEMappings(inputStream));
					} else if (filename.contains("blayyke_yarn")) {
						input_fcis.put("blayyke_yarn", new PDMEMappings(inputStream));
					} else if (filename.contains("plasma")) {
						input_fcis.put("plasma", new PDMEMappings(inputStream));
					} else if (filename.contains("duvet-intermediary")) {
						input_fcis.put("duvet-intermediary", new PDMEMappings(inputStream));
					} else if (filename.contains("chaos")) {
						input_fcis.put("chaos", new PDMEMappings(inputStream));
					} else if (filename.contains("pigeon")) {
						input_fcis.put("pigeon", new PDMEMappings(inputStream));
					} else if (filename.contains("calamus")) {
						input_fcis.put("calamus", new PDMEMappings(inputStream));
					} else if (filename.contains("feather")) {
						input_fcis.put("feather", new PDMEMappings(inputStream));
					} else if (filename.contains("quilt")) {
						input_fcis.put("quilt", new PDMEMappings(inputStream));
					} else if (filename.contains("client")) {
						input_fcis.put("client", new PDMEMappings(inputStream));
					} else if (filename.contains("server")) {
						input_fcis.put("server", new PDMEMappings(inputStream));
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

	public static void getDynamicMappings() {
		// TODO Auto-generated method stub
		App.logger.info("Getting Dynamic Mappings");
		Map<String, String> locations = new HashMap<String, String>();
		locations.put("16w39c", "https://github.com/FyberOptic/DynamicMappings/raw/master/currentmappings.txt");
		locations.put("16w33a",
				"https://github.com/FyberOptic/DynamicMappings/raw/44954734c7470a3e550cefbbd3ce9542fe41971f/currentmappings.txt");
		locations.put("16w32b",
				"https://github.com/FyberOptic/DynamicMappings/raw/ae0ad7df649adcd8eeca3068085dcbe26c864dcd/currentmappings.txt");
		locations.put("16w32a",
				"https://github.com/FyberOptic/DynamicMappings/raw/d7da453ddb5b0950745e706ff257d99707f27e36/currentmappings.txt");
		locations.put("16w20a",
				"https://github.com/FyberOptic/DynamicMappings/raw/9b1bf17efc5ae09a9902362ec0346bd404b82068/currentmappings.txt");
		locations.put("1.9.4",
				"https://github.com/FyberOptic/DynamicMappings/raw/8ee06f146daa9c3e57167ebdcb80084028639f47/currentmappings.txt");
		locations.put("1.9.3-pre2",
				"https://github.com/FyberOptic/DynamicMappings/blob/f26acccfa6c18f010e79404ac096b9436794a9bc/currentmappings.txt");
		locations.put("15w49a",
				"https://github.com/FyberOptic/DynamicMappings/raw/a1f2281db1c809146d76e26a5c19141583407fe1/currentmappings.txt");
		locations.put("15w47b",
				"https://github.com/FyberOptic/DynamicMappings/raw/8f097575fffb49e2b9e240d50e45569c5adbeddd/currentmappings.txt");
		locations.put("15w34d",
				"https://github.com/FyberOptic/DynamicMappings/blob/a958b1cb9f9dd4adc38ad27531922137daca501f/currentmappings.txt");
//Not all versions but we do not need all versions

		if (locations.containsKey(version)) {
			input.put("dynamic-mappings",
					new DynamicMappingsTXT(FCIGenUtils.getFileInputStream(locations.get(version))).getReverse());// These
																													// ones
																													// are
																													// mapped
																													// to
																													// obf
																													// rather
																													// than
																													// obf
																													// to
																													// mapped
		}

	}

	public static void getFabricIntermediary() {
		App.logger.info("Getting Fabric Intermediary");
		String download_url;
		if (is_snapshot) {
			if (isNewerThanVersion("18w43a")) {
				download_url = "https://repo.legacyfabric.net/repository/legacyfabric/net/legacyfabric/v2/intermediary/"
						+ version + "/intermediary-" + version + ".jar";
			} else {
				download_url = "https://maven.fabricmc.net/net/fabricmc/intermediary/" + version + "/intermediary-"
						+ version + ".jar";
			}

		} else {
			if (isNewerThanVersion("1.13.2")) {
				download_url = "https://repo.legacyfabric.net/repository/legacyfabric/net/legacyfabric/v2/intermediary/"
						+ version + "/intermediary-" + version + ".jar";
			} else {
				download_url = "https://maven.fabricmc.net/net/fabricmc/intermediary/" + version + "/intermediary-"
						+ version + ".jar";
			}
		}
		InputStream downloaded_jar = FCIGenUtils.getFileInputStream(download_url); // prolly should have just used the
																					// github but i guess i was high
		if (downloaded_jar != null) {
			try {
				input.put("fabric-intermediary",
						new Tiny(FCIGenUtils.getFileFromJarInputStream(downloaded_jar, "mappings/mappings.tiny")));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				App.logger.info("Fabric Intermediary not found (common for versions before 1.8) or Failed to download");
				e.printStackTrace();
			}
		}

	}

	public static void getLegacyFabricIntermediary() {
		App.logger.info("Getting Legacy Fabric Intermediary");
		String download_url;
		download_url = "https://repo.legacyfabric.net/repository/legacyfabric/net/legacyfabric/intermediary/" + version
				+ "/intermediary-" + version + ".jar";

		InputStream downloaded_jar = FCIGenUtils.getFileInputStream(download_url); // prolly should have just used the
																					// github but i guess i was high
		if (downloaded_jar != null) {
			try {
				input.put("legacy-fabric-intermediary",
						new Tiny(FCIGenUtils.getFileFromJarInputStream(downloaded_jar, "mappings/mappings.tiny")));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				App.logger.error("Failed to get mappings from Jar");
			}
		} else {
			App.logger.info(
					"Failed to download Legacy Fabric Intermediary. Note that is is not avalible for versions past 1.14 or most snapshots so if you are doing a newer version this is to be expected");
		}

	}

	public static void getHashed() {
		App.logger.info("Getting Hashed MojMap");
		String download_url;
		download_url = "https://maven.quiltmc.org/repository/release/org/quiltmc/hashed/" + version + "/hashed-"
				+ version + ".jar";

		InputStream downloaded_jar = FCIGenUtils.getFileInputStream(download_url); // prolly should have just used the
																					// github but i guess i was high
		if (downloaded_jar != null) {
			try {
				input.put("hashed-mojmap",
						new TinyV2(FCIGenUtils.getFileFromJarInputStream(downloaded_jar, "mappings/mappings.tiny")));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				App.logger.error("Failed to get mappings from Jar");
			}
		} else {
			App.logger.info(
					"Failed to download Hashed. Note that is is not avalible for versions before 1.14 so if you are doing a newer version this is to be expected");
		}

	}

	public static void getCalamus() {
		App.logger.info("Getting Calamus");
		String download_url;
		download_url = "https://maven.ornithemc.net/releases/net/ornithemc/calamus/" + version + "/calamus-" + version
				+ ".jar";

		InputStream downloaded_jar = FCIGenUtils.getFileInputStream(download_url); // prolly should have just used the
																					// github but i guess i was high
		if (downloaded_jar != null) {
			try {
				input.put("calamus",
						new Tiny(FCIGenUtils.getFileFromJarInputStream(downloaded_jar, "mappings/mappings.tiny")));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				App.logger.error("Failed to get mappings from Jar");
			}
		} else {
			App.logger.info(
					"Failed to download Calamus. Note that is is not avalible for versions past 1.14 or most snapshots so if you are doing a newer version this is to be expected");
		}
	}

	public static void getDuvetIntermediary() {
		// TODO Auto-generated method stub
		App.logger.info("Getting Duvet Intermediary");
		String download_url;
		download_url = "https://raw.githubusercontent.com/DuvetMC/old-intermediaries/main/new-intermediaries/" + version
				+ ".tiny";

		InputStream gh = FCIGenUtils.getFileInputStream(download_url); // prolly should have just used the
																		// github but i guess i was high
		if (gh != null) {
			input.put("duvet-intermediary", new Tiny(gh));
		} else {
			App.logger.info(
					"Failed to download Duvet Intermediary. Note that is is not avalible for versions past 1.14 or most snapshots so if you are doing a newer version this is to be expected");
		}
	}

	public static void getSugarCane() { // Soon actually need to do parchment and then remove the p_'s at the beginning,
										// though tbf the fci format does not really do from param names but rather from
										// param locations so only the output matters
		App.logger.info("Getting Sugarcane/Parchment/MojMap");
		if (version_json.get("downloads").has("client_mappings")) {
			String uri = version_json.get("downloads").get("client_mappings").get("url").asString();
			input.put("sugarcane", new Proguard(FCIGenUtils.getFileInputStream(uri)).getReverse());
		}

	}

	public static void getM3LMappings() {
		App.logger.info("Getting M3L Mappings");
		String download_url;
		if (App.config.get("side").asString().equals("server")) {
			download_url = "https://raw.githubusercontent.com/Spartan322/M3L/master/conf/" + version
					+ ".server.mappings";
		} else {
			download_url = "https://raw.githubusercontent.com/Spartan322/M3L/master/conf/" + version
					+ ".client.mappings";
		}

		InputStream stream = FCIGenUtils.getFileInputStream(download_url);
		if (stream != null) {
			input.put("m3l-mappings", new M3LMappings(stream));
		} else {
			App.logger.info(
					"M3L Mappings not found for version (common on versions other than 1.8.3) or failed to download).");
		}

	}

	public static void getYarn() {
		// TODO Auto-generated method stub
		App.logger.info("Getting Yarn");

		String intermed;
		String manifest_rul;
		Mappings intmap;
		if (is_snapshot) {
//No legacy yarn for snapshots seems to exist on the manifest i am using so i will skip it for now
			manifest_rul = "https://meta.fabricmc.net/v2/versions/yarn";
			intermed = "fabric-intermediary";

		} else {
			if (isNewerThanVersion("1.13.3")) {
				manifest_rul = "https://meta.legacyfabric.net/v2/versions/yarn";
				intermed = "legacy-fabric-intermediary";
			} else {
				manifest_rul = "https://meta.fabricmc.net/v2/versions/yarn";
				intermed = "fabric-intermediary";
			}
		}
		InputStream metanode = FCIGenUtils.getFileInputStream(manifest_rul);

		if (metanode != null) {
			try {
				ModelNode meta = ModelNode.fromJSONStream(metanode);
				String full_version = null;
				for (ModelNode subnode : meta.asList()) { // Higher builds are generally at top so this is fine
					if (subnode.get("gameVersion").asString().equals(version)) {
						full_version = subnode.get("version").asString();
						break;
					}
				}
				if (full_version != null) {
					String download_url;
					if (intermed.equals("legacy-fabric-intermediary")) {
						download_url = "https://repo.legacyfabric.net/repository/legacyfabric/net/legacyfabric/yarn/"
								+ full_version + "/yarn-" + full_version + "-mergedv2.jar";
						intmap = input.get("legacy-fabric-intermediary");
						InputStream downloaded_jar = FCIGenUtils.getFileInputStream(download_url);
						Mappings yarnfain = new TinyV2(
								FCIGenUtils.getFileFromJarInputStream(downloaded_jar, "mappings/mappings.tiny"));
						input.put("yarn", yarnfain);
					} else if (isNewerThanVersion("1.15.3")) {
						download_url = "https://maven.fabricmc.net/net/fabricmc/yarn/" + full_version + "/yarn-"
								+ full_version + "-v2.jar";
						intmap = input.get("fabric-intermediary");
						InputStream downloaded_jar = FCIGenUtils.getFileInputStream(download_url);
						Mappings yarnfain = new TinyV2(
								FCIGenUtils.getFileFromJarInputStream(downloaded_jar, "mappings/mappings.tiny"),
								TinyV2.TinyV2SubType.OBF_TO_CLEAN);
						Mappings yarn = new PDMEMappings();
						Mappings.convert(yarnfain, intmap.getReverse(), yarn);
						input.put("yarn", yarn);
					} else {
						download_url = "https://maven.fabricmc.net/net/fabricmc/yarn/" + full_version + "/yarn-"
								+ full_version + "-mergedv2.jar";
						intmap = input.get("fabric-intermediary");
						InputStream downloaded_jar = FCIGenUtils.getFileInputStream(download_url);
						Mappings yarnfain = new TinyV2(
								FCIGenUtils.getFileFromJarInputStream(downloaded_jar, "mappings/mappings.tiny"));
						input.put("yarn", yarnfain);
					}

				} else {
					App.logger.info("Yarn not found for version");
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				App.logger.info("Error Downloading Yarn Mappings.");
			}

		}
	}

	public static void getFeather() {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub

		App.logger.info("Getting Feather");
		String manifest_rul = "https://meta.ornithemc.net/v3/versions/feather/";

		InputStream metanode = FCIGenUtils.getFileInputStream(manifest_rul);

		if (metanode != null) {
			try {
				ModelNode meta = ModelNode.fromJSONStream(metanode);
				String full_version = null;
				for (ModelNode subnode : meta.asList()) { // Higher builds are generally at top so this is fine
					if (subnode.get("gameVersion").asString().equals(version)) {
						full_version = subnode.get("version").asString();
						break;
					}
				}
				if (full_version != null) {
					String download_url;

					download_url = "https://maven.ornithemc.net/releases/net/ornithemc/feather/" + full_version
							+ "/feather-" + full_version + "-mergedv2.jar";
					InputStream downloaded_jar = FCIGenUtils.getFileInputStream(download_url);
					Mappings yarnfain = new TinyV2(
							FCIGenUtils.getFileFromJarInputStream(downloaded_jar, "mappings/mappings.tiny"));
					input.put("feather", yarnfain);

				} else {
					App.logger.info("Feather not found for version");
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				App.logger.info("Error Downloading Feather Mappings.");
			}

		}
	}

	public static void getQuilt() {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub

		App.logger.info("Getting Quilt Mappings");
		String manifest_rul = "https://meta.quiltmc.org/v3/versions/quilt-mappings";

		InputStream metanode = FCIGenUtils.getFileInputStream(manifest_rul);

		if (metanode != null) {
			try {
				ModelNode meta = ModelNode.fromJSONStream(metanode);
				String full_version = null;
				for (ModelNode subnode : meta.asList()) { // Higher builds are generally at top so this is fine
					if (subnode.get("gameVersion").asString().equals(version)) {
						full_version = subnode.get("version").asString();
						break;
					}
				}
				if (full_version != null) {
					String download_url;

					download_url = "https://maven.quiltmc.org/repository/release/org/quiltmc/quilt-mappings/"
							+ full_version + "/quilt-mappings-" + full_version + "-mergedv2.jar";
					InputStream downloaded_jar = FCIGenUtils.getFileInputStream(download_url);
					Mappings yarnfain = new TinyV2(
							FCIGenUtils.getFileFromJarInputStream(downloaded_jar, "mappings/mappings.tiny"));
					input.put("quilt", yarnfain);

				} else {
					App.logger.info("Quilt Mappings not found for version");
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				App.logger.info("Error Downloading Quilt Mappings.");
			}

		}
	}

	public static void getSRG() {
		// TODO Auto-generated method stub
		// TODO ones older than 1.12

		App.logger.info("Getting SRG");
		String tsrg_url = null;
		if (is_snapshot) { // the snapshot situation in MCPConfig is weird but i gotta find a better way to
							// optimise this

			if (isNewerThanVersion("17w47")) {

				switch (version) {
				case "20w14infinite":
					tsrg_url = "https://github.com/MinecraftForge/MCPConfig/raw/master/versions/snapshot/april/20w14infinite/joined.tsrg";
					break;
				case "22w13oneblockatatime":
					tsrg_url = "https://github.com/MinecraftForge/MCPConfig/raw/master/versions/snapshot/april/22w13oneblockatatime/joined.tsrg";
					break;
				case "23w13a_or_b":
					tsrg_url = "https://github.com/MinecraftForge/MCPConfig/blob/master/versions/snapshot/april/23w13a_or_b/joined.tsrg";
					break;
				case "22w03a":
					tsrg_url = "https://github.com/MinecraftForge/MCPConfig/raw/master/versions/snapshot/1.18/22w03a/joined.tsrg";
					break;
				default:
					tsrg_url = "https://github.com/MinecraftForge/MCPConfig/raw/master/versions/snapshot/1.13/"
							+ version + "/joined.tsrg"; // Need too edit this to actually find the actual version not
														// just 1.13
				}
			}

		} else if (!isNewerThanVersion("1.11.2")) {
			if (version.contains("pre") || version.contains("rc")) {
				if (version.contains("1.20.2")) { // has its own folder
					tsrg_url = "https://github.com/MinecraftForge/MCPConfig/raw/master/versions/pre/1.20.2/" + version
							+ "/joined.tsrg";
				} else {
					String substr = version.substring(0, 4);
					tsrg_url = "https://github.com/MinecraftForge/MCPConfig/raw/master/versions/pre/" + substr + "/"
							+ version + "/joined.tsrg";
				}
			} else {
				tsrg_url = "https://github.com/MinecraftForge/MCPConfig/raw/master/versions/release/" + version
						+ "/joined.tsrg";
			}

		} else if (!isNewerThanVersion("1.7.9")) {

			String srg_url = "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp/" + version + "/mcp-" + version
					+ "-srg.zip";
			InputStream downloaded_jar = FCIGenUtils.getFileInputStream(srg_url);
			try {
				input.put("srg",
						new SRG(FCIGenUtils.getFileFromJarInputStream(downloaded_jar, "joined.srg"), game_jar));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} // Currently only doing 1.12 and newer
		else if (!isNewerThanVersion("1.7.1")) {
			// 1.7.2 has joined but is not on MC Forge Maven
			String srg_url = "https://ia601701.us.archive.org/view_archive.php?archive=/29/items/minecraftcoderpack/minecraftcoderpack.zip&file=minecraftcoderpack/1.7.2/mcp903.zip";
			InputStream downloaded_jar = FCIGenUtils.getFileInputStream(srg_url);
			try {
				input.put("srg",
						new SRG(FCIGenUtils.getFileFromJarInputStream(downloaded_jar, "conf/joined.srg"), game_jar));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} // Stop at 1.7.1 for now

		if (tsrg_url != null) {
			InputStream tsrg = FCIGenUtils.getFileInputStream(tsrg_url);
			if (tsrg != null) {
				if (isNewerThanVersion("1.16.4")) {
					input.put("srg", new TSRG(tsrg, game_jar));
				} else {
					input.put("srg", new TSRG2(tsrg, game_jar));
				}
			} else {
				App.logger.error("Could not get SRG version");
			}
		} else {
			App.logger.info("This version does not use MCPConfig");
		}

		// else {
//			manifest_rul = "https://meta.fabricmc.net/v2/versions/yarn";
//			intermed = "fabric-intermediary";
//		}
	}

	public static void getMCP() {
		// TODO Auto-generated method stub
		App.logger.info("Getting MCP");
		String latest_mcp = null;

		if (version.equals("1.16")) {
			latest_mcp = "20200514";
		} else if (version.equals("1.16.1")) {
			latest_mcp = "20200820";
		} else if (version.equals("1.16.2")) {
			latest_mcp = "20200916";
		} else if (version.equals("1.16.3")) {
			latest_mcp = "20201028";
		} else if (version.equals("1.16.4") || version.equals("1.16.5")) {
			latest_mcp = "20210309";
		} else if (!isNewerThanVersion("1.7.9")) {
			
			try {
				InputStream stre = FCIGenUtils
						.getFileInputStream("https://maven.minecraftforge.net/de/oceanlabs/mcp/versions.json");
				ModelNode node = ModelNode.fromJSONString(FCIGenUtils.getStringFromInputStream(stre));
				if (node.has(version)) {
					latest_mcp = node.get(version).get("snapshot").get(0).asString();// Snapshot always has but not //
																						// always stable
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (latest_mcp != null) {
				String dl = "https://files.minecraftforge.net/maven/de/oceanlabs/mcp/mcp_snapshot/" + latest_mcp + "-"
						+ version + "/" + "mcp_snapshot-" + latest_mcp + "-" + version + ".zip";
				InputStream downloaded_jar = FCIGenUtils.getFileInputStream(dl);
				try {
					InputStream fields = FCIGenUtils.getFileFromJarInputStream(downloaded_jar, "fields.csv");
					downloaded_jar.reset();
					InputStream methods = FCIGenUtils.getFileFromJarInputStream(downloaded_jar, "methods.csv");
					downloaded_jar.reset();
					InputStream params = FCIGenUtils.getFileFromJarInputStream(downloaded_jar, "params.csv");
					input.put("mcp", new MCPCSVNewer(input.get("srg"), fields, methods, params));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} else if (!isNewerThanVersion("1.7.1")) {// Will do for old versions later its just i dont want to get their
													// MCP location yet cuz i am lazy though the zip parsing is
													// basically the same
			String dl = "https://archive.org/download/minecraftcoderpack/minecraftcoderpack.zip/minecraftcoderpack%2F1.7.2%2Fmcp903.zip";
			InputStream downloaded_jar = FCIGenUtils.getFileInputStream(dl);
			try {
				InputStream fields = FCIGenUtils.getFileFromJarInputStream(downloaded_jar, "conf/fields.csv");
				downloaded_jar.reset();
				InputStream methods = FCIGenUtils.getFileFromJarInputStream(downloaded_jar, "conf/methods.csv");
				downloaded_jar.reset();
				InputStream params = FCIGenUtils.getFileFromJarInputStream(downloaded_jar, "conf/params.csv");
				downloaded_jar.reset();
				input.put("mcp", new MCPCSVNewer(input.get("srg"), fields, methods, params));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public static void getSpigot() {
		// TODO Auto-generated method stub
		App.logger.info("Getting Spigot Mappings");
		String manifest_url = "https://hub.spigotmc.org/versions/" + version + ".json";
		try {
			InputStream manstr = FCIGenUtils.getFileInputStream(manifest_url);
			if (manstr != null) {
				ModelNode manifest = ModelNode.fromJSONStream(manstr);
				String build = manifest.get("refs").get("BuildData").asString();
				String download = "https://hub.spigotmc.org/stash/rest/api/latest/projects/SPIGOT/repos/builddata/archive?at="
						+ build + "&format=zip";
				InputStream dl = FCIGenUtils.getFileInputStream(download);
				InputStream classmap = FCIGenUtils.getFileFromJarInputStream(dl,
						"mappings/bukkit-" + version + "-cl.csrg");
				Mappings out;
				dl.reset();
				InputStream membermap = FCIGenUtils.getFileFromJarInputStream(dl,
						"mappings/bukkit-" + version + "-members.csrg");

				if (classmap != null) {
					if (membermap != null) {
						game_jar.reset();
						out = new CSRG(new ClassesCSRG(classmap), membermap, game_jar);
					} else {
						out = new ClassesCSRG(classmap);
						App.logger.info("This version of Spigot Maps only has classnames");
					}
					input.put("spigot", out);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			App.logger.info("No Spigot Mappings for this version");
			e.printStackTrace();
		}

	}

	public static boolean isPre1() {
		// TODO Auto-generated method stub

		if (version.startsWith("rd-") || version.startsWith("mc-") || version.startsWith("c") || version.startsWith("i")
				|| version.startsWith("0") || version.startsWith("mc-") || version.startsWith("a")
				|| version.startsWith("b")) {
			return true;
		}

		return false;
	}

}
