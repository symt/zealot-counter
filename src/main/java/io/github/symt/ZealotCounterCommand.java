package io.github.symt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.time.StopWatch;

public class ZealotCounterCommand extends CommandBase {

  private ZealotCounter zealotCounter;

  ZealotCounterCommand(ZealotCounter zealotCounter) {
    this.zealotCounter = zealotCounter;
  }

  @Override
  public List<String> getCommandAliases() {
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
    if (ics instanceof EntityPlayer && zealotCounter.isInSkyblock) {
      EntityPlayer player = (EntityPlayer)ics;
      if (args.length == 2 && args[0].equalsIgnoreCase("timer") && (Arrays
          .asList(new String[]{"start", "stop", "reset", "resume"})
          .contains(args[1].toLowerCase()))) {
        switch (args[1].toLowerCase()) {
          case "reset":
            zealotCounter.zealotSession = 0;
            zealotCounter.eventHandler.perHourTimer = new StopWatch();
            player
                .addChatMessage(
                    new ChatComponentText(
                        EnumChatFormatting.GREEN + "Session reset successfully. Use "
                            + EnumChatFormatting.DARK_GREEN + "/zc timer start"
                            + EnumChatFormatting.GREEN + " to restart the timer"));
            break;
          case "stop":
            if (!zealotCounter.eventHandler.perHourTimer.isSuspended() && !zealotCounter.eventHandler.perHourTimer
                .isStopped()) {
              zealotCounter.eventHandler.perHourTimer.suspend();
              player
                  .addChatMessage(new ChatComponentText(
                      EnumChatFormatting.GREEN + "Session paused. Use "
                          + EnumChatFormatting.DARK_GREEN + "/zc timer resume"
                          + EnumChatFormatting.GREEN
                          + " to resume"));
            } else {
              player
                  .addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                      + "The timer is already stopped. Use "
                      + EnumChatFormatting.DARK_RED + "/zc timer start"));
            }
            break;
          case "start":
          case "resume":
            if (zealotCounter.eventHandler.perHourTimer.isSuspended()) {
              zealotCounter.eventHandler.perHourTimer.resume();
              player.addChatMessage(
                  new ChatComponentText(EnumChatFormatting.GREEN + "Session resumed."));
            } else if (zealotCounter.eventHandler.perHourTimer.isStopped()) {
              zealotCounter.eventHandler.perHourTimer.start();
              player.addChatMessage(
                  new ChatComponentText(EnumChatFormatting.GREEN + "Session started."));
            } else {
              player.addChatMessage(
                  new ChatComponentText(
                      EnumChatFormatting.RED + "The timer has already been started/resumed."));
            }
            break;
          default:
            player.addChatMessage(new ChatComponentText(
                EnumChatFormatting.RED + "Please use a valid option. To see the options, use "
                    + EnumChatFormatting.DARK_RED + "/zc"));
            break;
        }
      } else if (args.length == 2 && args[0].equalsIgnoreCase("color") && ZealotCounter
          .isInteger(args[1], 16) && args[1].length() == 6) {
        zealotCounter.color = Integer.parseInt(args[1], 16);
      } else {
        zealotCounter.openGui = "normal";
      }
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
