package io.github.symt;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class ZealotCounterCommand extends CommandBase {

  @Override
  public String getCommandName() {
    return "zealotcounter";
  }

  @Override
  public String getCommandUsage(ICommandSender sender) {
    return "/zealotcounter [subcommand]";
  }

  @Override
  public void processCommand(ICommandSender ics, String[] args) {
    if (ics instanceof EntityPlayer) {
      final EntityPlayer player = (EntityPlayer) ics;
      if (args.length == 3 && args[0].equalsIgnoreCase("location") && ZealotCounter
          .isInteger(args[1]) && ZealotCounter.isInteger(args[2])) {
        ZealotCounter.guiLocation = new int[]{Integer.parseInt(args[1]), Integer.parseInt(args[2])};
        ZealotCounter.saveZealotInfo(ZealotCounter.zealotCount, ZealotCounter.summoningEyes,
            ZealotCounter.sinceLastEye);
      } else if (args.length == 2 && args[0].equalsIgnoreCase("align")) {
        ZealotCounter.align = (args[1].equalsIgnoreCase("right")) ? "right" : "left";
      } else if (args.length == 2 && args[0].equalsIgnoreCase("color") && ZealotCounter
          .isInteger(args[1], 16) && args[1].length() == 6) {
        ZealotCounter.color = Integer.parseInt(args[1], 16);
      } else if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
        ZealotCounter.summoningEyes = 0;
        ZealotCounter.zealotCount = 0;
        ZealotCounter.saveZealotInfo(0, 0, 0);
        player.addChatMessage(
            new ChatComponentText(EnumChatFormatting.GREEN + "ZealotCounter has been reset"));
      } else if (args.length == 1 && args[0].equalsIgnoreCase("save")) {
        ZealotCounter.saveZealotInfo(ZealotCounter.zealotCount, ZealotCounter.summoningEyes,
            ZealotCounter.sinceLastEye);
        player.addChatMessage(
            new ChatComponentText(EnumChatFormatting.GREEN + "ZealotCounter has been saved"));
      } else {
        player.addChatMessage(
            new ChatComponentText(EnumChatFormatting.DARK_GRAY + "---------------------------"));
        player.addChatMessage(new ChatComponentText(
            EnumChatFormatting.BLUE + "/zealotcounter [subcommand] [arguments]"));
        player.addChatMessage(new ChatComponentText(
            EnumChatFormatting.DARK_PURPLE + "1. " + EnumChatFormatting.LIGHT_PURPLE + "save"));
        player.addChatMessage(new ChatComponentText(
            EnumChatFormatting.DARK_PURPLE + "2. " + EnumChatFormatting.LIGHT_PURPLE + "reset"));
        player.addChatMessage(new ChatComponentText(
            EnumChatFormatting.DARK_PURPLE + "3. " + EnumChatFormatting.LIGHT_PURPLE
                + "location (x) (y)"));
        player.addChatMessage(new ChatComponentText(
            EnumChatFormatting.DARK_PURPLE + "4. " + EnumChatFormatting.LIGHT_PURPLE
                + "color (hex color)"));
        player.addChatMessage(new ChatComponentText(
            EnumChatFormatting.DARK_PURPLE + "5. " + EnumChatFormatting.LIGHT_PURPLE
                + "align (left|right)"));
        player.addChatMessage(
            new ChatComponentText(EnumChatFormatting.DARK_GRAY + "---------------------------"));
      }
    }
  }

  public boolean canCommandSenderUseCommand(final ICommandSender sender) {
    return true;
  }
}
