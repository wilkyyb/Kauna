package lol.wilkyy.kauna.mixin.features.kahakka.parkour;

import lol.wilkyy.kauna.features.kahakka.duel.DuelManager;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class NoServerTitleMixin {

	@Inject(method = "setTitleText", at = @At("HEAD"), cancellable = true)
	private void swallowTitle(ClientboundSetTitleTextPacket packet, CallbackInfo ci) {
		if (DuelManager.inParkourDuel() && Kauna.inKahakka()) {
			ci.cancel();
		}
	}

	@Inject(method = "setSubtitleText", at = @At("HEAD"), cancellable = true)
	private void swallowSubtitle(ClientboundSetSubtitleTextPacket packet, CallbackInfo ci) {
		if (DuelManager.inParkourDuel() && Kauna.inKahakka()) {
			ci.cancel();
		}
	}


	@Inject(method = "setTitlesAnimation", at = @At("HEAD"), cancellable = true)
	private void swallowFade(ClientboundSetTitlesAnimationPacket packet, CallbackInfo ci) {
		if (DuelManager.inParkourDuel() && Kauna.inKahakka()) {
			ci.cancel();
		}
	}
}
