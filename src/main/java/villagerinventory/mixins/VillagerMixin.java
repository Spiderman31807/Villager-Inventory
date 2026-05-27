package villagerinventory.mixins;

import villagerinventory.VillagerInventoryMod;

import villagerinventory.ViewableInventory;

import villagerinventory.InventoryMenu;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.Level;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;

import io.netty.buffer.Unpooled;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager implements ViewableInventory {
	@Unique
	private Player viewer = null;

	public VillagerMixin(EntityType<? extends AbstractVillager> type, Level world) {
		super(type, world);
	}

	private static void openInventory(Villager villager, ServerPlayer player) {
		player.openMenu(new MenuProvider() {
			@Override
			public Component getDisplayName() {
				return villager.getDisplayName();
			}

			@Override
			public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
				FriendlyByteBuf packetBuffer = new FriendlyByteBuf(Unpooled.buffer());
				packetBuffer.writeBlockPos(player.blockPosition());
				packetBuffer.writeByte(0);
				packetBuffer.writeVarInt(villager.getId());
				packetBuffer.writeInt(villager.getPlayerReputation(player));
				return new InventoryMenu(id, inventory, packetBuffer);
			}
		}, buf -> {
			buf.writeBlockPos(player.blockPosition());
			buf.writeByte(0);
			buf.writeVarInt(villager.getId());
			buf.writeInt(villager.getPlayerReputation(player));
		});
	}

	private Villager villager() {
		return (Villager) (Object) this;
	}

	public Player getViewer() {
		if(this.viewer instanceof Player player && player.containerMenu instanceof InventoryMenu menu && menu.boundEntity.equals(villager()))
			return this.viewer;
		return null;
	}

	public void setViewer(Player player) {
		this.viewer = player;
	}

	@Nullable
	@Override
	public Player getTradingPlayer() {
		Player tradingWith = super.getTradingPlayer();
		return tradingWith == null ? this.getViewer() : tradingWith;
	}

	@Inject(method = "setTradingPlayer", at = @At("HEAD"), cancellable = true)
	private void setTradingPlayer(@Nullable Player player, CallbackInfo callback) {
		if(this.getViewer() != null)
			callback.cancel();
	}

	@Inject(method = "mobInteract", at = @At("RETURN"), cancellable = true)
	private void mobInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> callback) {
		Villager villager = villager();
		if (villager.isSleeping() || villager.isTrading())
			return;
		InteractionResult result = callback.getReturnValue();
		if (result != InteractionResult.FAIL && result != InteractionResult.PASS)
			return;
		if (player.isShiftKeyDown()) {
			if (player instanceof ServerPlayer serverPlayer) {
				if (villager.getPlayerReputation(serverPlayer) <= 5) {
					villager.setUnhappyCounter(40);
					if (!villager.level().isClientSide())
						villager.makeSound(SoundEvents.VILLAGER_NO);
					callback.setReturnValue(InteractionResult.FAIL);
					return;
				}
				VillagerInventoryMod.LOGGER.info("Player opened Villager Inventory with Rep of " + villager.getPlayerReputation(serverPlayer));
				openInventory(villager, serverPlayer);
			}
			callback.setReturnValue(InteractionResult.SUCCESS);
		}
	}
}
