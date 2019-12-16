package io.github.symt.client.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

public class Gui extends GuiScreen {

  Dimension screenSize;

  @Override
  public void initGui() {
    screenSize = new Dimension(Minecraft.getMinecraft().displayWidth,
        Minecraft.getMinecraft().displayHeight);

    Dimension normalizedButtonSize = new Dimension((int)(screenSize.getWidth()/1920 * 150), (int)(screenSize.getHeight()/1080 * 50));

    buttonList.add(new GuiButton(0, (int)(screenSize.getWidth()/3 - normalizedButtonSize.getWidth()/2), (int)(screenSize.getHeight()/3 - normalizedButtonSize.getHeight()/2), (int)normalizedButtonSize.getWidth(), (int)normalizedButtonSize.getHeight(), "Click me"));
    super.initGui();
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawRect(0, 0, screenSize.width, screenSize.height, new Color(0, 0, 0, 255 / 2).getRGB());
    drawCenteredString("ZealotCounter", 15, 0xFFFFFF, 1F);

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
