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
    VoicechatServerApi api;
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

    @SubscribeEvent(priority = EventPriority.LOW)
    void onServerChatEvent(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        ServerLevel server = event.getPlayer().serverLevel();
        VoicechatConnection connection = api.getConnectionOf(player.getUUID());
        if (connection != null) {
            Group group = api.getConnectionOf(player.getUUID()).getGroup();
            if (group != null) {
                Type type = group.getType();
                if (type.equals(Type.ISOLATED) || type.equals(Type.NORMAL)) {
                    playerGroupStatus.forEach((hearer, groupId) -> {
                        if (player.getUUID() == hearer) {
                            return;
                        }
                        if (groupId == group.getId()) {
                            server.getPlayerByUUID(hearer).displayClientMessage(
                                    Component.literal(String.format("[%s] ", group.getName())).withStyle(ChatFormatting.GREEN)
                                            .append(Component.literal(String.format("<%s> ", player.getDisplayName()))
                                            .append(event.getMessage())),
                            false);
                        }
                    });
                } else if (type.equals(Type.OPEN)) {
                    playerGroupStatus.forEach((hearer, groupId) -> {
                        if (player.getUUID() == hearer) {
                            return;
                        }
                        if (groupTypes.get(groupId) != Type.ISOLATED) {
                            server.getPlayerByUUID(hearer).displayClientMessage(
                                    Component.literal(String.format("[%s] ", group.getName())).withStyle(ChatFormatting.GREEN)
                                            .append(Component.literal(String.format("[%s] <%s> ", group.getName(), player.getDisplayName()))
                                            .append(event.getMessage())),
                            false);
                        }
                    });
                }
            } else {
                playerGroupStatus.forEach((hearer, groupId) -> {
                    if (player.getUUID() == hearer) {
                        return;
                    }
                    if (groupTypes.get(groupId) != Type.ISOLATED) {
                        server.getPlayerByUUID(hearer).displayClientMessage(
                                Component.literal(String.format("<%s> ", player.getDisplayName())).append(event.getMessage()), false);
                    }
                });
            }
        }
        event.setCanceled(true);
        SimpleGroupMsg.LOGGER.info("Event is canceled? " + event.isCanceled());
    }
}
