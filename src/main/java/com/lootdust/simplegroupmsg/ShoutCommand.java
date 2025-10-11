package com.lootdust.simplegroupmsg;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.maxhenkel.voicechat.api.Group;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;

import static com.lootdust.simplegroupmsg.GroupMsgPlugin.groupTypes;
import static com.lootdust.simplegroupmsg.GroupMsgPlugin.playerGroupStatus;

@SuppressWarnings("unchecked")
public class ShoutCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(Commands.literal("shout")
                        .then(Commands.argument("message", MessageArgument.message()).executes((context) -> {
                            Component message = MessageArgument.getMessage(context, "message");
                            playerGroupStatus.forEach((hearer, groupId) -> {
                                if (groupTypes.get(groupId) != Group.Type.ISOLATED) {
                                    hearer.sendSystemMessage(Component.literal(String.format("%s ", context.getSource().getDisplayName())).append(message), false);
                                }
                            });
                            return message.getString().length();
                        })));
    }
}
