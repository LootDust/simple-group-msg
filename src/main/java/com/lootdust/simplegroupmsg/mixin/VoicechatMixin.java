package com.lootdust.simplegroupmsg.mixin;

import com.lootdust.simplegroupmsg.SimpleGroupMsg;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.server.ServerVoiceEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Voicechat.class, remap = false)
public class VoicechatMixin {
    @Shadow public static ServerVoiceEvents SERVER;

    @Inject(method = "initialize", at = @At("TAIL"))
    private void initialize(CallbackInfo ci) {
        SimpleGroupMsg.voicechatServer = SERVER;
    }
}
