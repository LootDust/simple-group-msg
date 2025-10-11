package com.lootdust.simplegroupmsg;

import com.google.common.base.Predicates;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class ShoutCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("shout").then(Commands.argument("message", ComponentArgument.textComponent()).executes((context) -> {
            String speakerName;
            if (context.getSource().source instanceof ServerPlayer) {
                speakerName = ((ServerPlayer) context.getSource().source).getDisplayName().getString();
            } else if (context.getSource().source instanceof LivingEntity) {
                speakerName = ((LivingEntity) context.getSource().source).hasCustomName() ?
                        ((LivingEntity) context.getSource().source).getCustomName().getString() : ((LivingEntity) context.getSource().source).getType().toString();
            } else {
                for (ServerPlayer hearer : context.getSource().getLevel().getPlayers(Predicates.alwaysTrue())) {
                    hearer.sendSystemMessage((Component.literal("[ Server: ")
                            .append(Component.literal(context.getArgument("message", String.class)).append(Component.literal(" ]")))).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
                }
                return context.getArgument("message", String.class).length();
            }
            for (ServerPlayer hearer : context.getSource().getLevel().getPlayers(Predicates.alwaysTrue())) {
                hearer.sendSystemMessage(Component.literal(String.format("<%s> ", speakerName))
                        .append(Component.literal(context.getArgument("message", String.class))));
            }
            return context.getArgument("message", String.class).length();
        })));
    }
}
