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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = ZealotCounter.MODID, version = ZealotCounter.VERSION)
public class ZealotCounter {

  static final String MODID = "ZealotCounter";
  static final String VERSION = "1.0.2";
  private static final String ZEALOT_PATH = "zealotcounter.dat";
  static boolean loggedIn = false;
  static boolean dragonsNest = false;
  static int color = 0x55FFFF;
  static String align = "left";
  static int zealotCount = 0;
  static int summoningEyes = 0;
  static int sinceLastEye = 0;
  static int[] guiLocation = new int[2];

  private static void scheduleNestCheck() {
    Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
      if (loggedIn) {
        List<String> scoreboard = getSidebarLines();
        boolean found = false;
        for (String s : scoreboard) {
          if (StringUtils.stripControlCodes(s).contains("Dragon's") && StringUtils.stripControlCodes(s).contains("Nest")) {
            found = true;
            break;
          }
        }
        dragonsNest = found;
      }
    }, 0, 5, TimeUnit.SECONDS);
  }

  private static void scheduleFileSave() {
    Executors.newSingleThreadScheduledExecutor()
        .scheduleAtFixedRate(() -> {
          if (loggedIn) {
            saveZealotInfo(zealotCount, summoningEyes, sinceLastEye);
          }
        }, 0, 2, TimeUnit.MINUTES);
  }

  static void saveZealotInfo(int zealots, int eyes, int last) {
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

  private static List<String> getSidebarLines() {
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

  @EventHandler
  public void init(FMLInitializationEvent event) {
    ClientCommandHandler.instance.registerCommand(new ZealotCounterCommand());
    MinecraftForge.EVENT_BUS.register(new io.github.symt.EventHandler());
    if (new File(ZEALOT_PATH).isFile()) {
      try {
        String[] input = new BufferedReader(new FileReader(ZEALOT_PATH)).readLine().split(",");
        if (input.length == 7 && isInteger(input[0]) && isInteger(input[1]) && isInteger(input[2])
            && isInteger(input[3]) && isInteger(input[4]) && isInteger(input[5], 16)) {
          zealotCount = Integer.parseInt(input[0]);
          summoningEyes = Integer.parseInt(input[1]);
          sinceLastEye = Integer.parseInt(input[2]);
          guiLocation = new int[]{Integer.parseInt(input[3]), Integer.parseInt(input[4])};
          color = Integer.parseInt(input[5], 16);
          align = input[6];
        } else {
          saveZealotInfo(0, 0, 0);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      saveZealotInfo(0, 0, 0);
    }
    scheduleFileSave();
    scheduleNestCheck();
  }
}
