package io.github.symt;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class ZealotCounterCommand extends CommandBase {

  ZealotCounter zealotCounter;

  public ZealotCounterCommand(ZealotCounter zealotCounter) {
    this.zealotCounter = zealotCounter;
  }

  @Override
  public List getCommandAliases() {
    return new ArrayList<String>() {
      {
        add("zc");
      }
    };
  }

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
    if (ics instanceof EntityPlayer /* && ZealotCounter.isInSkyblock */) {
      zealotCounter.openGui = true;
    } else if (!zealotCounter.isInSkyblock) {
      ics.addChatMessage(
          new ChatComponentText(
              EnumChatFormatting.RED + "Please join SkyBlock to use this command."));
    }
  }

  public boolean canCommandSenderUseCommand(final ICommandSender sender) {
    return true;
  }
}
