package io.github.symt.ZealotCounter;

import io.github.symt.ZealotCounter.client.gui.Gui;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.commons.lang3.time.StopWatch;
import org.json.JSONObject;

public class EventHandler {

  public StopWatch perHourTimer = new StopWatch();
  private boolean firstJoin = true;
  private FontRenderer renderer = Minecraft.getMinecraft().fontRendererObj;
  private int tick = 1;
  private boolean tempSuspend = false;
  private ZealotCounter zealotCounter;
  private Set<Entity> countedEndermen = new HashSet<>();

  EventHandler(ZealotCounter zealotCounter) {
    this.zealotCounter = zealotCounter;
  }

  /*
   * The following two events (onMobDeath and onHit) are *mostly* courtesy of BiscuitDevelopment & SkyblockAddons.
   */
  @SubscribeEvent
  public void onMobDeath(LivingDeathEvent e) {
    if (e.entity instanceof EntityEnderman) {
      if (countedEndermen.remove(e.entity)) {
        if (perHourTimer.isStarted() && !perHourTimer.isSuspended()) {
          zealotCounter.zealotSession++;
        }
        zealotCounter.zealotCount++;
        zealotCounter.sinceLastEye++;
      }
    }
  }

  @SubscribeEvent
  public void onHit(AttackEntityEvent e) {
    if (e.target instanceof EntityEnderman) {
      List<EntityArmorStand> stands = Minecraft.getMinecraft().theWorld
          .getEntitiesWithinAABB(EntityArmorStand.class,
              new AxisAlignedBB(e.target.posX - 1, e.target.posY, e.target.posZ - 1,
                  e.target.posX + 1, e.target.posY + 5, e.target.posZ + 1));
      if (stands.isEmpty()) {
        return;
      }

      EntityArmorStand armorStand = stands.get(0);
      if (armorStand.hasCustomName() && armorStand.getCustomNameTag().contains("Zealot")) {
        countedEndermen.add(e.target);
      }
    }
  }

  @SubscribeEvent
  public void onChatMessageReceived(ClientChatReceivedEvent e) {
    if (Utils.stripString(e.message.getUnformattedText())
        .equals("A special Zealot has spawned nearby!")) {
      zealotCounter.summoningEyes++;
      zealotCounter.sinceLastEye = 0;
    } else if (Utils.stripString(e.message.getUnformattedText())
        .startsWith("You are playing on profile: ")) {
      String nextSetup = Minecraft.getMinecraft().thePlayer.getUniqueID() + ":" + Utils.stripString(
          e.message.getUnformattedText())
          .split(" ")[5];
      if (!nextSetup.equalsIgnoreCase(zealotCounter.currentSetup)) {
        zealotCounter.updateInfoWithCurrentSetup(zealotCounter.currentSetup.split(":"), nextSetup);
      }
    }
  }

  @SubscribeEvent
  public void onTick(TickEvent.ClientTickEvent e) {
    if (e.phase == TickEvent.Phase.START) {
      tick++;
      if (tick > 40 && Minecraft.getMinecraft() != null
          && Minecraft.getMinecraft().thePlayer != null) {
        if (Minecraft.getMinecraft().theWorld.getScoreboard().getObjectiveInDisplaySlot(1)
            != null) {
          zealotCounter.isInSkyblock = (Utils.stripString(StringUtils.stripControlCodes(
              Minecraft.getMinecraft().theWorld.getScoreboard().getObjectiveInDisplaySlot(1)
                  .getDisplayName())).startsWith("SKYBLOCK"));
        }
        if (zealotCounter.loggedIn && zealotCounter.isInSkyblock) {
          List<String> scoreboard = Utils.getSidebarLines();
          boolean found = false;
          for (String s : scoreboard) {
            String validated = Utils.stripString(s);
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
      zealotCounter.usingLabyMod = Loader.isModLoaded("labymod");
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
                    ZealotCounter.preRelease = true;
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
  public void onRenderLabymod(RenderGameOverlayEvent event) {
    if (event.type == null && isUsingLabymod() && zealotCounter.isInSkyblock) {
      renderStats(false);
    }
  }

  @SubscribeEvent
  public void onRenderRegular(RenderGameOverlayEvent.Post e) {
    if ((!isUsingLabymod() || Minecraft.getMinecraft().ingameGUI instanceof GuiIngameForge)) {
      if (e.type == RenderGameOverlayEvent.ElementType.EXPERIENCE
          || e.type == RenderGameOverlayEvent.ElementType.JUMPBAR) {
        renderStats(false);
      }
    }
  }

  private boolean isUsingLabymod() {
    return zealotCounter.usingLabyMod;
  }

  public void renderStats(boolean forcedRender) {
    if (forcedRender || (zealotCounter.toggled && zealotCounter.dragonsNest
        && !zealotCounter.currentSetup
        .equals(""))) {
      String zealotEye =
          "Zealots/Eye: " + ((zealotCounter.summoningEyes == 0) ? zealotCounter.zealotCount
              : new DecimalFormat("#.##")
                  .format(zealotCounter.zealotCount / (zealotCounter.summoningEyes * 1.0d)));
      String zealot = "Zealots: " + zealotCounter.zealotCount;
      String eye = "Eyes: " + zealotCounter.summoningEyes;
      String lastEye = "Zealots since last eye: " + zealotCounter.sinceLastEye;
      String zealotsPerHour = "Zealots/Hour: " + Math.round(zealotCounter.zealotSession / (
          ((perHourTimer.getTime() == 0) ? 1 : perHourTimer.getTime()) / 3600000d));
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