package io.github.symt.ZealotCounter;

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
    if (ics instanceof EntityPlayer && (zealotCounter.isInSkyblock || ZealotCounter.preRelease)) {
      EntityPlayer player = (EntityPlayer) ics;
      if (args.length == 3 && args[0].equalsIgnoreCase("location") && Utils
          .isInteger(args[1]) && Utils.isInteger(args[2])) {
        zealotCounter.guiLocation = new int[]{Integer.parseInt(args[1]), Integer.parseInt(args[2])};
        zealotCounter.saveZealotInfo();
      } else if (args.length == 2 && args[0].equalsIgnoreCase("align")) {
        zealotCounter.align = (args[1].equalsIgnoreCase("right")) ? "right" : "left";
      } else if (args.length == 2 && args[0].equalsIgnoreCase("color") && Utils
          .isInteger(args[1], 16) && args[1].length() == 6) {
        zealotCounter.color = Integer.parseInt(args[1], 16);
      } else if (args.length == 1 && args[0].equalsIgnoreCase("toggle")) {
        zealotCounter.toggled ^= true;
        player.addChatMessage(
            new ChatComponentText(EnumChatFormatting.GREEN + "ZealotCounter has been toggled "
                + EnumChatFormatting.DARK_GREEN + (zealotCounter.toggled ? "on" : "off")));
      } else if (args.length == 2 && args[0].equalsIgnoreCase("timer") && (Arrays
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
            if (!zealotCounter.eventHandler.perHourTimer.isSuspended()
                && !zealotCounter.eventHandler.perHourTimer
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
      } else if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
        zealotCounter.summoningEyes = 0;
        zealotCounter.zealotCount = 0;
        zealotCounter.sinceLastEye = 0;
        zealotCounter.saveZealotInfo();
        player.addChatMessage(
            new ChatComponentText(EnumChatFormatting.GREEN + "ZealotCounter has been reset"));
      } else if (args.length == 1 && args[0].equalsIgnoreCase("save")) {
        zealotCounter.saveZealotInfo();
        player.addChatMessage(
            new ChatComponentText(EnumChatFormatting.GREEN + "ZealotCounter has been saved"));
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
