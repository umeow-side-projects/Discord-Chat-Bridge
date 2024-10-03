package com.umeow.discordchatbridge.discord;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.umeow.discordchatbridge.DiscordChatBridge;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class DiscordClient extends WebSocketClient
{
	private static String gatewayUrl = "wss://gateway.discord.gg/?v=10&encoding=json";
	
	private static ICommandSender player = null;
	
	private static DiscordClient client = null;
	
	private Gson gson = new Gson();
	private TimerTask timerTask = null;
	private Timer timer = new Timer();
	private Integer seq = null;
	
	private DiscordClient(URI serverURI)
	{
		super(serverURI);
	}
	
	public static DiscordClient getInstance()
	{
		if(client != null)
			return client;
		
		try
		{
			URI url = new URI(gatewayUrl);
			client = new DiscordClient(url);
			return client;
		}
		catch(URISyntaxException err)
		{
			err.printStackTrace();
			return null;
		}
	}
	
	public static void init(ICommandSender sender)
	{
		if(!DiscordChatBridge.config.Dc2Mc)
		{
			if(client != null)
				client.close(1000, "init");
			
			client = null;
			return;
		}
		
		if(client != null)
			client.close(1000, "init");
		
		try {
			player = sender;
			client = new DiscordClient(new URI(gatewayUrl));
			client.connect();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	private JsonObject makeHeartbeatMsg()
	{
		JsonObject msg = new JsonObject();
		msg.addProperty("op", 1);
		
		if(seq == null)
        	msg.add("d", JsonNull.INSTANCE);
        else
        	msg.addProperty("d", seq);
		
		return msg;
	}
	
	private void setHeartbeat(JsonObject event)
	{
		int heartbeatMs = event
				.get("d").getAsJsonObject()
				.get("heartbeat_interval").getAsInt();
		
		this.timerTask = new TimerTask() {
            @Override
            public void run() {
            	heartbeat();
            }
        };
        
        timer.schedule(timerTask, heartbeatMs, heartbeatMs);
	}
	
	private void heartbeat()
	{
		JsonObject msg = makeHeartbeatMsg();

        DiscordClient.getInstance().send(msg.toString());
	}
	
	private void login()
	{	
		String token = DiscordChatBridge.config.token;
		
		JsonObject properties = new JsonObject();
		properties.addProperty("os", "linux");
		properties.addProperty("browser", "Minecraft Mod - Discord Chat Bridge");
		properties.addProperty("device", "Minecraft Mod - Discord Chat Bridge");
		
		JsonObject d = new JsonObject();
		d.addProperty("token", token);
		d.addProperty("intents", 32768 + 512);
		d.add("properties", properties);
		
		JsonObject msg = new JsonObject();
		msg.addProperty("op", 2);
		msg.add("d", d);
		
		DiscordClient.getInstance().send(msg.toString());
	}
	
	private Boolean isBot(JsonObject event)
	{
		try
		{
			return event
					.get("d").getAsJsonObject()
					.get("author").getAsJsonObject()
					.get("bot").getAsBoolean();
		}
		catch(NullPointerException err)
		{
			return false;
		}
	}
	
	private String getUsername(JsonObject event)
	{
		JsonObject d = event.get("d").getAsJsonObject();
		
		JsonObject author = d.get("author").getAsJsonObject();
		
		JsonObject member = d.get("member").getAsJsonObject();
		
		JsonElement nick = member.get("nick");
		JsonElement globalName = author.get("global_name");
		
		if(nick.isJsonNull())
		{
			if(!globalName.isJsonNull())
				return globalName.getAsString();
			else
				return author.get("username").getAsString();
		}
		
		return nick.getAsString();
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {}

	@Override
	public void onMessage(String message)
	{
		JsonObject event = gson.fromJson(message, JsonObject.class);
		
		if(!event.get("s").isJsonNull())
			this.seq = event.get("s").getAsInt();
		
		int op = event.get("op").getAsInt();
		
		if(op == 1)
			heartbeat();
		
		else if(op == 10)
		{
			setHeartbeat(event);
			login();
		}
		
		else if(op == 0 && event.get("t").getAsString().equals("MESSAGE_CREATE"))
		{
			Boolean bot = isBot(event);
			
			if(bot)
				return;
			
			String username = getUsername(event);
			
			String content = event
				.get("d").getAsJsonObject()
				.get("content").getAsString();
			
			if(content.isEmpty())
			{
				JsonArray attachments = event
						.get("d").getAsJsonObject()
						.get("attachments").getAsJsonArray();
				
				if(attachments.size() == 0)
					DiscordChatBridge.logger.error("Discord message content is empty, please check your discord bot `MESSAGE CONTENT INTENT` permission is on!");
				return;
			}
			
			String chat = DiscordChatBridge.config.format;
			
			chat = chat.replaceAll("\\{username\\}", username);
			chat = chat.replaceAll("\\{content\\}", content);

			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
			server.getPlayerList().sendMessage(new TextComponentString(chat));		
		}
		
		else if(op == 0 && event.get("t").getAsString().equals("READY"))
		{
			if(player == null)
				return;
			
			JsonObject d = event.get("d").getAsJsonObject();
			JsonObject user = d.get("user").getAsJsonObject();
			
			String username = user.get("username").getAsString();
			
			player.sendMessage(
					new TextComponentString("§bSuccessfully connect to the discord bot -> " + username));
		}
	}

	@Override
	public void onClose(int code, String reason, boolean remote)
	{
		this.seq = null;
		this.timerTask.cancel();
		
		if(player != null && !reason.equals("init"))
		{
			player.sendMessage(
					new TextComponentString("Please check your discord bot token, it seem not working..."));
		}
	}

	@Override
	public void onError(Exception ex)
	{
		ex.printStackTrace();
	}
}
