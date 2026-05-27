
package villagerinventory;

import net.neoforged.neoforge.items.wrapper.EntityArmorInvWrapper;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.IItemHandler;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPos;

import java.util.function.Supplier;
import java.util.Map;
import java.util.HashMap;

import com.google.common.collect.ImmutableMap;

public class InventoryMenu extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>> {
	public static final Map<Item, Integer> FOOD_POINTS = ImmutableMap.of(Items.BREAD, 4, Items.POTATO, 1, Items.CARROT, 1, Items.BEETROOT, 1);
	public final static HashMap<String, Object> guistate = new HashMap<>();
	public final Level world;
	public final Player entity;
	public int x, y, z;
	private ContainerLevelAccess access = ContainerLevelAccess.NULL;
	private IItemHandler internal;
	private final Map<Integer, Slot> customSlots = new HashMap<>();
	private boolean bound = false;
	public LivingEntity boundEntity = null;
	public int playerRep;
	public int update;

	public boolean hasBinding(ItemStack stack) {
		return stack.getEnchantmentLevel(this.world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.BINDING_CURSE)) != 0;
	}

	public boolean villagerUpset() {
		if(this.boundEntity instanceof Villager villager)
			villager.setUnhappyCounter(10);
		if (!this.world.isClientSide())
			this.boundEntity.makeSound(SoundEvents.VILLAGER_NO);
		return false;
	}

	public InventoryMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
		super(Menus.Inventory.get(), id);
		this.entity = inv.player;
		this.world = inv.player.level();
		this.internal = new ItemStackHandler(12);
		BlockPos pos = null;
		if (extraData != null) {
			pos = extraData.readBlockPos();
			this.x = pos.getX();
			this.y = pos.getY();
			this.z = pos.getZ();
			access = ContainerLevelAccess.create(world, pos);
		}
		if (pos != null) {
			if (extraData.readableBytes() > 1) {
				extraData.readByte();
				boundEntity = (LivingEntity) world.getEntity(extraData.readVarInt());
				if (boundEntity instanceof Villager villager) {
					this.internal = this.getWrapper(villager);
					this.bound = true;
				}
			}
		}

		this.playerRep = extraData.readInt();
		if(this.boundEntity instanceof ViewableInventory inventory)
			inventory.setViewer(this.entity);
		this.customSlots.put(0, this.addSlot(new SlotItemHandler(internal, 0, 8, 62) {
			private final int slot = 0;

			@Override
			public int getMaxStackSize() {
				return 1;
			}

			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.canEquip(EquipmentSlot.FEET, boundEntity);
			}

			@Override
			public boolean mayPickup(Player player) {
				if (playerRep <= 25)
					return villagerUpset();
				return !this.getItem().isEmpty() && !player.isCreative() && hasBinding(this.getItem()) ? false : super.mayPickup(player);
			}

			@Override
			public ResourceLocation getNoItemIcon() {
				return net.minecraft.world.inventory.InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS;
			}
		}));
		this.customSlots.put(1, this.addSlot(new SlotItemHandler(internal, 1, 8, 44) {
			private final int slot = 1;

			@Override
			public int getMaxStackSize() {
				return 1;
			}

			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.canEquip(EquipmentSlot.LEGS, boundEntity);
			}

			@Override
			public boolean mayPickup(Player player) {
				if (playerRep <= 25)
					return villagerUpset();
				return !this.getItem().isEmpty() && !player.isCreative() && hasBinding(this.getItem()) ? false : super.mayPickup(player);
			}

			@Override
			public ResourceLocation getNoItemIcon() {
				return net.minecraft.world.inventory.InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS;
			}
		}));
		this.customSlots.put(2, this.addSlot(new SlotItemHandler(internal, 2, 8, 26) {
			private final int slot = 2;

			@Override
			public int getMaxStackSize() {
				return 1;
			}

			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.canEquip(EquipmentSlot.CHEST, boundEntity);
			}

			@Override
			public boolean mayPickup(Player player) {
				if (playerRep <= 25)
					return villagerUpset();
				return !this.getItem().isEmpty() && !player.isCreative() && hasBinding(this.getItem()) ? false : super.mayPickup(player);
			}

			@Override
			public ResourceLocation getNoItemIcon() {
				return net.minecraft.world.inventory.InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE;
			}
		}));
		this.customSlots.put(3, this.addSlot(new SlotItemHandler(internal, 3, 8, 8) {
			private final int slot = 3;

			@Override
			public int getMaxStackSize() {
				return 1;
			}

			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.canEquip(EquipmentSlot.HEAD, boundEntity);
			}

			@Override
			public boolean mayPickup(Player player) {
				if (playerRep <= 25)
					return villagerUpset();
				return !this.getItem().isEmpty() && !player.isCreative() && hasBinding(this.getItem()) ? false : super.mayPickup(player);
			}

			@Override
			public ResourceLocation getNoItemIcon() {
				return net.minecraft.world.inventory.InventoryMenu.EMPTY_ARMOR_SLOT_HELMET;
			}
		}));

		for(int idx = 4; idx < 12; idx++) {
			int y = idx < 8 ? 39 : 57;
			int x = 89 + (idx - (y == 39 ? 4 : 8)) * 18;
			this.customSlots.put(idx, this.addSlot(this.createSlot(idx, x, y)));
		}
		
		for (int si = 0; si < 3; ++si)
			for (int sj = 0; sj < 9; ++sj)
				this.addSlot(new Slot(inv, sj + (si + 1) * 9, 0 + 8 + sj * 18, 0 + 84 + si * 18));
		for (int si = 0; si < 9; ++si)
			this.addSlot(new Slot(inv, si, 0 + 8 + si * 18, 0 + 142));
		this.LoadInventory();
	}

	public SlotItemHandler createSlot(int idx, int x, int y) {
		return new SlotItemHandler(internal, idx, x, y) {
			private final int slot = idx;

			@Override
			public ItemStack remove(int amount) {
				ItemStack stack = super.remove(amount);
				SaveSlot(this.slot, this.getItem());
				return stack;
			}

			@Override
			public void onTake(Player player, ItemStack stack) {
				super.onTake(player, stack);
				SaveSlot(this.slot, this.getItem());
			}

			@Override
			public void set(ItemStack stack) {
				super.set(stack);
				SaveSlot(this.slot, this.getItem());
			}

			@Override
			public boolean mayPlace(ItemStack stack) {
				if (world instanceof ServerLevel server && boundEntity instanceof Villager villager) {
					if (villager.wantsToPickUp(server, stack))
						return true;
					return villagerUpset();
				}
				return false;
			}

			@Override
			public boolean mayPickup(Player player) {
				if (boundEntity instanceof Villager villager && playerRep <= 10)
					return villagerUpset();
				return super.mayPickup(player);
			}
		};
	}

	public CombinedInvWrapper getWrapper(Villager villager) {
		return new CombinedInvWrapper(new EntityArmorInvWrapper(villager), new ItemStackHandler(8));
	}

	public void SaveSlot(int slot, ItemStack stack) {
		if (this.boundEntity instanceof Villager villager)
			villager.getInventory().setItem(slot - 4, stack);
	}

	public void LoadInventory() {
		this.update = 10;
		if (this.boundEntity instanceof Villager villager && this.internal instanceof CombinedInvWrapper wrapper) {
			SimpleContainer inventory = villager.getInventory();
			for (int idx = 0; idx < inventory.getContainerSize(); idx++) {
				wrapper.setStackInSlot(idx + 4, inventory.getItem(idx));
			}
		}
	}

	public int countFoodPointsInInventory() {
		if (this.boundEntity instanceof Villager villager) {
			SimpleContainer simplecontainer = villager.getInventory();
			return FOOD_POINTS.entrySet().stream().mapToInt((p_186300_) -> {
				return simplecontainer.countItem(p_186300_.getKey()) * p_186300_.getValue();
			}).sum();
		}
		return 0;
	}

	@Override
	public boolean stillValid(Player player) {
		if (!(this.boundEntity instanceof Villager))
			return false;
		if (this.update-- < 0)
			this.LoadInventory();
		return this.boundEntity.isAlive();
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = (Slot) this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (index < 8) {
				if (!this.moveItemStackTo(itemstack1, 8, this.slots.size(), true))
					return ItemStack.EMPTY;
				slot.onQuickCraft(itemstack1, itemstack);
			} else if (!this.moveItemStackTo(itemstack1, 0, 8, false)) {
				if (index < 8 + 27) {
					if (!this.moveItemStackTo(itemstack1, 8 + 27, this.slots.size(), true))
						return ItemStack.EMPTY;
				} else {
					if (!this.moveItemStackTo(itemstack1, 8, 8 + 27, false))
						return ItemStack.EMPTY;
				}
				return ItemStack.EMPTY;
			}
			if (itemstack1.getCount() == 0)
				slot.set(ItemStack.EMPTY);
			else
				slot.setChanged();
			if (itemstack1.getCount() == itemstack.getCount())
				return ItemStack.EMPTY;
			slot.onTake(playerIn, itemstack1);
		}
		return itemstack;
	}

	@Override
	protected boolean moveItemStackTo(ItemStack p_38904_, int p_38905_, int p_38906_, boolean p_38907_) {
		boolean flag = false;
		int i = p_38905_;
		if (p_38907_) {
			i = p_38906_ - 1;
		}
		if (p_38904_.isStackable()) {
			while (!p_38904_.isEmpty()) {
				if (p_38907_) {
					if (i < p_38905_) {
						break;
					}
				} else if (i >= p_38906_) {
					break;
				}
				Slot slot = this.slots.get(i);
				ItemStack itemstack = slot.getItem();
				if (slot.mayPlace(itemstack) && !itemstack.isEmpty() && ItemStack.isSameItemSameComponents(p_38904_, itemstack)) {
					int j = itemstack.getCount() + p_38904_.getCount();
					int maxSize = Math.min(slot.getMaxStackSize(), p_38904_.getMaxStackSize());
					if (j <= maxSize) {
						p_38904_.setCount(0);
						itemstack.setCount(j);
						slot.set(itemstack);
						flag = true;
					} else if (itemstack.getCount() < maxSize) {
						p_38904_.shrink(maxSize - itemstack.getCount());
						itemstack.setCount(maxSize);
						slot.set(itemstack);
						flag = true;
					}
				}
				if (p_38907_) {
					--i;
				} else {
					++i;
				}
			}
		}
		if (!p_38904_.isEmpty()) {
			if (p_38907_) {
				i = p_38906_ - 1;
			} else {
				i = p_38905_;
			}
			while (true) {
				if (p_38907_) {
					if (i < p_38905_) {
						break;
					}
				} else if (i >= p_38906_) {
					break;
				}
				Slot slot1 = this.slots.get(i);
				ItemStack itemstack1 = slot1.getItem();
				if (itemstack1.isEmpty() && slot1.mayPlace(p_38904_)) {
					if (p_38904_.getCount() > slot1.getMaxStackSize()) {
						slot1.setByPlayer(p_38904_.split(slot1.getMaxStackSize()));
					} else {
						slot1.setByPlayer(p_38904_.split(p_38904_.getCount()));
					}
					slot1.setChanged();
					flag = true;
					break;
				}
				if (p_38907_) {
					--i;
				} else {
					++i;
				}
			}
		}
		return flag;
	}

	@Override
	public void removed(Player playerIn) {
		super.removed(playerIn);
		if (!bound && playerIn instanceof ServerPlayer serverPlayer) {
			if (!serverPlayer.isAlive() || serverPlayer.hasDisconnected()) {
				for (int j = 0; j < internal.getSlots(); ++j) {
					playerIn.drop(internal.extractItem(j, internal.getStackInSlot(j).getCount(), false), false);
				}
			} else {
				for (int i = 0; i < internal.getSlots(); ++i) {
					playerIn.getInventory().placeItemBackInInventory(internal.extractItem(i, internal.getStackInSlot(i).getCount(), false));
				}
			}
		}
	}

	public Map<Integer, Slot> get() {
		return customSlots;
	}
}
