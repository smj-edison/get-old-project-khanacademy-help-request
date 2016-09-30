package com.mason.getOldRequest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class Parser {
	private static int limit = 300;
	static final String DATA_FOLDER_NAME = System.getProperty("user.home") + System.getProperty("file.separator") + ".old_programs";
	static final Path DATA_FOLDER = Paths.get(DATA_FOLDER_NAME);
	static final Path LINK_FILE = Paths.get(DATA_FOLDER_NAME + System.getProperty("file.separator") + "helpRequests.txt");
	public static String parseString = "";
	public static List<byte[]> l;
	public static StringBuilder s;
	public static void main(Main m, int limit){
		String url = "https://www.khanacademy.org/api/internal/projecthelp?casing=camel&limit=" + limit + "&topic=computer-programming&max_answers=0&lang=en&_=1473445492698";
		URL myurl;
		HttpsURLConnection con;
		StringBuilder builder = new StringBuilder(4096);
		try {
			checkAndCreateFiles();
			//connect to https://www.khanacademy.org/api
			myurl = new URL(url);
			con = (HttpsURLConnection) myurl.openConnection();
			InputStream ins = con.getInputStream();
			InputStreamReader isr = new InputStreamReader(ins);
			BufferedReader in = new BufferedReader(isr);
			String inputLine;
			//TODO: have progress bar active
			while((inputLine = in.readLine()) != null){
			  builder.append(inputLine);
			}
			in.close();
			parseString = builder.toString();
		} catch(MalformedURLException e){
			e.printStackTrace();
		} catch(IOException e){  
			e.printStackTrace();
		}
		//parse json
		JsonObject jsonObject = new JsonParser().parse(builder.toString()).getAsJsonObject();
		builder = new StringBuilder(4096);
		int num = jsonObject.get("length").getAsInt() - 1;
		JsonArray o = jsonObject.get("feedback").getAsJsonArray();
		//put links together
		for(int i = 0; i < o.size(); i++){
			builder.append((i != 0 ? "," : "") + o.get(i).getAsJsonObject().get("focusUrl").getAsString() + "?qa_expand_key=" + o.get(i).getAsJsonObject().get("key").getAsString());
		}
		//write data
		write(builder.toString());
		//call read on Main
		m.read();
	}
	static public boolean write(String json){
		try {
			File file = LINK_FILE.toFile(); 
			FileOutputStream fop = new FileOutputStream(file, false);
			if(!file.exists()){
				file.createNewFile();
			}
			byte[] contentInBytes = json.getBytes("US-ASCII");
			fop.write(contentInBytes);
			fop.flush();
			fop.close();
		} catch(IOException f){
			return false;
		}
		return true;
	}
	//creates main files
	private static void checkAndCreateFiles(){
		if(!Files.exists(DATA_FOLDER)){
			try {
				Files.createDirectory(DATA_FOLDER);
			} catch(IOException l){
				l.printStackTrace();
				System.exit(1);
			}
		}
		if(!Files.exists(LINK_FILE)){
			try {
				Files.createFile(LINK_FILE);
			} catch(IOException m){
				m.printStackTrace();
				System.exit(1);
			}
		}
	}
}
