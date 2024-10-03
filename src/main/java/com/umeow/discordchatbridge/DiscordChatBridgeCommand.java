package com.umeow.discordchatbridge;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class DiscordChatBridgeCommand extends CommandBase {

	@Override
	public String getName() {
		return DiscordChatBridge.MODID;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/" + DiscordChatBridge.MODID + " reload";
	}
	
	@Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException 
	{
		if(args.length < 1)
		{
			sender.sendMessage(new TextComponentString("/" + DiscordChatBridge.MODID + " reload"));
			return;
		}
		
		if(!args[0].equals("reload"))
		{
			sender.sendMessage(new TextComponentString("Unknown command."));
			return;
		}
		
		DiscordChatBridgeConfigUtils.reload(sender);
	}
}
