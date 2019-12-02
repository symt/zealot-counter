package io.github.symt;

import java.text.DecimalFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class EventHandler {

  private boolean firstJoin = true;
  private FontRenderer renderer = Minecraft.getMinecraft().fontRendererObj;
  private int attackedEntity = -1;
  private int prevEntity = -1;

  @SubscribeEvent(priority = EventPriority.HIGH)
  public void onMobDeath(LivingDeathEvent event) {
    MovingObjectPosition objectMouseOver = Minecraft.getMinecraft().objectMouseOver;
    if (((objectMouseOver != null && objectMouseOver.typeOfHit == MovingObjectType.ENTITY
        && objectMouseOver.entityHit.getEntityId() == event.entity.getEntityId())
        || attackedEntity == event.entity.getEntityId())
        && (event.entity.getName().substring(2).equals("Enderman")
        || event.entity instanceof EntityEnderman)
        && prevEntity != event.entity.getEntityId()) {
      prevEntity = event.entity.getEntityId();
      ZealotCounter.zealotCount++;
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGH)
  public void onAttack(AttackEntityEvent event) {
    if (Minecraft.getMinecraft().thePlayer.worldObj.isRemote
        && event.entity.getEntityId() == Minecraft.getMinecraft().thePlayer.getEntityId() &&
        (event.target.getName().substring(2).equals("Enderman")
            || event.target instanceof EntityEnderman)) {
      attackedEntity = event.target.getEntityId();
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGH)
  public void onChatMessageReceived(ClientChatReceivedEvent e) {
    if (e.message.getUnformattedText().equals("A special Zealot has spawned nearby!")) {
      ZealotCounter.summoningEyes++;
    }
  }

  @SubscribeEvent
  public void onPlayerJoinEvent(FMLNetworkEvent.ClientConnectedToServerEvent event) {
    if (firstJoin) {
      ZealotCounter.loggedIn = true;
      firstJoin = false;
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
      if (ZealotCounter.align.equals("right")) {
        renderer.drawString(zealot, ZealotCounter.guiLocation[0] +
                renderer.getStringWidth(zealotEye) -
                renderer.getStringWidth(zealot),
            ZealotCounter.guiLocation[1], ZealotCounter.color, true);
        renderer.drawString(eye,
            ZealotCounter.guiLocation[0] +
                renderer.getStringWidth(zealotEye) -
                renderer.getStringWidth(eye),
            ZealotCounter.guiLocation[1] + renderer.FONT_HEIGHT, ZealotCounter.color, true);
        renderer.drawString(zealotEye, ZealotCounter.guiLocation[0],
            ZealotCounter.guiLocation[1] + renderer.FONT_HEIGHT * 2, ZealotCounter.color, true);
      } else {
        renderer.drawString(zealot, ZealotCounter.guiLocation[0],
            ZealotCounter.guiLocation[1], ZealotCounter.color, true);
        renderer.drawString(eye,
            ZealotCounter.guiLocation[0],
            ZealotCounter.guiLocation[1] + renderer.FONT_HEIGHT, ZealotCounter.color, true);
        renderer.drawString(zealotEye, ZealotCounter.guiLocation[0],
            ZealotCounter.guiLocation[1] + renderer.FONT_HEIGHT * 2, ZealotCounter.color, true);
      }
    }
  }
}
