package com.umeow.discordchatbridge;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;

public class DiscordChatBridgeConfigLoader
{
	public static String getDefaultConfigString() throws IOException
	{
		InputStream inputStream = DiscordChatBridgeConfigLoader.class.getResourceAsStream("/discordchatbridge.json");
		byte[] bytes = IOUtils.toByteArray(inputStream);
		
		String fileString =  new String(bytes, StandardCharsets.UTF_8);
		int sliceIndex = fileString.indexOf("\n");
		
		return fileString.substring(sliceIndex+1);
	}
	
	public static void createConfigFile() throws IOException 
	{
		File configFile = new File("config/discordchatbridge.json");
		
		if(!configFile.exists())
		{
			configFile.createNewFile();
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8);
			writer.write(getDefaultConfigString());
			writer.close();
		}
	}
	
	public static String readConfigString() throws IOException
	{
		File configFile = new File("config/discordchatbridge.json");
		
		if(!configFile.exists() || configFile.isDirectory())
		{
			return null;
		}
		
		return new String(Files.readAllBytes(configFile.toPath()), StandardCharsets.UTF_8);
	}
	
	public static void loadConfig()
	{
		try
		{
			createConfigFile();
			DiscordChatBridge.config = null;
			
			createConfigFile();
		
			String json = readConfigString();
		
			Gson gson = new Gson();
		
			DiscordChatBridge.config = gson.fromJson(json, DiscordChatBridgeConfig.class);
			
			if(DiscordChatBridge.config == null)
				return;
			
			if(!DiscordChatBridgeUtils.checkWebHookUrl(DiscordChatBridge.config.webhookUrl))
				DiscordChatBridge.config.webhookUrl = null;
		}
		catch(IOException err)
		{
			err.printStackTrace();
			DiscordChatBridge.config = null;
		}
	}
}
