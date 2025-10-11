package com.lootdust.simplegroupmsg;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.*;
import de.maxhenkel.voicechat.api.Group.Type;
import de.maxhenkel.voicechat.plugins.impl.ServerPlayerImpl;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.UUID;

public class GroupMsgPlugin implements VoicechatPlugin {
    public static VoicechatApi api;
    public static VoicechatServerApi sapi;
    public static HashMap<UUID, UUID> playerGroupStatus;
    public static HashMap<UUID, Type> groupTypes;

    @Override
    public String getPluginId() {
        return SimpleGroupMsg.MODID;
    }

    @Override
    public void initialize(VoicechatApi api) {
        GroupMsgPlugin.api = api;
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
        sapi = event.getVoicechat();
        sapi.getGroups().forEach((group -> groupTypes.put(group.getId(), group.getType())));
        SimpleGroupMsg.LOGGER.info("Api is null? " + (sapi == null));
    }

    private void onPlayerJoinGroup(JoinGroupEvent event) {
        ServerPlayer player = ((ServerPlayerImpl) event.getConnection().getPlayer()).getRealServerPlayer();
        playerGroupStatus.put(player.getUUID(), event.getGroup().getId());
        SimpleGroupMsg.LOGGER.info("Player " + player.getDisplayName() + " joined group " + event.getGroup().getName());
    }

    private void onPlayerLeaveGroup(LeaveGroupEvent event) {
        ServerPlayer player = ((ServerPlayerImpl) event.getConnection().getPlayer()).getRealServerPlayer();
        playerGroupStatus.remove(player.getUUID());
        SimpleGroupMsg.LOGGER.info("Player " + player.getDisplayName() + " left group " + event.getGroup().getName());
    }

    private void onGroupCreate(CreateGroupEvent event) {
        groupTypes.put(event.getGroup().getId(), event.getGroup().getType());
    }

    private void onGroupRemove(RemoveGroupEvent event) {
        groupTypes.remove(event.getGroup().getId());
    }
}
