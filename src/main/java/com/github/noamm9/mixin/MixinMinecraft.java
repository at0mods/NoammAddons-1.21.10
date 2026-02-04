package com.github.noamm9.mixin;

import com.github.noamm9.event.EventBus;
import com.github.noamm9.event.impl.PlayerInteractEvent;
import com.github.noamm9.features.impl.visual.CpsDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
    @Shadow @Nullable public Screen screen;
    @Shadow
    @Nullable
    public HitResult hitResult;
    @Shadow
    public LocalPlayer player;

    @Shadow
    public abstract void setScreen(@Nullable Screen screen);

    @Inject(method = "startAttack", at = @At("HEAD"))
    private void onStartAttack(CallbackInfoReturnable<Boolean> cir) {
        CpsDisplay.addLeftClick();
    }

    @Inject(method = "startUseItem", at = @At("HEAD"))
    private void onStartUseItem(CallbackInfo ci) {
        CpsDisplay.addRightClick();
    }

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;interactAt(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/EntityHitResult;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"), cancellable = true)
    private void cancelEntityUse(CallbackInfo ci) {
        handleHitResult(ci, false);
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void preAttack(CallbackInfoReturnable<Boolean> cir) {
        handleHitResult(cir, true);
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void preWhileAttack(boolean bl, CallbackInfo ci) {
        if (!bl) return;
        handleHitResult(ci, true);
    }

    @Unique
    private void handleHitResult(CallbackInfo ci, Boolean left) {
        ItemStack itemStack = player.getMainHandItem();
        PlayerInteractEvent event = hitResult == null ? new PlayerInteractEvent.LEFT_CLICK.AIR(itemStack) :
            switch (hitResult.getType()) {
                case ENTITY: {
                    EntityHitResult ehr = (EntityHitResult) this.hitResult;
                    yield (left
                        ? new PlayerInteractEvent.LEFT_CLICK.ENTITY(itemStack, ehr.getEntity())
                        : new PlayerInteractEvent.RIGHT_CLICK.ENTITY(itemStack, ehr.getEntity())
                    );
                }
                case BLOCK: {
                    BlockHitResult bhr = (BlockHitResult) this.hitResult;
                    yield (left
                        ? new PlayerInteractEvent.LEFT_CLICK.BLOCK(itemStack, bhr.getBlockPos())
                        : new PlayerInteractEvent.RIGHT_CLICK.BLOCK(itemStack, bhr.getBlockPos())
                    );
                }
                case MISS: {
                    yield (left
                        ? new PlayerInteractEvent.LEFT_CLICK.AIR(itemStack)
                        : new PlayerInteractEvent.RIGHT_CLICK.AIR(itemStack)
                    );
                }
            };

        if (EventBus.post(event)) ci.cancel();
    }
}