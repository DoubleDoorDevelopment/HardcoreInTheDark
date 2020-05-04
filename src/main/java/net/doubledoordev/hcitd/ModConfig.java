package net.doubledoordev.hcitd;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static net.doubledoordev.hcitd.HardcoreInTheDark.MOD_ID;

@Config(modid = MOD_ID, category = "All")
@Mod.EventBusSubscriber(modid = MOD_ID)
public class ModConfig
{
    @Config.Comment({"This level and below are unsafe, dying in this light level acts like hardcore."
    })
    @Config.RangeInt(min = 0, max = 16)
    public static int unsafeLight = 7;

    @Config.Comment({"Message Player gets after dying in an unsafe light level."
    })
    public static String deathMessage = "Whoops, you died in an unsafe area! Perhaps next time you will be more careful.";

    @Config.Comment({"Should players be banned on a server?"
    })
    public static boolean shouldBanOnServer = false;

    @Config.Comment({"Should players be set to Spectator on a server?"
    })
    public static boolean shouldSpectateOnServer = true;

    @Config.Comment({"Should the player data for this player be deleted on death? WARNING: IRREVERSABLE!"
    })
    public static boolean shouldDeletePlayerData = false;

    @Config.Comment({"How long a user is banned for IN REAL TIME on a server. Time in seconds!",
            "60 = 1 Minute, 3600 = 1 hour, 43200 = 12 horus, 86400 =  24 hours"
    })
    @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
    public static int banTime = 3600;

    @Config.Comment({"Message used when player is kicked on the ban."
    })
    public static String disconnectMessage = "YOU DIED! YOU BANNED! YOU BAD!";

    @Config.Comment({"Ban message user gets when trying to connect again while banned.",
            "Use %s for the light level the user died at."
    })
    public static String banMessage = "Died at or below the unsafe light level (7) was %s";

    @Mod.EventBusSubscriber
    public static class SyncConfig
    {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
        {
            if (event.getModID().equals(MOD_ID))
            {
                ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
            }
        }
    }
}
