package net.doubledoordev.hcitd;

import java.io.File;
import java.util.Date;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.GameType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.mojang.authlib.GameProfile;

@Mod(
        modid = HardcoreInTheDark.MOD_ID,
        name = HardcoreInTheDark.MOD_NAME,
        version = HardcoreInTheDark.VERSION,
        acceptableRemoteVersions = "*"
)
public class HardcoreInTheDark
{
    public static final String MOD_ID = "hardcoreinthedark";
    public static final String MOD_NAME = "Hardcore In The Dark";
    public static final String VERSION = "1.1.1";

    /**
     * This is the instance of your mod as created by Forge. It will never be null.
     */
    @Mod.Instance(MOD_ID)
    public static HardcoreInTheDark INSTANCE;
    static Logger log;

    /**
     * This is the first initialization event. Register tile entities here.
     * The registry events below will have fired prior to entry to this method.
     */
    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event)
    {
        log = event.getModLog();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingHurtEvent event)
    {
        if (event.getEntity() instanceof EntityPlayerMP && event.getEntityLiving().getHealth() <= event.getAmount())
        {
            int lightLevel = event.getEntityLiving().getEntityWorld().getLight(event.getEntityLiving().getPosition(), false);
            int dimID = event.getEntityLiving().dimension;
            int lightLevelTrigger = ModConfig.globalUnsafeLight;

            if (ModConfig.unsafeLightDimOverride.containsKey(String.valueOf(dimID)))
                lightLevelTrigger = ModConfig.unsafeLightDimOverride.get(String.valueOf(dimID));

            if (lightLevel <= lightLevelTrigger)
                //True = SP.
                if (event.getEntity().world.isRemote)
                {
                    ((EntityPlayerMP) event.getEntity()).setGameType(GameType.SPECTATOR);
                    ((EntityPlayerMP) event.getEntity()).sendStatusMessage(new TextComponentString(ModConfig.deathMessage), false);
                }
                else
                {
                    MinecraftServer server = event.getEntityLiving().getServer();
                    EntityPlayerMP playerMP = (EntityPlayerMP) event.getEntity();
                    UUID playerUUID = playerMP.getUniqueID();

                    if (ModConfig.shouldSpectateOnServer)
                    {
                        playerMP.setGameType(GameType.SPECTATOR);
                        server.getWorld(playerMP.dimension).getGameRules().setOrCreateGameRule("spectatorsGenerateChunks", "false");

                        ((EntityPlayerMP) event.getEntity()).sendStatusMessage(new TextComponentString(ModConfig.deathMessage), false);
                    }

                    if (ModConfig.shouldBanOnServer)
                    {
                        Date thisInstant = new Date();
                        Date endDate = new Date(ModConfig.banTime * 1000 + thisInstant.getTime());
                        UserListBansEntry userlistbansentry =
                                new UserListBansEntry(((EntityPlayerMP) event.getEntity()).getGameProfile(), thisInstant, "HardcoreInTheDark", endDate,
                                        String.format(ModConfig.banMessage, lightLevel));
                        server.getPlayerList().getBannedPlayers().addEntry(userlistbansentry);

                        playerMP.connection.disconnect(new TextComponentString(ModConfig.disconnectMessage));
                    }

                    //This whole mod is fairly simple to understand except why this is all done.
                    if (ModConfig.shouldDeletePlayerData)
                    {
                        // We need an unobtainable UUID to make a player with as we don't want to fuck up everyone.
                        GameProfile emptyProfile = new GameProfile(null, "YouFoundAnEasterEgg<3");
                        EntityPlayerMP emptyPlayer = new EntityPlayerMP(server, server.getWorld(0), emptyProfile, new PlayerInteractionManager(server.getWorld(0)));
                        // Then because you can't directly delete player data we indirectly delete it by copying a non-existing player's fresh data over our player.
                        playerMP.copyFrom(emptyPlayer, false);

                        //Then because advancements are part of the content these days and add things to the player data we need to clear these also otherwise...
                        // the recipe book breaks and you can't get stuff back for it. So we direct delete the file and then force a reload, due to the file not existing a blank is created.
                        File advancementPlayerData = new File(server.getWorld(0).getSaveHandler().getWorldDirectory(), "advancements/" + playerUUID + ".json");
                        advancementPlayerData.delete();
                        playerMP.getAdvancements().reload();
                        //Absolute fukin magic this is...
                    }
                }
        }
    }
}
