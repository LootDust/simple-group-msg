package com.lootdust.simplegroupmsg;

import com.mojang.logging.LogUtils;
import de.maxhenkel.voicechat.ForgeVoicechatMod;
import de.maxhenkel.voicechat.voice.server.ServerVoiceEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(SimpleGroupMsg.MODID)
public class SimpleGroupMsg {
    public static final String MODID = "simplegroupmsg";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ServerVoiceEvents voicechatServer;

    public SimpleGroupMsg()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(new ServerChatHandler());
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("AAA SGM Loaded.");
    }

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        ShoutCommand.register(event.getDispatcher());
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
