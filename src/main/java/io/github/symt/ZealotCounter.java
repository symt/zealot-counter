package io.github.symt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = ZealotCounter.MODID, version = ZealotCounter.VERSION)
public class ZealotCounter {

  static final String MODID = "ZealotCounter";
  static final String VERSION = "1.0";
  private static final String ZEALOT_PATH = "zealotcounter.dat";
  static boolean loggedIn = false;
  static int color = 0x55FFFF;
  static String align = "left";
  static int zealotCount = 0;
  static int summoningEyes = 0;
  static int[] guiLocation = new int[2];

  private static void scheduleFileSave() {
    Executors.newSingleThreadScheduledExecutor()
        .scheduleAtFixedRate(() -> {
          if (loggedIn) {
            saveZealotInfo(zealotCount, summoningEyes);
          }
        }, 0, 2, TimeUnit.MINUTES);
  }

  static void saveZealotInfo(int zealots, int eyes) {
    new Thread(() -> {
      File zealot_file = new File(ZEALOT_PATH);
      try {
        FileWriter fw = new FileWriter(zealot_file, false);
        fw.write(zealots + "," + eyes + "," + guiLocation[0] + "," + guiLocation[1] + "," + Integer
            .toHexString(color) + "," + align);
        fw.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }).start();
  }

  static boolean isInteger(String s) {
    return isInteger(s, 10);
  }

  static boolean isInteger(String s, int radix) {
    if (s.isEmpty()) {
      return false;
    }
    for (int i = 0; i < s.length(); i++) {
      if (i == 0 && s.charAt(i) == '-') {
        if (s.length() == 1) {
          return false;
        } else {
          continue;
        }
      }
      if (Character.digit(s.charAt(i), radix) < 0) {
        return false;
      }
    }
    return true;
  }

  @EventHandler
  public void init(FMLInitializationEvent event) {
    ClientCommandHandler.instance.registerCommand(new ZealotCounterCommand());
    MinecraftForge.EVENT_BUS.register(new io.github.symt.EventHandler());
    if (new File(ZEALOT_PATH).isFile()) {
      try {
        String[] input = new BufferedReader(new FileReader(ZEALOT_PATH)).readLine().split(",");
        if (input.length == 6 && isInteger(input[0]) && isInteger(input[1]) && isInteger(input[2])
            && isInteger(input[3]) && isInteger(input[4], 16)) {
          zealotCount = Integer.parseInt(input[0]);
          summoningEyes = Integer.parseInt(input[1]);
          guiLocation = new int[]{Integer.parseInt(input[2]), Integer.parseInt(input[3])};
          color = Integer.parseInt(input[4], 16);
          align = input[5];
        } else {
          saveZealotInfo(0, 0);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      saveZealotInfo(0, 0);
    }
    scheduleFileSave();
  }
}
