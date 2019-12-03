package io.github.symt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.json.JSONObject;

public class EventHandler {

  private boolean firstJoin = true;
  private FontRenderer renderer = Minecraft.getMinecraft().fontRendererObj;
  private int attackedEntity = -1;
  private int prevEntity = -1;
  private long lastHit = 0;

  @SubscribeEvent(priority = EventPriority.HIGH)
  public void onMobDeath(LivingDeathEvent event) {
    MovingObjectPosition objectMouseOver = Minecraft.getMinecraft().objectMouseOver;
    if (((objectMouseOver != null && objectMouseOver.typeOfHit == MovingObjectType.ENTITY
        && objectMouseOver.entityHit.getEntityId() == event.entity.getEntityId())
        || attackedEntity == event.entity.getEntityId())
        && (event.entity.getName().substring(2).equals("Enderman")
        || event.entity instanceof EntityEnderman)
        && prevEntity != event.entity.getEntityId()
        && System.currentTimeMillis() - lastHit < 150
        && ZealotCounter.dragonsNest) {
      prevEntity = event.entity.getEntityId();
      ZealotCounter.zealotCount++;
      ZealotCounter.sinceLastEye++;
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGH)
  public void onAttack(AttackEntityEvent event) {
    if (event.entity.getEntityId() == Minecraft.getMinecraft().thePlayer.getEntityId() &&
        (event.target.getName().substring(2).equals("Enderman")
            || event.target instanceof EntityEnderman) && ZealotCounter.dragonsNest) {
      attackedEntity = event.target.getEntityId();
      lastHit = System.currentTimeMillis();
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGH)
  public void onChatMessageReceived(ClientChatReceivedEvent e) {
    if (e.message.getUnformattedText().equals("A special Zealot has spawned nearby!")) {
      ZealotCounter.summoningEyes++;
      ZealotCounter.sinceLastEye = 0;
    }
  }

  @SubscribeEvent
  public void onPlayerJoinEvent(FMLNetworkEvent.ClientConnectedToServerEvent event) {
    if (firstJoin) {
      ZealotCounter.loggedIn = true;
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
                      .addChatMessage(new ChatComponentTranslation("", new Object[0]));
                  if (latestTag[i].compareTo(currentTag[i]) <= -1) {
                    Minecraft.getMinecraft().thePlayer
                        .addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN +
                            "You are currently on a pre-release build of ZealotCounter. Please report any bugs that you may come across"));
                  } else if (latestTag[i].compareTo(currentTag[i]) >= 1) {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                        EnumChatFormatting.GREEN + "You are currently on version "
                            + EnumChatFormatting.DARK_GREEN + current + EnumChatFormatting.GREEN
                            + " and the latest version is " + EnumChatFormatting.DARK_GREEN
                            + latest + EnumChatFormatting.GREEN
                            + ". Please update to the latest version of ZealotCounter."));
                  }
                  Minecraft.getMinecraft().thePlayer
                      .addChatMessage(new ChatComponentTranslation("", new Object[0]));
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
    ZealotCounter.loggedIn = false;
    firstJoin = true;
  }

  @SubscribeEvent
  public void renderGameOverlayEvent(RenderGameOverlayEvent event) {
    if (event.type == RenderGameOverlayEvent.ElementType.TEXT) {
      String zealotEye =
          "Zealots/Eye: " + ((ZealotCounter.summoningEyes == 0) ? ZealotCounter.zealotCount
              : new DecimalFormat("#.##")
                  .format(ZealotCounter.zealotCount / (ZealotCounter.summoningEyes * 1.0d)));
      String zealot = "Zealots: " + ZealotCounter.zealotCount;
      String eye = "Eyes: " + ZealotCounter.summoningEyes;
      String lastEye = "Zealots since last eye: " + ZealotCounter.sinceLastEye;
      String longest =
          (lastEye.length() > zealot.length() && lastEye.length() > zealotEye.length()) ? lastEye
              : (zealot.length() > zealotEye.length()) ? zealot : zealotEye;
      if (ZealotCounter.align.equals("right")) {
        renderer.drawString(zealot, ZealotCounter.guiLocation[0] +
                renderer.getStringWidth(longest) -
                renderer.getStringWidth(zealot),
            ZealotCounter.guiLocation[1], ZealotCounter.color, true);
        renderer.drawString(eye, ZealotCounter.guiLocation[0] +
                renderer.getStringWidth(longest) -
                renderer.getStringWidth(eye),
            ZealotCounter.guiLocation[1] + renderer.FONT_HEIGHT, ZealotCounter.color, true);
        renderer.drawString(zealotEye, ZealotCounter.guiLocation[0] +
                renderer.getStringWidth(longest) -
                renderer.getStringWidth(zealotEye),
            ZealotCounter.guiLocation[1] + renderer.FONT_HEIGHT * 2, ZealotCounter.color, true);
        renderer.drawString(lastEye, ZealotCounter.guiLocation[0] +
                renderer.getStringWidth(longest) -
                renderer.getStringWidth(lastEye),
            ZealotCounter.guiLocation[1] + renderer.FONT_HEIGHT * 3, ZealotCounter.color, true);
      } else {
        renderer.drawString(zealot, ZealotCounter.guiLocation[0],
            ZealotCounter.guiLocation[1], ZealotCounter.color, true);
        renderer.drawString(eye,
            ZealotCounter.guiLocation[0],
            ZealotCounter.guiLocation[1] + renderer.FONT_HEIGHT, ZealotCounter.color, true);
        renderer.drawString(zealotEye, ZealotCounter.guiLocation[0],
            ZealotCounter.guiLocation[1] + renderer.FONT_HEIGHT * 2, ZealotCounter.color, true);
        renderer.drawString(lastEye, ZealotCounter.guiLocation[0],
            ZealotCounter.guiLocation[1] + renderer.FONT_HEIGHT * 3, ZealotCounter.color, true);
      }
    }
  }
}
