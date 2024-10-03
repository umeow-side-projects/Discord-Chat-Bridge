package com.umeow.discordchatbridge;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;

import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DiscordChatBridgeEventHandler {
	@SubscribeEvent
    public void onPlayerSendChat(ServerChatEvent event) {
		if(DiscordChatBridge.config == null)
			return;
		
		if(!DiscordChatBridge.config.Mc2Dc)
			return;
		
		if(DiscordChatBridgeConfigUtils.getWebhookURI() == null)
			return;
		
		URL url = DiscordChatBridgeConfigUtils.getWebhookURL();
		
		CompletableFuture.runAsync(() -> {
			try {
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append("username=").append(URLEncoder.encode(event.getUsername(), "UTF-8")).append("&");
				stringBuilder.append("avatar_url=").append(URLEncoder.encode("https://minotar.net/helm/" + event.getPlayer().getUniqueID(), "UTF-8")).append("&");
				stringBuilder.append("content=").append(URLEncoder.encode(event.getMessage(), "UTF-8"));
				
				final byte[] postAsBytes = stringBuilder.toString().getBytes();
				
				HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
				connection.setConnectTimeout(15000);
		        connection.setReadTimeout(15000);
		        connection.setUseCaches(false);
				
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.setRequestProperty("Content-Length", "" + postAsBytes.length);
				connection.setRequestProperty("User-Agent", "Mozilla/5.0 ");
				connection.setDoOutput(true);
				connection.setDoInput(true);
				
				OutputStream outputStream = connection.getOutputStream();
	            IOUtils.write(postAsBytes, outputStream);
	            
	            IOUtils.closeQuietly(outputStream);
	            
	            InputStream inputStream = null;
	            try {
	                inputStream = connection.getInputStream();
	                IOUtils.toString(inputStream, StandardCharsets.UTF_8);
	            } catch (final IOException e) {
	                IOUtils.closeQuietly(inputStream);
	                inputStream = connection.getErrorStream();
	                IOUtils.toString(inputStream, StandardCharsets.UTF_8);
	            } finally {
	                IOUtils.closeQuietly(inputStream);
	            }
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
    }
}