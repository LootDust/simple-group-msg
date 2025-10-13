package com.lootdust.simplegroupmsg;

import com.google.common.base.Predicates;
import com.mojang.brigadier.Message;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import de.maxhenkel.voicechat.voice.server.PlayerStateManager;
import de.maxhenkel.voicechat.voice.server.ServerGroupManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerChatHandler {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onServerChatEvent(ServerChatEvent event) {
        boolean should_cancel_it = true;

        if (SimpleGroupMsg.voicechatServer == null) {
            SimpleGroupMsg.LOGGER.warn("Cannot get voicechat server event! Is it initialized?");
            return;
        }

        if (SimpleGroupMsg.voicechatServer.getServer() == null) {
            SimpleGroupMsg.LOGGER.warn("Cannot get voicechat server! Is it initialized?");
            return;
        }

        ServerPlayer speaker = event.getPlayer();
        Message message = event.getMessage();

        //PlayerChatMessage chatMessage = PlayerChatMessage.unsigned(speaker.getUUID(), "This will be displayed");
        //player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(chatMessage), false, ChatType.bind(ChatType.CHAT, speaker));

        if (isPlayerInGroup(speaker)) {
            Group.Type speakerGroupType = getPlayerGroupType(speaker);
            if (speakerGroupType == null) {
                should_cancel_it = false;
            }
            if (speakerGroupType == Group.Type.OPEN) {
                List<ServerPlayer> members = getGroupMembers(speaker);
                if (members != null) {
                    for (ServerPlayer member : members) {
                        member.sendSystemMessage(Component.literal(String.format("[%s] ", getPlayerGroupName(speaker))).withStyle(ChatFormatting.GREEN)
                                .append(Component.literal(String.format("<%s> ", speaker.getName().getString())).withStyle(ChatFormatting.WHITE)
                                        .append(Component.literal(message.getString())
                                        )
                                    )
                                );
                    }
                } else {
                    should_cancel_it = false;
                }
                List<ServerPlayer> hearers = getPlayerExceptIsolatedAndMember(speaker);
                if (hearers != null && should_cancel_it) {
                    for (ServerPlayer hearer : hearers) {
                        hearer.sendSystemMessage(Component.literal(String.format("<%s> ", speaker.getName().getString()))
                                        .append(Component.literal(message.getString())));
                    }
                } else {
                    should_cancel_it = false;
                }
            } else if (speakerGroupType == Group.Type.NORMAL || speakerGroupType == Group.Type.ISOLATED) {
                List<ServerPlayer> hearers = getGroupMembers(speaker);
                if (hearers != null) {
                    for (ServerPlayer hearer : hearers) {
                        hearer.sendSystemMessage(Component.literal(String.format("[%s] ", getPlayerGroupName(speaker))).withStyle(ChatFormatting.GREEN)
                                    .append(Component.literal(String.format("<%s> ", speaker.getName().getString())).withStyle(ChatFormatting.WHITE)
                                        .append(Component.literal(message.getString())
                                        )
                                    )
                                );
                    }
                } else {
                    should_cancel_it = false;
                }
            } else {
                for (ServerPlayer hearer : speaker.serverLevel().getPlayers(Predicates.alwaysTrue())) {
                    hearer.sendSystemMessage(Component.literal(String.format("<%s> ", speaker.getName().getString()))
                            .append(Component.literal(message.getString())));
                }
            }
        } else {
            List<ServerPlayer> hearers = getPlayerExceptIsolated(speaker);
            if (hearers != null) {
                for (ServerPlayer hearer : hearers) {
                    hearer.sendSystemMessage(Component.literal(String.format("<%s> ", speaker.getName().getString()))
                            .append(Component.literal(message.getString())));
                }
            } else {
                should_cancel_it = false;
            }
        }
        if (should_cancel_it) event.setCanceled(true);
    }

    private boolean isPlayerInGroup(ServerPlayer player) {
        try {
            PlayerStateManager psM = SimpleGroupMsg.voicechatServer.getServer().getPlayerStateManager();
            return psM.getState(player.getUUID()).getGroup() != null;
        } catch (NullPointerException e) {
            SimpleGroupMsg.LOGGER.warn("NullPointerException occured! Is Voicechat server not initialized?");
            //SimpleGroupMsg.LOGGER.warn(Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

    private String getPlayerGroupName(ServerPlayer player) {
        try {
            PlayerStateManager psM = SimpleGroupMsg.voicechatServer.getServer().getPlayerStateManager();
            ServerGroupManager sgM = SimpleGroupMsg.voicechatServer.getServer().getGroupManager();
            return sgM.getGroup(psM.getState(player.getUUID()).getGroup()).getName();
        } catch (NullPointerException e) {
            SimpleGroupMsg.LOGGER.warn("NullPointerException occured! Is Voicechat server not initialized?");
            //SimpleGroupMsg.LOGGER.warn(Arrays.toString(e.getStackTrace()));
            return "Unknown Group";
        }
    }

    @Nullable
    private Group.Type getPlayerGroupType(ServerPlayer player) {
        try{
            PlayerStateManager psM = SimpleGroupMsg.voicechatServer.getServer().getPlayerStateManager();
            ServerGroupManager sgM = SimpleGroupMsg.voicechatServer.getServer().getGroupManager();
            return sgM.getGroup(psM.getState(player.getUUID()).getGroup()).getType();
        } catch (NullPointerException e) {
            SimpleGroupMsg.LOGGER.warn("NullPointerException occured! Is Voicechat server not initialized?");
            //SimpleGroupMsg.LOGGER.warn(Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    // 当有群组时搜索组员
    @Nullable
    private List<ServerPlayer> getGroupMembers(ServerPlayer player) {
        try {
            PlayerStateManager psM = SimpleGroupMsg.voicechatServer.getServer().getPlayerStateManager();
            UUID playerGroupID = psM.getState(player.getUUID()).getGroup();
            if (playerGroupID == null) return null;
            ArrayList<ServerPlayer> members = new ArrayList<>();
            for (PlayerState state : psM.getStates()) {
                UUID hearerGroupID = state.getGroup();
                if (hearerGroupID != null && hearerGroupID == playerGroupID) {
                    members.add((ServerPlayer) player.serverLevel().getPlayerByUUID(state.getUuid()));
                }
            }
            return members;
        } catch (NullPointerException e) {
            SimpleGroupMsg.LOGGER.warn("NullPointerException occured! Is Voicechat server not initialized?");
            //SimpleGroupMsg.LOGGER.warn(Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    // 当无群组时搜寻非孤立玩家
    @Nullable
    private List<ServerPlayer> getPlayerExceptIsolated(ServerPlayer player) {
        try {
            PlayerStateManager psM = SimpleGroupMsg.voicechatServer.getServer().getPlayerStateManager();
            ServerGroupManager sgM = SimpleGroupMsg.voicechatServer.getServer().getGroupManager();
            ArrayList<ServerPlayer> members = new ArrayList<>();
            for (PlayerState state : psM.getStates()) {
                UUID hearerGroupID = state.getGroup();
                if (hearerGroupID == null) {
                    members.add((ServerPlayer) player.serverLevel().getPlayerByUUID(state.getUuid()));
                    continue;
                }
                if (sgM.getGroup(hearerGroupID).getType() != Group.Type.ISOLATED) members.add((ServerPlayer) player.serverLevel().getPlayerByUUID(state.getUuid()));
            }
            return members;
        } catch (NullPointerException e) {
            SimpleGroupMsg.LOGGER.warn("NullPointerException occured! Is Voicechat server not initialized?");
            //SimpleGroupMsg.LOGGER.warn(Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    // 当群组类型为开放时搜寻非组员非孤立玩家
    @Nullable
    private List<ServerPlayer> getPlayerExceptIsolatedAndMember(ServerPlayer player) {
        try {
            PlayerStateManager psM = SimpleGroupMsg.voicechatServer.getServer().getPlayerStateManager();
            ServerGroupManager sgM = SimpleGroupMsg.voicechatServer.getServer().getGroupManager();
            ArrayList<ServerPlayer> members = new ArrayList<>();
            for (PlayerState state : psM.getStates()) {
                UUID hearerGroupID = state.getGroup();
                if (hearerGroupID == null) {
                    members.add((ServerPlayer) player.serverLevel().getPlayerByUUID(state.getUuid()));
                    continue;
                }
                if (sgM.getGroup(hearerGroupID).getType() != Group.Type.ISOLATED
                && !(hearerGroupID == psM.getState(player.getUUID()).getGroup())) members.add((ServerPlayer) player.serverLevel().getPlayerByUUID(state.getUuid()));
            }
            return members;
        } catch (NullPointerException e) {
            SimpleGroupMsg.LOGGER.warn("NullPointerException occured! Is Voicechat server not initialized?");
            //SimpleGroupMsg.LOGGER.warn(Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    /*
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onServerChatEvent(ServerChatEvent event) {
        SimpleGroupMsg.LOGGER.info(playerGroupStatus.toString());
        SimpleGroupMsg.LOGGER.info(groupTypes.toString());
        ServerPlayer player = event.getPlayer();
        ServerLevel server = event.getPlayer().serverLevel();
        VoicechatConnection connection = sapi.getConnectionOf(player.getUUID());
        if (connection != null) {
            Group group = sapi.getConnectionOf(player.getUUID()).getGroup();
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
     */
}
