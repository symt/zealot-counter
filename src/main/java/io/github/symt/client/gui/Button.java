package io.github.symt.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class Button extends GuiButton {

  /**
   * Create a button that is assigned a feature (to toggle/change color etc.).
   */

  private String buttonText;

  Button(int buttonId, int x, int y, int width, int height, String buttonText) {
    super(buttonId, x, y, width, height, buttonText);
    this.buttonText = buttonText;
  }

  @Override
  public void drawButton(Minecraft mc, int mouseX, int mouseY) {
    if (visible) {
      mc.getTextureManager().bindTexture(Gui.buttonTemplate);
      drawModalRectWithCustomSizedTexture(xPosition, yPosition, 0, 0, width, height, width, height);
      Gui.drawCenteredString(buttonText, xPosition + width / 2,
          yPosition + height / 2 - Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT / 2,
          0xFFFFFF, 1F);
    }
  }
}