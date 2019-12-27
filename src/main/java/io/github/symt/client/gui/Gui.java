package io.github.symt.client.gui;

import io.github.symt.ZealotCounter;
import java.awt.Color;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
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
        ZealotCounter.instance.zealotCount = 0;
        ZealotCounter.instance.summoningEyes = 0;
        ZealotCounter.instance.sinceLastEye = 0;
        ZealotCounter.instance
            .saveSetup(ZealotCounter.instance.currentSetup.split(" ")[0],
                ZealotCounter.instance.currentSetup.split(" ")[1], 0, 0, 0);
        break;
      case 2:
        // Move
        ZealotCounter.instance.openGui = "location";
        break;
      case 3:
        // Color
        ZealotCounter.instance.openGui = "color";
        break;
      case 4:
        // Save
        ZealotCounter.instance.saveSetup(ZealotCounter.instance.currentSetup.split(" ")[0],
            ZealotCounter.instance.currentSetup.split(" ")[1], ZealotCounter.instance.zealotCount,
            ZealotCounter.instance.summoningEyes, ZealotCounter.instance.sinceLastEye);
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