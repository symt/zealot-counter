package io.github.symt.ZealotCounter.client.gui;

import club.sk1er.elementa.components.Window;
import net.minecraft.client.gui.GuiScreen;

public class Gui extends GuiScreen {

  private Window window = new Window();

  @Override
  public void initGui() {
    super.initGui();
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    super.drawScreen(mouseX, mouseY, partialTicks);
    window.draw();
  }

  public static class MoveGui extends GuiScreen {

  }

  public static class ColorGui extends GuiScreen {

  }
}