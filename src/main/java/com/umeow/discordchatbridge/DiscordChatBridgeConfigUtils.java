package com.umeow.discordchatbridge;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.umeow.discordchatbridge.discord.DiscordClient;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;

public class DiscordChatBridgeConfigUtils {
	private static URI webhookURL = null;
	
	public static URI getWebhookURI()
	{
		if(webhookURL != null)
			return webhookURL;
		
		if(DiscordChatBridge.config.webhookUrl == null)
			return null;
		
		try 
		{
			URI url = new URI(DiscordChatBridge.config.webhookUrl);
			
			webhookURL = url;
			
			return url;
		}
		catch(URISyntaxException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static URL getWebhookURL()
	{
		if(DiscordChatBridge.config.webhookUrl == null)
			return null;
		
		try {
			URL url = new URL(DiscordChatBridge.config.webhookUrl);
			return url;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static JsonObject getWebhookInfo()
	{
		URL url = getWebhookURL();
		
		if(url == null)
			return null;
		
		try {
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setUseCaches(false);
			connection.setAllowUserInteraction(false);
			connection.setConnectTimeout(15000);   //设置连接主机超时（单位：毫秒） 
			connection.setReadTimeout(15000);      //设置从主机读取数据超时（单位：毫秒） 
			connection.setRequestProperty("User-Agent","Mozilla/5.0");
			connection.setDoInput(true);
			
			connection.connect();

			String response = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
		
			Gson gson = new Gson();
			JsonObject result = gson.fromJson(response, JsonObject.class);
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void reload(ICommandSender sender)
	{
		sender.sendMessage(new TextComponentString("§bDiscord Chat Bridge reloading..."));
		
		webhookURL = null;
		
		CompletableFuture.runAsync(() -> {
			try
			{
				DiscordChatBridgeConfigLoader.loadConfig();
			
				DiscordClient.init(sender);
				
				if(!DiscordChatBridge.config.Mc2Dc)
					return;
			
				JsonObject webhookInfo = getWebhookInfo();
			
				if(webhookInfo == null || webhookInfo.has("code"))
				{
					sender.sendMessage(new TextComponentString("§bWebhook URL not working!"));
					return;
				}
				
				String webhookName = webhookInfo.get("name").getAsString();
				
				sender.sendMessage(
						new TextComponentString("§bSuccessfully connect to webhook -> " + webhookName));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		});
	}
}
