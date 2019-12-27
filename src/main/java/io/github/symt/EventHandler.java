package io.github.symt;

import io.github.symt.client.gui.Gui;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.commons.lang3.time.StopWatch;
import org.json.JSONObject;

public class EventHandler {

  StopWatch perHourTimer = new StopWatch();
  private boolean firstJoin = true;
  private FontRenderer renderer = Minecraft.getMinecraft().fontRendererObj;
  private int attackedEntity = -1;
  private int prevEntity = -1;
  private int tick = 1;
  private boolean tempSuspend = false;
  private ZealotCounter zealotCounter;

  EventHandler(ZealotCounter zealotCounter) {
    this.zealotCounter = zealotCounter;
  }

  @SubscribeEvent
  public void playSoundEvent(PlaySoundEvent event) {
    System.out.println(event.sound.getPitch());
  }

  @SubscribeEvent
  public void onMobDeath(LivingDeathEvent event) {
    MovingObjectPosition objectMouseOver = Minecraft.getMinecraft().objectMouseOver;
    if (((objectMouseOver != null && objectMouseOver.typeOfHit == MovingObjectType.ENTITY
        && objectMouseOver.entityHit.getEntityId() == event.entity.getEntityId())
        || attackedEntity == event.entity.getEntityId())
        && ((event.entity.getName().length() > 2 && event.entity.getName().substring(2)
        .equals("Enderman"))
        || event.entity instanceof EntityEnderman)
        && prevEntity != event.entity.getEntityId()
        && zealotCounter.dragonsNest) {
      prevEntity = event.entity.getEntityId();
      if (perHourTimer.isStarted() && !perHourTimer.isSuspended()) {
        zealotCounter.zealotSession++;
      }
      zealotCounter.zealotCount++;
      zealotCounter.sinceLastEye++;
    }
  }

  @SubscribeEvent
  public void onAttack(AttackEntityEvent event) {
    if (event.entity.getEntityId() == Minecraft.getMinecraft().thePlayer.getEntityId() &&
        ((event.target.getName().length() > 2 && event.target.getName().substring(2)
            .equals("Enderman"))
            || event.target instanceof EntityEnderman) && zealotCounter.dragonsNest) {
      attackedEntity = event.target.getEntityId();
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onChatMessageReceived(ClientChatReceivedEvent e) {
    if (stripString(e.message.getUnformattedText()).equals("A special Zealot has spawned nearby!")) {
      zealotCounter.summoningEyes++;
      zealotCounter.sinceLastEye = 0;
    } else if (stripString(e.message.getUnformattedText()).startsWith("You are playing on profile: ")) {
      zealotCounter.currentSetup =
          Minecraft.getMinecraft().thePlayer.getUniqueID() + " " + stripString(e.message.getUnformattedText())
              .split(" ")[5];
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onTick(TickEvent.ClientTickEvent e) {
    if (e.phase == TickEvent.Phase.START) {
      tick++;
      if (tick > 40 && Minecraft.getMinecraft() != null
          && Minecraft.getMinecraft().thePlayer != null) {
        if (Minecraft.getMinecraft().theWorld.getScoreboard().getObjectiveInDisplaySlot(1)
            != null) {
          zealotCounter.isInSkyblock = (stripString(StringUtils.stripControlCodes(
              Minecraft.getMinecraft().theWorld.getScoreboard().getObjectiveInDisplaySlot(1)
                  .getDisplayName())).startsWith("SKYBLOCK"));
        }
        if (!zealotCounter.lastSetup.equals(zealotCounter.currentSetup)) {
          zealotCounter.updateInfoWithCurrentSetup(zealotCounter.currentSetup.split(" ")[0],
              zealotCounter.currentSetup.split(" ")[1]);
        }
        if (zealotCounter.loggedIn && zealotCounter.isInSkyblock) {
          List<String> scoreboard = zealotCounter.getSidebarLines();
          boolean found = false;
          for (String s : scoreboard) {
            String validated = stripString(s);
            if (validated.contains("Dragon's Nest")) {
              if (tempSuspend) {
                tempSuspend = false;
                if (perHourTimer.isSuspended() && !perHourTimer
                    .isStopped()) {
                  perHourTimer.resume();
                }
              }
              found = true;
              break;
            }
          }
          if (!found && !tempSuspend && perHourTimer.isStarted()
              && !perHourTimer.isSuspended()) {
            perHourTimer.suspend();
            tempSuspend = true;
          }
          zealotCounter.dragonsNest = found;
        } else if (!zealotCounter.isInSkyblock) {
          if (perHourTimer.isStarted() && !perHourTimer.isSuspended()) {
            perHourTimer.suspend();
          }
          zealotCounter.dragonsNest = false;
        }
        tick = 0;
      }
    }
  }

  @SubscribeEvent
  public void onAttemptedRender(TickEvent.RenderTickEvent e) {
    switch (zealotCounter.openGui) {
      case "normal":
        Minecraft.getMinecraft().displayGuiScreen(new Gui());
        break;
      case "location":
        Minecraft.getMinecraft().displayGuiScreen(new Gui.MoveGui());
        break;
      case "color":
        Minecraft.getMinecraft().displayGuiScreen(new Gui.ColorGui());
        break;
    }
    zealotCounter.openGui = "";
  }

  @SubscribeEvent
  public void onPlayerJoinEvent(FMLNetworkEvent.ClientConnectedToServerEvent event) {
    if (firstJoin) {
      zealotCounter.loggedIn = true;
      firstJoin = false;
      new ScheduledThreadPoolExecutor(1).schedule(() -> {
        try {
          URL url = new URL("https://api.github.com/repos/symt/zealot-counter/releases/latest");
          HttpURLConnection con = (HttpURLConnection) url.openConnection();
          con.setRequestMethod("GET");
          con.setRequestProperty("User-Agent", "Mozilla/5.0");
          int responseCode = con.getResponseCode();

          if (responseCode == 200) {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
              response.append(inputLine);
            }
            in.close();

            JSONObject json = new JSONObject(response.toString());
            String latest = ((String) json.get("tag_name"));
            String[] latestTag = latest.split("\\.");
            String current = ZealotCounter.VERSION;
            String[] currentTag = current.split("\\.");

            if (latestTag.length == 3 && currentTag.length == 3) {
              for (int i = 0; i < latestTag.length; i++) {
                if (latestTag[i].compareTo(currentTag[i]) != 0) {
                  Minecraft.getMinecraft().thePlayer
                      .addChatMessage(new ChatComponentTranslation(""));
                  if (latestTag[i].compareTo(currentTag[i]) <= -1) {
                    Minecraft.getMinecraft().thePlayer
                        .addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN +
                            "You are currently on a pre-release build of ZealotCounter. Please report any bugs that you may come across"));
                  } else if (latestTag[i].compareTo(currentTag[i]) >= 1) {
                    ChatComponentText updateLink = new ChatComponentText(
                        EnumChatFormatting.DARK_GREEN + "" + EnumChatFormatting.BOLD
                            + "[CLICK HERE]");
                    updateLink
                        .setChatStyle(updateLink.getChatStyle().setChatClickEvent(new ClickEvent(
                            Action.OPEN_URL,
                            "https://github.com/symt/zealot-counter/releases/latest")));
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                        EnumChatFormatting.GREEN + "You are currently on version "
                            + EnumChatFormatting.DARK_GREEN + current + EnumChatFormatting.GREEN
                            + " and the latest version is " + EnumChatFormatting.DARK_GREEN
                            + latest + EnumChatFormatting.GREEN
                            + ". Please update to the latest version of ZealotCounter. ")
                        .appendSibling(updateLink));
                  }
                  Minecraft.getMinecraft().thePlayer
                      .addChatMessage(new ChatComponentTranslation(""));
                  break;
                }
              }
            }
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }, 3, TimeUnit.SECONDS);
    }
  }

  @SubscribeEvent
  public void onPlayerLeaveEvent(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
    zealotCounter.loggedIn = false;
    firstJoin = true;
  }

  @SubscribeEvent
  public void renderLabymodOverlay(RenderGameOverlayEvent event) {
    if (event.type == null && isUsingLabymod() && zealotCounter.isInSkyblock) {
      renderStats();
    }
  }

  @SubscribeEvent
  public void renderGameOverlayEvent(RenderGameOverlayEvent.Post event) {
    if ((event.type == RenderGameOverlayEvent.ElementType.EXPERIENCE
        || event.type == RenderGameOverlayEvent.ElementType.JUMPBAR) && zealotCounter.isInSkyblock
        && !isUsingLabymod()) {
      renderStats();
    }
  }

  private String stripString(String s) {
    char[] nonValidatedString = StringUtils.stripControlCodes(s).toCharArray();
    StringBuilder validated = new StringBuilder();
    for (char a : nonValidatedString) {
      if ((int) a < 127 && (int) a > 20) {
        validated.append(a);
      }
    }
    return validated.toString();
  }

  private boolean isUsingLabymod() {
    return zealotCounter.usingLabyMod;
  }

  private void renderStats() {
    if (zealotCounter.toggled && zealotCounter.dragonsNest && !zealotCounter.currentSetup
        .equals("")) {
      String zealotEye =
          "Zealots/Eye: " + ((zealotCounter.summoningEyes == 0) ? zealotCounter.zealotCount
              : new DecimalFormat("#.##")
                  .format(zealotCounter.zealotCount / (zealotCounter.summoningEyes * 1.0d)));
      String zealot = "Zealots: " + zealotCounter.zealotCount;
      String eye = "Eyes: " + zealotCounter.summoningEyes;
      String lastEye = "Zealots since last eye: " + zealotCounter.sinceLastEye;
      String zealotsPerHour = "Zealots/Hour: " + Math.round(zealotCounter.zealotSession / (
          ((perHourTimer.getTime() == 0) ? 1 : perHourTimer.getTime()) / 3600000d));
      String chanceOfEye =
          "Current drop rate: " + (1 + Math.floor(zealotCounter.zealotCount / 420d)) + "/420";
      String longest =
          (lastEye.length() > zealot.length() && lastEye.length() > zealotEye.length()) ? lastEye
              : (zealot.length() > zealotEye.length()) ? zealot : zealotEye;
      if (zealotCounter.align.equals("right")) {
        renderer.drawString(eye, zealotCounter.guiLocation[0] +
                renderer.getStringWidth(longest) -
                renderer.getStringWidth(eye),
            zealotCounter.guiLocation[1], zealotCounter.color, true);
        renderer.drawString(zealot, zealotCounter.guiLocation[0] +
                renderer.getStringWidth(longest) -
                renderer.getStringWidth(zealot),
            zealotCounter.guiLocation[1] + renderer.FONT_HEIGHT, zealotCounter.color, true);
        renderer.drawString(zealotEye, zealotCounter.guiLocation[0] +
                renderer.getStringWidth(longest) -
                renderer.getStringWidth(zealotEye),
            zealotCounter.guiLocation[1] + renderer.FONT_HEIGHT * 2, zealotCounter.color, true);
        renderer.drawString(lastEye, zealotCounter.guiLocation[0] +
                renderer.getStringWidth(longest) -
                renderer.getStringWidth(lastEye),
            zealotCounter.guiLocation[1] + renderer.FONT_HEIGHT * 3, zealotCounter.color, true);
        if (zealotCounter.zealotSession != 0 && perHourTimer.isStarted()) {
          renderer.drawString(zealotsPerHour, zealotCounter.guiLocation[0] +
                  renderer.getStringWidth(longest) -
                  renderer.getStringWidth(zealotsPerHour),
              zealotCounter.guiLocation[1] + renderer.FONT_HEIGHT * 4, zealotCounter.color, true);
        }
      } else {
        renderer.drawString(eye, zealotCounter.guiLocation[0],
            zealotCounter.guiLocation[1], zealotCounter.color, true);
        renderer.drawString(zealot, zealotCounter.guiLocation[0],
            zealotCounter.guiLocation[1] + renderer.FONT_HEIGHT, zealotCounter.color, true);
        renderer.drawString(zealotEye, zealotCounter.guiLocation[0],
            zealotCounter.guiLocation[1] + renderer.FONT_HEIGHT * 2, zealotCounter.color, true);
        renderer.drawString(lastEye, zealotCounter.guiLocation[0],
            zealotCounter.guiLocation[1] + renderer.FONT_HEIGHT * 3, zealotCounter.color, true);
        if (zealotCounter.zealotSession != 0 && perHourTimer.isStarted()) {
          renderer.drawString(zealotsPerHour, zealotCounter.guiLocation[0],
              zealotCounter.guiLocation[1] + renderer.FONT_HEIGHT * 4, zealotCounter.color, true);
        }
      }
    }
  }
}
