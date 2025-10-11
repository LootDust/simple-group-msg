package com.lootdust.simplegroupmsg;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.*;
import de.maxhenkel.voicechat.api.Group.Type;
import de.maxhenkel.voicechat.plugins.impl.ServerPlayerImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.UUID;


@Mod.EventBusSubscriber
public class GroupMsgPlugin implements VoicechatPlugin {
    public static VoicechatServerApi api;
    public static HashMap<UUID, UUID> playerGroupStatus;
    public static HashMap<UUID, Type> groupTypes;

    @Override
    public String getPluginId() {
        return SimpleGroupMsg.MODID;
    }

    @Override
    public void initialize(VoicechatApi api) {

    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
        registration.registerEvent(JoinGroupEvent.class, this::onPlayerJoinGroup);
        registration.registerEvent(LeaveGroupEvent.class, this::onPlayerLeaveGroup);
        registration.registerEvent(CreateGroupEvent.class, this::onGroupCreate);
        registration.registerEvent(RemoveGroupEvent.class, this::onGroupRemove);
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        api = event.getVoicechat();
        api.getGroups().forEach((group -> groupTypes.put(group.getId(), group.getType())));
        SimpleGroupMsg.LOGGER.info("Api is null? " + (api == null));
    }

    private void onPlayerJoinGroup(JoinGroupEvent event) {
        playerGroupStatus.put(((ServerPlayerImpl) event.getConnection().getPlayer()).getRealServerPlayer().getUUID(), event.getGroup().getId());
    }

    private void onPlayerLeaveGroup(LeaveGroupEvent event) {
        playerGroupStatus.remove(((ServerPlayerImpl) event.getConnection().getPlayer()).getRealServerPlayer().getUUID());
    }

    private void onGroupCreate(CreateGroupEvent event) {
        groupTypes.put(event.getGroup().getId(), event.getGroup().getType());
    }

    private void onGroupRemove(RemoveGroupEvent event) {
        groupTypes.remove(event.getGroup().getId());
    }
}
