package com.lootdust.simplegroupmsg;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.*;
import de.maxhenkel.voicechat.api.Group.Type;
import de.maxhenkel.voicechat.plugins.impl.ServerPlayerImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.UUID;


public class GroupMsgPlugin implements VoicechatPlugin {
    VoicechatServerApi api;
    public static HashMap<ServerPlayer, UUID> playerGroupStatus;
    public static HashMap<UUID, Type> groupTypes;

    @Override
    public String getPluginId() {
        return "simple_group_msg";
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
    }

    private void onPlayerJoinGroup(JoinGroupEvent event) {
        playerGroupStatus.put(((ServerPlayerImpl) event.getConnection().getPlayer()).getRealServerPlayer(),event.getGroup().getId());
    }

    private void onPlayerLeaveGroup(LeaveGroupEvent event) {
        playerGroupStatus.remove(((ServerPlayerImpl) event.getConnection().getPlayer()).getRealServerPlayer());
    }

    private void onGroupCreate(CreateGroupEvent event) {
        groupTypes.put(event.getGroup().getId(), event.getGroup().getType());
    }

    private void onGroupRemove(RemoveGroupEvent event) {
        groupTypes.remove(event.getGroup().getId());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    void onServerChatEvent(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        VoicechatConnection connection = api.getConnectionOf(player.getUUID());
        if (connection != null) {
            Group group = api.getConnectionOf(player.getUUID()).getGroup();
            if (group != null) {
                Type type = group.getType();
                if (type.equals(Type.ISOLATED) || type.equals(Type.NORMAL)) {
                    playerGroupStatus.forEach((hearer, groupId) -> {
                        if (groupId == group.getId()) {
                            hearer.sendSystemMessage(Component.literal(String.format("[%s] %s ", group.getName(), player.getDisplayName())).append(event.getMessage()), false);
                        }
                    });
                    event.setCanceled(true);
                } else if (type.equals(Type.OPEN)) {
                    playerGroupStatus.forEach((hearer, groupId) -> {
                        if (groupTypes.get(groupId) != Type.ISOLATED) {
                            hearer.sendSystemMessage(Component.literal(String.format("[%s] %s ", group.getName(), player.getDisplayName())).append(event.getMessage()), false);
                        }
                    });
                    event.setCanceled(true);
                }
            } else {
                playerGroupStatus.forEach((hearer, groupId) -> {
                    if (groupTypes.get(groupId) != Type.ISOLATED) {
                        hearer.sendSystemMessage(Component.literal(String.format("%s ", player.getDisplayName())).append(event.getMessage()), false);
                    }
                });
                event.setCanceled(true);
            }
        }
        event.setCanceled(true);
    }
}
