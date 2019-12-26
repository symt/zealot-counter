package io.github.symt.client.gui;

import io.github.symt.ZealotCounter;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class Gui extends GuiScreen {

  Dimension screenSize;
  ZealotCounter zealotCounter;

  Map<String, ResourceLocation> textures = new HashMap<String, ResourceLocation>() {
    {
      put("reset", new ResourceLocation(ZealotCounter.MODID, "textures/buttons/reset.png"));
      put("move", new ResourceLocation(ZealotCounter.MODID, "textures/buttons/move.png"));
      put("import", new ResourceLocation(ZealotCounter.MODID, "textures/buttons/import.png"));
      put("save", new ResourceLocation(ZealotCounter.MODID, "textures/buttons/save.png"));
      put("color", new ResourceLocation(ZealotCounter.MODID, "textures/buttons/color.png"));
    }
  };

  @Override
  public void initGui() {
    zealotCounter = ZealotCounter.instance;
    screenSize = new Dimension(Minecraft.getMinecraft().displayWidth,
        Minecraft.getMinecraft().displayHeight);
    super.initGui();
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawRect(0, 0, screenSize.width, screenSize.height, new Color(0, 0, 0, 255 / 2).getRGB());
    drawCenteredString("ZealotCounter", 15, 0xFFFFFF, 1F);
    Minecraft.getMinecraft().renderEngine.bindTexture(textures.get("reset"));
    drawTexturedModalRect(0, 0, 0,0, 200, 88);

    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  @Override
  protected void actionPerformed(GuiButton guiButton) throws IOException {
    super.actionPerformed(guiButton);
  }

  private void drawCenteredString(String text, int y, int color, double scale) {
    double x = width / 2;
    GlStateManager.pushMatrix();
    GlStateManager.scale(scale, scale, 1);
    Minecraft.getMinecraft().fontRendererObj.drawString(text,
        (int) (x / scale) - Minecraft.getMinecraft().fontRendererObj.getStringWidth(text)/2, (int) (y / scale), color);
    GlStateManager.popMatrix();
  }
}
