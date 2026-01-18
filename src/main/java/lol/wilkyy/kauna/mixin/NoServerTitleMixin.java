package lol.wilkyy.kauna.mixin;

import lol.wilkyy.kauna.Kauna;
import lol.wilkyy.kauna.inDuelChecks;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class NoServerTitleMixin {

	@Inject(method = "onTitle", at = @At("HEAD"), cancellable = true)
	private void swallowTitle(TitleS2CPacket packet, CallbackInfo ci) {
		if (inDuelChecks.inParkourDuel() && Kauna.inKahakka()) {
			ci.cancel();
		}
	}

	@Inject(method = "onSubtitle", at = @At("HEAD"), cancellable = true)
	private void swallowSubtitle(SubtitleS2CPacket packet, CallbackInfo ci) {
		if (inDuelChecks.inParkourDuel() && Kauna.inKahakka()) {
			ci.cancel();
		}
	}


	@Inject(method = "onTitleFade", at = @At("HEAD"), cancellable = true)
	private void swallowFade(TitleFadeS2CPacket packet, CallbackInfo ci) {
		if (inDuelChecks.inParkourDuel() && Kauna.inKahakka()) {
			ci.cancel();
		}
	}
}
