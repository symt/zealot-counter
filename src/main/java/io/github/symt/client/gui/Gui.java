package io.github.symt.client.gui;

import io.github.symt.ZealotCounter;
import java.awt.Color;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public class Gui extends GuiScreen {


  static final ResourceLocation buttonTemplate = new ResourceLocation(ZealotCounter.MODID,
      "textures/buttonTemplate.png");

  static void drawCenteredString(String text, int x, int y, int color, double scale) {
    GlStateManager.pushMatrix();
    GlStateManager.scale(scale, scale, 1);
    Minecraft.getMinecraft().fontRendererObj.drawString(text,
        (int) (x / scale) - Minecraft.getMinecraft().fontRendererObj.getStringWidth(text) / 2,
        (int) (y / scale), color);
    GlStateManager.popMatrix();
  }

  @Override
  public void initGui() {
    super.initGui();
    this.buttonList.add(new Button(1, this.width / 2 - 26, this.height / 2 - 13, 52, 26, "Reset"));
    this.buttonList
        .add(new Button(2, this.width / 2 - 26, this.height / 2 - 13 - 30, 52, 26, "Move"));
    this.buttonList
        .add(new Button(3, this.width / 2 - 26, this.height / 2 - 13 - 60, 52, 26, "Color"));
    this.buttonList
        .add(new Button(4, this.width / 2 - 26, this.height / 2 - 13 + 30, 52, 26, "Save"));
    this.buttonList
        .add(new Button(5, this.width / 2 - 26, this.height / 2 - 13 + 60, 52, 26, "Toggle"));
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawRect(0, 0, this.width, this.height, new Color(0, 0, 0, 200).getRGB());
    drawCenteredString("ZealotCounter", width / 2, 15, 0xFFFFFF, 1F);

    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  @Override
  protected void actionPerformed(GuiButton guiButton) throws IOException {

    switch (guiButton.id) {
      case 1:
        // Reset
        if (ZealotCounter.instance.currentSetup.equals("")) {
          Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
              EnumChatFormatting.RED
                  + "You are not on a known profile. Please join SkyBlock so the mod can detect which profile you're using."));
        } else {
          ZealotCounter.instance.zealotCount = 0;
          ZealotCounter.instance.summoningEyes = 0;
          ZealotCounter.instance.sinceLastEye = 0;
          ZealotCounter.instance
              .saveSetup(ZealotCounter.instance.currentSetup.split(" ")[0],
                  ZealotCounter.instance.currentSetup.split(" ")[1], 0, 0, 0);
        }
        break;
      case 2:
        // Move
        ZealotCounter.instance.openGui = "location";
        break;
      case 3:
        // Color
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
            EnumChatFormatting.RED
                + "The color spinner isn't done yet and will be out in the next update. In the meantime, use "
                + EnumChatFormatting.DARK_RED + "/zc color (hex color)"));
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
            EnumChatFormatting.RED + "Example: " + EnumChatFormatting.DARK_RED
                + "/zc color ff0000"));
        // ZealotCounter.instance.openGui = "color";
        break;
      case 4:
        // Save
        if (ZealotCounter.instance.currentSetup.equals("")) {
          Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
              EnumChatFormatting.RED
                  + "You are not on a known profile. Please join SkyBlock so the mod can detect which profile you're using."));
        } else {
          ZealotCounter.instance.saveSetup(ZealotCounter.instance.currentSetup.split(" ")[0],
              ZealotCounter.instance.currentSetup.split(" ")[1], ZealotCounter.instance.zealotCount,
              ZealotCounter.instance.summoningEyes, ZealotCounter.instance.sinceLastEye);
        }
        break;
      case 5:
        // Toggle
        ZealotCounter.instance.toggled ^= true;
        break;
      default:
    }
    Minecraft.getMinecraft().displayGuiScreen(null);
    super.actionPerformed(guiButton);
  }

  public static class MoveGui extends GuiScreen {

    int moveX = ZealotCounter.instance.guiLocation[0], moveY = ZealotCounter.instance.guiLocation[1];
    boolean moving = true;
    long pressTime = 0;

    @Override
    public void initGui() {
      super.initGui();

      buttonList.add(new Button(0, moveX, moveY, 125, 40, ""));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      drawRect(0, 0, this.width, this.height, new Color(0, 0, 0, 125).getRGB());
      buttonList.get(0).xPosition = moveX;
      buttonList.get(0).yPosition = moveY;

      ZealotCounter.instance.eventHandler.renderStats(true);
      GlStateManager.color(1, 1, 1, 0F);
      super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
      return false;
    }

    @Override
    protected void actionPerformed(GuiButton guiButton) throws IOException {
      if (guiButton.id == 0) {
        moving = true;
        if (pressTime == 0) {
          pressTime = System.currentTimeMillis();
        } else {
          if (System.currentTimeMillis() - pressTime <= 500) {
            ZealotCounter.instance.align =
                (ZealotCounter.instance.align.equals("left")) ? "right" : "left";
          }
          pressTime = 0;
        }
      }
      super.actionPerformed(guiButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton,
        long timeSinceLastClick) {
      super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
      if (moving) {
        moveX = mouseX;
        moveY = mouseY;
        ZealotCounter.instance.guiLocation = new int[]{moveX, moveY};
      }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
      super.mouseReleased(mouseX, mouseY, state);
      moving = false;
    }
  }

  public static class ColorGui extends GuiScreen {

    @Override
    public void initGui() {
      super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      drawRect(0, 0, this.width, this.height, new Color(0, 0, 0, 50).getRGB());

      super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton guiButton) throws IOException {
      super.actionPerformed(guiButton);
    }
  }
}