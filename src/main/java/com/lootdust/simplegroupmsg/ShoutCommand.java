package com.lootdust.simplegroupmsg;

import com.google.common.base.Predicates;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Arrays;

public class ShoutCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("shout").then(Commands.argument("message", MessageArgument.message()).executes((context) -> {
            try {
                String speakerName;
                if (context.getSource().source instanceof ServerPlayer) {
                    speakerName = ((ServerPlayer) context.getSource().source).getDisplayName().getString();
                } else if (context.getSource().source instanceof LivingEntity) {
                    speakerName = ((LivingEntity) context.getSource().source).hasCustomName() ?
                            ((LivingEntity) context.getSource().source).getCustomName().getString() : ((LivingEntity) context.getSource().source).getType().toString();
                } else {
                    for (ServerPlayer hearer : context.getSource().getLevel().getPlayers(Predicates.alwaysTrue())) {
                        hearer.sendSystemMessage((Component.literal("[ Server: ")
                                .append(Component.literal(MessageArgument.getMessage(context, "message").getString()).append(Component.literal(" ]")))).withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
                    }
                    return MessageArgument.getMessage(context, "message").getString().length();
                }
                for (ServerPlayer hearer : context.getSource().getLevel().getPlayers(Predicates.alwaysTrue())) {
                    hearer.sendSystemMessage(Component.literal(String.format("<%s> ", speakerName))
                            .append(Component.literal(MessageArgument.getMessage(context, "message").getString())));
                }
                return MessageArgument.getMessage(context, "message").getString().length();
            } catch (Exception e) {
                SimpleGroupMsg.LOGGER.warn("Exception occured in command!");
                //SimpleGroupMsg.LOGGER.warn(Arrays.toString(e.getStackTrace()));
                return 0;
            }
        })));
    }
}
