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
  public static final String VERSION = "1.3.1";
  private static final String ZEALOT_PATH = "zealotcounter.json";
  public static ZealotCounter instance;
  public String openGui = "";
  public String currentSetup = "";
  public int zealotCount = 0;
  public int summoningEyes = 0;
  public int sinceLastEye = 0;
  public boolean toggled = true;
  public EventHandler eventHandler;
  public String align = "left";
  public int[] guiLocation = new int[]{2, 2};
  int zealotSession = 0;
  boolean loggedIn = false;
  boolean usingLabyMod = false;
  boolean dragonsNest = false;
  int color = 0x55FFFF;
  String lastSetup = "";
  boolean isInSkyblock = false;
  private JSONObject zealotData;
  private ScheduledExecutorService autoSaveExecutor;

  private static boolean isInteger(String s) {
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

  private void scheduleFileSave(boolean toggle, int delay) {
    if (autoSaveExecutor != null && !autoSaveExecutor.isShutdown()) {
      autoSaveExecutor.shutdownNow();
    }
    if (toggle) {
      autoSaveExecutor = Executors.newSingleThreadScheduledExecutor();
      autoSaveExecutor.scheduleAtFixedRate(() -> {
        if (loggedIn && !currentSetup.equals("")) {
          saveSetup(currentSetup.split(" ")[0],
              currentSetup.split(" ")[1], zealotCount,
              summoningEyes, sinceLastEye);
        }
      }, 0, delay, TimeUnit.SECONDS);
    }
  }

  public void updateInfoWithCurrentSetup(String uuid, String profile) {
    if (!lastSetup.equals("")) {
      saveSetup(lastSetup.split(" ")[0], lastSetup.split(" ")[1], zealotCount, summoningEyes,
          sinceLastEye);
    }
    lastSetup = currentSetup;
    if (!zealotData.getJSONObject("player").isNull(uuid)
        && !zealotData.getJSONObject("player").getJSONObject(uuid).isNull(profile)) {
      JSONObject currentProfile = zealotData.getJSONObject("player").getJSONObject(uuid)
          .getJSONObject(profile);
      zealotCount = currentProfile.getInt("zealotCount");
      summoningEyes = currentProfile.getInt("summoningEyes");
      sinceLastEye = currentProfile.getInt("sinceLastEye");
    } else {
      saveSetup(currentSetup.split(" ")[0], currentSetup.split(" ")[1], zealotCount, summoningEyes,
          sinceLastEye);
    }
  }

  public void saveSetup(String uuid, String profile, int zealotCount, int summoningEyes,
      int sinceLastEye) {
    if (zealotData.getJSONObject("player").isNull(uuid)) {
      zealotData.getJSONObject("player").put(uuid, new JSONObject("{}"));
    }
    if (zealotData.getJSONObject("player").getJSONObject(uuid).isNull(profile)) {
      zealotData.getJSONObject("player").getJSONObject(uuid).put(profile, new JSONObject("{}"));
    }
    zealotData.getJSONObject("player").getJSONObject(uuid).getJSONObject(profile)
        .put("zealotCount", zealotCount);
    zealotData.getJSONObject("player").getJSONObject(uuid).getJSONObject(profile)
        .put("summoningEyes", summoningEyes);
    zealotData.getJSONObject("player").getJSONObject(uuid).getJSONObject(profile)
        .put("sinceLastEye", sinceLastEye);
    saveZealotInfo();
  }

  private void saveZealotInfo() {
    new Thread(() -> {
      File zealot_file = new File(ZEALOT_PATH);
      try {
        FileWriter fw = new FileWriter(zealot_file, false);
        zealotData.getJSONObject("player").put("color", Integer.toHexString(color));
        zealotData.getJSONObject("player").put("location", guiLocation);
        zealotData.getJSONObject("player").put("align", align);
        fw.write(zealotData.toString());
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
        zealotData = new JSONObject(
            IOUtils.toString(new BufferedReader(new FileReader(ZEALOT_PATH))));
        guiLocation = new int[]{
            Integer.parseInt(
                zealotData.getJSONObject("player").getJSONArray("location").toList().toArray()[0]
                    .toString()),
            Integer.parseInt(
                zealotData.getJSONObject("player").getJSONArray("location").toList().toArray()[1]
                    .toString())
        };
        color = Integer.parseInt(zealotData.getJSONObject("player").getString("color"), 16);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else if (new File("zealotcounter.dat").isFile()) {
      try {
        String[] input = new BufferedReader(new FileReader("zealotcounter.dat")).readLine()
            .split(",");
        zealotData = new JSONObject(
            "{\"player\":{\"color\": \"ff00ff\", \"location\": [2, 2], \"align\": \"left\"}}");
        if (input.length == 7 && isInteger(input[0]) && isInteger(input[1]) && isInteger(input[2])
            && isInteger(input[3]) && isInteger(input[4]) && isInteger(input[5], 16)) {
          zealotCount = Integer.parseInt(input[0]);
          summoningEyes = Integer.parseInt(input[1]);
          sinceLastEye = Integer.parseInt(input[2]);
          guiLocation = new int[]{Integer.parseInt(input[3]), Integer.parseInt(input[4])};
          color = Integer.parseInt(input[5], 16);
          align = input[6];
        } else {
          saveZealotInfo();
        }
        new File("zealotcounter.dat").deleteOnExit();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      zealotData = new JSONObject(
          "{\"player\":{\"color\": \"ff00ff\", \"location\": [2, 2], \"align\": \"left\"}}");
      saveZealotInfo();
    }
    scheduleFileSave(true, 120);
  }

  @Mod.EventHandler
  public void postInit(FMLPostInitializationEvent event) {
    usingLabyMod = Loader.isModLoaded("labymod");
  }
}
