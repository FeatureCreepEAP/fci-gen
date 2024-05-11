package com.asbestosstar.fci_gen;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class FCIGenUtils {

	  public static void writeStringToFile(String string, File file) {  
	        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {  
	            writer.write(string);  
	        } catch (IOException e) {  
	            e.printStackTrace();  
	            // 这里您可以根据需要处理异常，例如记录日志或抛出自定义异常  
	        }  
	    }  
	  public static String readFileToString(String filePath) {  
	        StringBuilder content = new StringBuilder();  
	        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {  
	            String line;  
	            while ((line = reader.readLine()) != null) {  
	                content.append(line).append(System.lineSeparator());  
	            }  
	        } catch (IOException e) {  
	            e.printStackTrace();  
	        }  
	        return content.toString();  
	    }  

	  
	  
	   public static InputStream getFileInputStream(String fileUrl) {  
	        try {  
	            URL url = new URL(fileUrl);  
	            HttpURLConnection connection = (HttpURLConnection) url.openConnection();  
	            connection.setRequestMethod("GET");  
	  
	            int responseCode = connection.getResponseCode();  
	            if (responseCode == HttpURLConnection.HTTP_OK) {  
	                // 读取输入流到字节数组中  
	                InputStream inputStream = new BufferedInputStream(connection.getInputStream());  
	                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();  
	                byte[] buffer = new byte[1024];  
	                int bytesRead;  
	                while ((bytesRead = inputStream.read(buffer)) != -1) {  
	                    byteArrayOutputStream.write(buffer, 0, bytesRead);  
	                }  
	                byte[] byteArray = byteArrayOutputStream.toByteArray();  
	                  
	                // 关闭流  
	                byteArrayOutputStream.close();  
	                inputStream.close();  
	                  
	                // 返回一个基于内存中字节数组的输入流  
	                return new ByteArrayInputStream(byteArray);  
	            } else {  
	                throw new IOException("Server response code: " + responseCode);  
	            }  
	        } catch (IOException e) {  
	            e.printStackTrace();  
	            return null;
	        }  
	    }  
		
		public static InputStream getFileFromJarInputStream(InputStream jarInputStream, String filePath) throws IOException {  
			   try (JarInputStream jarInput = new JarInputStream(new BufferedInputStream(jarInputStream))) {  
		            JarEntry entry;  
		            while ((entry = jarInput.getNextJarEntry()) != null) {  
		                if (entry.getName().equals(filePath)) {  
		                    // 当找到匹配的 entry 时，读取其内容到 ByteArrayOutputStream  
		                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();  
		                    byte[] buffer = new byte[1024];  
		                    int bytesRead;  
		                    while ((bytesRead = jarInput.read(buffer)) != -1) {  
		                        byteArrayOutputStream.write(buffer, 0, bytesRead);  
		                    }  
		                    // 从 ByteArrayOutputStream 创建一个 ByteArrayInputStream 并返回  
		                    return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());  
		                }  
		            }  
		        }  
		        // 如果没有找到匹配的 entry，返回 null  
		        return null;  
		}  
	  
	    public static String getStringFromInputStream(InputStream inputStream) throws IOException {
	        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	        String nl = System.getProperty("line.separator");
	        String line;
	        StringBuilder stringBuilder = new StringBuilder();
	        while ((line = bufferedReader.readLine()) != null) {
	        	stringBuilder.append(line+nl);
	        }
	        return stringBuilder.toString();
	    }
	  
}
