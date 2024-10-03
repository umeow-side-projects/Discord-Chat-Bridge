package com.umeow.discordchatbridge;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(
	modid = DiscordChatBridge.MODID,
	name = DiscordChatBridge.NAME, 
	version = DiscordChatBridge.VERSION,
	serverSideOnly = true,
	acceptableRemoteVersions = "*")
public class DiscordChatBridge
{
    public static final String MODID = "discordchatbridge";
    public static final String NAME = "Discord Chat Bridge";
    public static final String VERSION = "1.0";

    public static Logger logger = null;
    
    public static DiscordChatBridgeConfig config = null;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	MinecraftForge.EVENT_BUS.register(new DiscordChatBridgeEventHandler());
    }
    
    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new DiscordChatBridgeCommand());
        
        DiscordChatBridgeConfigUtils.reload(event.getServer());
    }
}
