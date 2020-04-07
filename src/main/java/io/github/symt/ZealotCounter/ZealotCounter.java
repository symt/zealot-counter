package io.github.symt.ZealotCounter;

import io.github.symt.ZealotCounter.modcore.ModCoreInstaller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.json.JSONObject;

@Mod(modid = ZealotCounter.MODID, version = ZealotCounter.VERSION)
public class ZealotCounter {

  public static final String MODID = "ZealotCounter";
  public static final String VERSION = "1.4.0";
  public static ZealotCounter instance;
  public static boolean preRelease;
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
  boolean isInSkyblock = false;
  private JSONObject zealotData;
  private File configFile;
  public static final String DEFAULT_PROFILE = "UNKNOWN_PROFILE";
  public static boolean onNewProfile = false;

  /**
   * @param userInfo array with uuid, profile name
   */
  public void updateInfoWithCurrentSetup(String[] userInfo, String nextSetup) {
    currentSetup = nextSetup;

    verifyInformation(userInfo);
    zealotData.getJSONObject("players").getJSONObject(userInfo[0]).getJSONObject(userInfo[1])
        .put("zealotCount", zealotCount)
        .put("summoningEyes", summoningEyes)
        .put("sinceLastEye", sinceLastEye);

    String[] nextInfo = nextSetup.split(":");
    verifyInformation(nextInfo);
    JSONObject nextUser = zealotData.getJSONObject("players").getJSONObject(nextInfo[0])
        .getJSONObject(nextInfo[1]);
    zealotCount = nextUser.getInt("zealotCount");
    summoningEyes = nextUser.getInt("summoningEyes");
    sinceLastEye = nextUser.getInt("sinceLastEye");
    
    saveZealotInfo();
    //a profile is not new if we know about it
    onNewProfile = false;
    
  }
  private void verifyInformation(String[] userInfo) {

    if (zealotData == null) {
      zealotData = new JSONObject().put("players", new JSONObject());
    }  
    if (!zealotData.getJSONObject("players").has(userInfo[0])) {
      zealotData.getJSONObject("players").put(userInfo[0], new JSONObject());
    }  
    if (!zealotData.getJSONObject("players").getJSONObject(userInfo[0]).has(userInfo[1])) {
    	//if we are not on a newly created profile and that profile is not in our database then it
    	//is the real name of the default profile
    	if(!userInfo[1].equals(DEFAULT_PROFILE) &&
    			zealotData.getJSONObject("players").getJSONObject(userInfo[0]).has(DEFAULT_PROFILE) &&
    			!onNewProfile) {
    		//replace placeholder profile name with real one
    		zealotData.getJSONObject("players").getJSONObject(userInfo[0]).put(userInfo[1], 
    			zealotData.getJSONObject("players").getJSONObject(userInfo[0]).getJSONObject(DEFAULT_PROFILE));
    		zealotData.getJSONObject("players").getJSONObject(userInfo[0]).remove(DEFAULT_PROFILE);
    	} else {
    		zealotData.getJSONObject("players").getJSONObject(userInfo[0])
    		  .put(userInfo[1], new JSONObject().put("zealotCount", 0)
              .put("summoningEyes", 0)
              .put("sinceLastEye", 0));
    	}
    } else {
    	JSONObject profileObject = zealotData
			.getJSONObject("players")
			.getJSONObject(userInfo[0])
			.getJSONObject(userInfo[1]);

    	zealotCount = profileObject.getInt("zealotCount");
    	summoningEyes = profileObject.getInt("summoningEyes");
    	sinceLastEye = profileObject.getInt("sinceLastEye");
    	
    }
  }

  public void saveZealotInfo() {
    new Thread(() -> {
      zealotData
          .put("guiLocation", guiLocation[0] + "," + guiLocation[1])
          .put("align", align)
          .put("color", Integer.toHexString(color))
          .put("toggled", toggled);
      Utils.saveConfigFile(configFile, zealotData.toString());
    }).start();
  }


  @Mod.EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    configFile = event.getSuggestedConfigurationFile();
    if (configFile.isFile()) {
      try {
        zealotData = new JSONObject(
            new String(Files.readAllBytes(Paths.get(configFile.getPath()))));
        String[] gui = zealotData.getString("guiLocation").split(",");
        if (gui.length == 2 && Utils.isInteger(gui[0]) && Utils.isInteger(gui[1])) {
          guiLocation = new int[]{Integer.parseInt(gui[0]), Integer.parseInt(gui[1])};
        }
        if (Utils.isInteger(zealotData.getString("color"), 16)) {
          color = Integer.parseInt(zealotData.getString("color"), 16);
        }
        align = zealotData.getString("align");
        toggled = zealotData.getBoolean("toggled");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Mod.EventHandler
  public void init(FMLInitializationEvent event) {
    instance = this;
    ModCoreInstaller.initializeModCore(Minecraft.getMinecraft().mcDataDir);
    eventHandler = new EventHandler(this);
    ClientCommandHandler.instance.registerCommand(new ZealotCounterCommand(this));
    MinecraftForge.EVENT_BUS.register(eventHandler);
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      updateInfoWithCurrentSetup(currentSetup.split(":"), currentSetup);
    }));
  }

  @Mod.EventHandler
  public void postInit(FMLPostInitializationEvent event) {
	String uuid = Minecraft.getMinecraft().getSession().getProfile().getId().toString();
	//don't create a default profile every time.
	if((zealotData == null) ||
		(zealotData.getJSONObject("players").getJSONObject(uuid).isEmpty())) {
		currentSetup = uuid + ":" + DEFAULT_PROFILE;
		updateInfoWithCurrentSetup(currentSetup.split(":"), currentSetup);
	}
  }
}
