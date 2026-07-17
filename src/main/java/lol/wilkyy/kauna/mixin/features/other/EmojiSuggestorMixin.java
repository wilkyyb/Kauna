package lol.wilkyy.kauna.mixin.features.other;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lol.wilkyy.kauna.features.chat.EmojiRegistry;
import lol.wilkyy.kauna.Kauna;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(CommandSuggestions.class)
public class EmojiSuggestorMixin {

    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Final
    @Shadow
    private EditBox input;

    @Inject(method = "updateCommandInfo()V", at = @At("RETURN"))
    private void kauna$injectEmojiSuggestions(CallbackInfo ci) {
        if (this.input == null) return;
        if (!Kauna.isCurrentlyOnRealmi()) return;

        String fullText    = this.input.getValue();
        int    cursor      = this.input.getCursorPosition();
        if (cursor <= 0 || fullText.isEmpty()) return;

        String beforeCursor = fullText.substring(0, Math.min(cursor, fullText.length()));

        int tokenStart = beforeCursor.lastIndexOf(':');
        if (tokenStart < 0) return;

        String token = beforeCursor.substring(tokenStart);
        if (token.contains(" ")) return;

        String tokenLower = token.toLowerCase();
        List<String> matches = new ArrayList<>();
        for (String key : EmojiRegistry.EMOJIS.keySet()) {
            if (key.toLowerCase().startsWith(tokenLower)) {
                matches.add(key);
            }
        }

        if (matches.isEmpty()) return;
        if (matches.size() == 1 && matches.get(0).equalsIgnoreCase(token)) return;

        SuggestionsBuilder builder = new SuggestionsBuilder(beforeCursor, tokenStart);
        for (String key : matches) {
            builder.suggest(key);
        }

        pendingSuggestions = builder.buildFuture();
    }
}