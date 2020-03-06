package io.github.symt.ZealotCounter;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.StringUtils;

public class Utils {

  static String stripString(String s) {
    char[] nonValidatedString = StringUtils.stripControlCodes(s).toCharArray();
    StringBuilder validated = new StringBuilder();
    for (char a : nonValidatedString) {
      if ((int) a < 127 && (int) a > 20) {
        validated.append(a);
      }
    }
    return validated.toString();
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

  static List<String> getSidebarLines() {
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
    List<Score> list = scores.stream()
        .filter(input -> input != null && input.getPlayerName() != null && !input.getPlayerName()
            .startsWith("#")).collect(Collectors.toList());

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

  @SuppressWarnings("ResultOfMethodCallIgnored")
  static void saveConfigFile(File configFile, String toSave) {
    try {
      if (!configFile.isFile()) {
        configFile.createNewFile();
      }
      Files.write(Paths.get(configFile.getAbsolutePath()),
          toSave.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
