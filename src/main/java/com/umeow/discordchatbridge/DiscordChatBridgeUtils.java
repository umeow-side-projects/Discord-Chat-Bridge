package com.umeow.discordchatbridge;

import java.net.URI;

public class DiscordChatBridgeUtils {
	public static boolean checkWebHookUrl(String webhookUrl)
	{
		try 
		{
			URI url = new URI(webhookUrl);
			
			if(!url.getScheme().equals("https"))
				return false;
			
			if(!url.getHost().equals("discord.com"))
				return false;
			
			if(!url.getPath().startsWith("/api/webhooks/"))
				return false;
			
			return true;
		} 
		catch(Exception err)
		{
			return false;
		}
	}
}
