package villager_inventory;

import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.client.Minecraft;

import io.netty.buffer.Unpooled;

@EventBusSubscriber(value = {Dist.CLIENT})
public class Events {
	public static void openInventory(Villager villager, ServerPlayer player) {
		player.swing(InteractionHand.MAIN_HAND, true);
		player.openMenu(new MenuProvider() {
			@Override
			public Component getDisplayName() {
				return Component.literal("Villager");
			}

			@Override
			public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
				FriendlyByteBuf packetBuffer = new FriendlyByteBuf(Unpooled.buffer());
				packetBuffer.writeBlockPos(player.blockPosition());
				packetBuffer.writeByte(0);
				packetBuffer.writeVarInt(villager.getId());
				return new Menu(id, inventory, packetBuffer);
			}
		}, buf -> {
			buf.writeBlockPos(player.blockPosition());
			buf.writeByte(0);
			buf.writeVarInt(villager.getId());
		});
	}

	public static void openInventory(Villager villager) {
		PacketDistributor.sendToServer(new KeyMessage(villager.getId()));
		Minecraft.getInstance().player.swing(InteractionHand.MAIN_HAND, true);
	}

	public static void openKeybindTriggered() {
		if (Minecraft.getInstance().crosshairPickEntity instanceof Villager villager)
			openInventory(villager);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
		if (!Keybinds.INTERACTION.isDefault())
			return;
		Entity target = event.getTarget();
		if (!target.isRemoved() && target instanceof Villager villager) {
			if (event.getEntity().isShiftKeyDown()) {
				openInventory(villager);
				event.setCanceled(true);
			}
		}
	}
}