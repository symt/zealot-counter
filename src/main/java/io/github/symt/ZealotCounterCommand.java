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
    if (ics instanceof EntityPlayer && ZealotCounter.isInSkyblock) {
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
      } else if (args.length == 2 && args[0].equalsIgnoreCase("timer") && (Arrays
          .asList(new String[]{"start", "stop", "reset", "resume"})
          .contains(args[1].toLowerCase()))) {
        switch (args[1].toLowerCase()) {
          case "reset":
            ZealotCounter.zealotSession = 0;
            EventHandler.perHourTimer = new StopWatch();
            EventHandler.perHourTimer.start();
            player
                .addChatMessage(
                    new ChatComponentText(
                        EnumChatFormatting.GREEN + "Session reset successfully."));
            break;
          case "stop":
            if (!EventHandler.perHourTimer.isSuspended()) {
              EventHandler.perHourTimer.suspend();
              player
                  .addChatMessage(new ChatComponentText(
                      EnumChatFormatting.GREEN + "Session paused. Use "
                          + EnumChatFormatting.DARK_GREEN + "/zc timer resume"
                          + EnumChatFormatting.GREEN
                          + " to resume"));
            } else {
              player
                  .addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                      + "The timer is either started or is already paused. Use "
                      + EnumChatFormatting.DARK_RED + "/zc timer start"));
            }
            break;
          case "start":
          case "resume":
            if (EventHandler.perHourTimer.isSuspended()) {
              EventHandler.perHourTimer.resume();
              player.addChatMessage(
                  new ChatComponentText(EnumChatFormatting.GREEN + "Session resumed."));
            } else {
              EventHandler.perHourTimer.start();
              player.addChatMessage(
                  new ChatComponentText(EnumChatFormatting.GREEN + "Session started."));
            }
            break;
          default:
            player.addChatMessage(new ChatComponentText(
                EnumChatFormatting.RED + "Please use a valid option. To see the options, use "
                    + EnumChatFormatting.DARK_RED + "/zc"));
            break;
        }

      } else if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
        ZealotCounter.summoningEyes = 0;
        ZealotCounter.zealotCount = 0;
        ZealotCounter.sinceLastEye = 0;
        ZealotCounter.saveZealotInfo(0, 0, 0);
        player.addChatMessage(
            new ChatComponentText(EnumChatFormatting.GREEN + "ZealotCounter has been reset"));
      } else if (args.length == 3 && args[0].equalsIgnoreCase("autosave") && (
          args[1].equalsIgnoreCase("disable") || args[1].equalsIgnoreCase("enable"))
          && ZealotCounter.isInteger(args[2])) {
        ZealotCounter
            .scheduleFileSave(args[1].equalsIgnoreCase("enable"), Integer.parseInt(args[2]));
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
        player.addChatMessage(new ChatComponentText(
            EnumChatFormatting.DARK_PURPLE + "6. " + EnumChatFormatting.LIGHT_PURPLE
                + "autosave (enable|disable) (delay in seconds)"));
        player.addChatMessage(new ChatComponentText(
            EnumChatFormatting.DARK_PURPLE + "7. " + EnumChatFormatting.LIGHT_PURPLE
                + "timer (start|stop|resume|reset)"));
        player.addChatMessage(
            new ChatComponentText(EnumChatFormatting.DARK_GRAY + "---------------------------"));
      }
    } else if (!ZealotCounter.isInSkyblock) {
      ics.addChatMessage(
          new ChatComponentText(
              EnumChatFormatting.RED + "Please join SkyBlock to use this command."));
    }
  }

  public boolean canCommandSenderUseCommand(final ICommandSender sender) {
    return true;
  }
}
