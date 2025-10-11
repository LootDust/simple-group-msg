package com.lootdust.simplegroupmsg;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.lootdust.simplegroupmsg.GroupMsgPlugin.*;

public class ServerChatHandler {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onServerChatEvent(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        ServerLevel server = event.getPlayer().serverLevel();
        VoicechatConnection connection = api.getConnectionOf(player.getUUID());
        if (connection != null) {
            Group group = api.getConnectionOf(player.getUUID()).getGroup();
            if (group != null) {
                Group.Type type = group.getType();
                if (type.equals(Group.Type.ISOLATED) || type.equals(Group.Type.NORMAL)) {
                    playerGroupStatus.forEach((hearer, groupId) -> {
                        if (groupId == group.getId()) {
                            server.getPlayerByUUID(hearer).displayClientMessage(
                                    Component.literal(String.format("[%s] ", group.getName())).withStyle(ChatFormatting.GREEN)
                                            .append(Component.literal(String.format("<%s> ", player.getDisplayName()))
                                                    .append(event.getMessage())),
                                    false);
                        }
                    });
                } else if (type.equals(Group.Type.OPEN)) {
                    playerGroupStatus.forEach((hearer, groupId) -> {
                        if (groupTypes.get(groupId) != Group.Type.ISOLATED) {
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
                    if (groupTypes.get(groupId) != Group.Type.ISOLATED) {
                        server.getPlayerByUUID(hearer).displayClientMessage(
                                Component.literal(String.format("<%s> ", player.getDisplayName())).append(event.getMessage()), false);
                    }
                });
            }
        }
        event.setCanceled(true);
    }
}
