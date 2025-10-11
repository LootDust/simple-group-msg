package com.lootdust.simplegroupmsg;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.maxhenkel.voicechat.api.Group;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import static com.lootdust.simplegroupmsg.GroupMsgPlugin.groupTypes;
import static com.lootdust.simplegroupmsg.GroupMsgPlugin.playerGroupStatus;

@SuppressWarnings("unchecked")
public class ShoutCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(Commands.literal("shout")
                        .then(Commands.argument("message", MessageArgument.message()).executes((context) -> {
                            Component message = MessageArgument.getMessage(context, "message");
                            ServerLevel server = context.getSource().getLevel();
                            if (context.getSource().source instanceof ServerPlayer player) {
                                playerGroupStatus.forEach((hearer, groupId) -> {
                                    if (((ServerPlayer) context.getSource().source).getUUID() == hearer) {
                                        return;
                                    }
                                    if (groupTypes.get(groupId) != Group.Type.ISOLATED) {
                                        ((ServerPlayer) server.getPlayerByUUID(hearer)).sendChatMessage((OutgoingChatMessage) Component.literal(String.format("<%s> ", player.getDisplayName())).append(message), true, ChatType.bind(ChatType.CHAT, player));
                                    }
                                });
                            } else {
                                playerGroupStatus.forEach((hearer, groupId) -> {
                                    if (groupTypes.get(groupId) != Group.Type.ISOLATED) {
                                        ((ServerPlayer) server.getPlayerByUUID(hearer)).sendChatMessage((OutgoingChatMessage) Component.literal(String.format("<%s> ", context.getSource().getDisplayName())).append(message), true, ChatType.bind(ChatType.CHAT, context.getSource()));
                                    }
                                });
                            }
                            return message.getString().length();
                        })));
    }
}
