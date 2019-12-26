package io.github.symt;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

@Mod(modid = ZealotCounter.MODID, version = ZealotCounter.VERSION)
public class ZealotCounter {

  public static final String MODID = "ZealotCounter";
  public static final String VERSION = "1.2.3";
  private static final String ZEALOT_PATH = "zealotcounter.json";
  public static ZealotCounter instance;
  boolean loggedIn = false;
  boolean usingLabyMod = false;
  boolean dragonsNest = false;
  boolean toggled = true;
  boolean openGui = false;
  int color = 0x55FFFF;
  String align = "left";
  int zealotCount = 0;
  int summoningEyes = 0;
  int sinceLastEye = 0;
  int zealotSession = 0;
  boolean isInSkyblock = false;
  int[] guiLocation = new int[]{2, 2};
  EventHandler eventHandler;
  private ScheduledExecutorService autoSaveExecutor;

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

  void scheduleFileSave(boolean toggle, int delay) {
    if (autoSaveExecutor != null && !autoSaveExecutor.isShutdown()) {
      autoSaveExecutor.shutdownNow();
    }
    if (toggle) {
      autoSaveExecutor = Executors.newSingleThreadScheduledExecutor();
      autoSaveExecutor.scheduleAtFixedRate(() -> {
        if (loggedIn && isInSkyblock) {
          saveZealotInfo(zealotCount, summoningEyes, sinceLastEye);
        }
      }, 0, delay, TimeUnit.SECONDS);
    }
  }

  void saveZealotInfo(int zealots, int eyes, int last) {
    new Thread(() -> {
      File zealot_file = new File(ZEALOT_PATH);
      try {
        FileWriter fw = new FileWriter(zealot_file, false);
        fw.write(
            zealots + "," + eyes + "," + last + "," + guiLocation[0] + "," + guiLocation[1] + ","
                + Integer
                .toHexString(color) + "," + align);
        fw.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }).start();
  }

  List<String> getSidebarLines() {
    List<String> lines = new ArrayList<>();
    Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
    if (scoreboard == null) {
      return lines;
    }

    ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);

    if (objective == null) {
      return lines;
    }

    Collection<Score> scores = scoreboard.getSortedScores(objective);
    List<Score> list = Lists.newArrayList(scores.stream()
        .filter(input -> input != null && input.getPlayerName() != null && !input.getPlayerName()
            .startsWith("#"))
        .collect(Collectors.toList()));

    if (list.size() > 15) {
      scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
    } else {
      scores = list;
    }

    for (Score score : scores) {
      ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
      lines.add(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()));
    }

    return lines;
  }

  @Mod.EventHandler
  public void init(FMLInitializationEvent event) {
    instance = this;
    eventHandler = new EventHandler(this);
    ClientCommandHandler.instance.registerCommand(new ZealotCounterCommand(this));
    MinecraftForge.EVENT_BUS.register(eventHandler);
    if (new File(ZEALOT_PATH).isFile()) {
      try {
        JSONObject data = new JSONObject(
            IOUtils.toString(new BufferedReader(new FileReader(ZEALOT_PATH))));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    scheduleFileSave(true, 120);
  }

  @Mod.EventHandler
  public void postInit(FMLPostInitializationEvent event) {
    usingLabyMod = Loader.isModLoaded("labymod");
  }
}
