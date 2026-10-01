package com.yuval.minestreet.mixin.common;

import com.mojang.brigadier.Command;
import com.yuval.minestreet.CommonModHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ItemEntity.class)
public class ItemPickupPreventionMixinBecauseMinecraftIsDumb {

    @Shadow private int pickupDelay;
    @Shadow private UUID target;

    @Inject(
            method = "playerTouch",
            at = @At("HEAD"),
            cancellable = true
    )
    private void preventPickupWhenFull(Player player, CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (!self.level().isClientSide()) {
            ItemStack itemStack = self.getItem();
            Item item = itemStack.getItem();
            int orgCount = itemStack.getCount();
            TriState result = EventHooks.fireItemPickupPre(self, player).canPickup();
            if (result.isFalse()) {
                return;
            }

            ItemStack originalCopy = itemStack.copy();
            if ((result.isTrue() || pickupDelay == 0 && (target == null || target.equals(player.getUUID()))) && player.getInventory().add(itemStack)) {
                EventHooks.fireItemPickupPost(self, player, originalCopy);
                orgCount = originalCopy.getCount() - itemStack.getCount();
                player.take(self, orgCount);
                if (itemStack.isEmpty()) {
                    self.discard();
                    itemStack.setCount(orgCount);
                }

                player.awardStat(Stats.ITEM_PICKED_UP.get(item), orgCount);
                player.onItemPickup(self);
            }
        }
        ci.cancel();
    }
}
