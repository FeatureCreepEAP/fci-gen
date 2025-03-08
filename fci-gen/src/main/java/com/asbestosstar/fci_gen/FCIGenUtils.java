package com.asbestosstar.fci_gen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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

}
