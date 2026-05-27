package villagerinventory;

import org.joml.Vector3f;
import org.joml.Quaternionf;

import net.minecraft.world.level.Level;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.ARGB;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;

import java.util.HashMap;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderPipelines;

public class InventoryScreen extends AbstractContainerScreen<InventoryMenu> {
	private final static HashMap<String, Object> guistate = InventoryMenu.guistate;
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private final InventoryMenu menu;
	private final LivingEntity boundEntity;

	public InventoryScreen(InventoryMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.menu = container;
		this.world = menu.world;
		this.x = menu.x;
		this.y = menu.y;
		this.z = menu.z;
		this.entity = menu.entity;
		this.boundEntity = (LivingEntity) menu.boundEntity;
		this.imageWidth = 176;
		this.imageHeight = 166;
	}

	private static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("villager_inventory", "textures/screens/inventory.png");

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (!(menu.boundEntity instanceof Villager))
			this.minecraft.player.closeContainer();
		this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		if (this.boundEntity != null)
			this.renderEntityInInventoryFollowsAngle(guiGraphics, this.leftPos + 52, this.topPos + 72, 30, 0f + (float) Math.atan((this.leftPos + 52 - mouseX) / 40.0), (float) Math.atan((this.topPos + 20 - mouseY) / 40.0), this.boundEntity);
	}

	private void renderEntityInInventoryFollowsAngle(GuiGraphics guiGraphics, int x, int y, int scale, float angleXComponent, float angleYComponent, LivingEntity entity) {
		Quaternionf pose = new Quaternionf().rotateZ((float) Math.PI);
		Quaternionf cameraOrientation = new Quaternionf().rotateX(angleYComponent * 20 * ((float) Math.PI / 180F));
		pose.mul(cameraOrientation);
		float f2 = entity.yBodyRot;
		float f3 = entity.getYRot();
		float f4 = entity.getXRot();
		float f5 = entity.yHeadRotO;
		float f6 = entity.yHeadRot;
		entity.yBodyRot = 180.0F + angleXComponent * 20.0F;
		entity.setYRot(180.0F + angleXComponent * 40.0F);
		entity.setXRot(-angleYComponent * 20.0F);
		entity.yHeadRot = entity.getYRot();
		entity.yHeadRotO = entity.getYRot();
		net.minecraft.client.gui.screens.inventory.InventoryScreen.renderEntityInInventory(guiGraphics, x - 37, y - 60, x + 37, y + 37, scale, new Vector3f(0, 0, 0), pose, cameraOrientation, entity);
		entity.yBodyRot = f2;
		entity.setYRot(f3);
		entity.setXRot(f4);
		entity.yHeadRotO = f5;
		entity.yHeadRot = f6;
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
		int foodLevel = this.menu.countFoodPointsInInventory();
		this.renderHunger(guiGraphics, foodLevel, this.leftPos + 105, this.topPos + 13);
		this.renderHunger(guiGraphics, foodLevel - 12, this.leftPos + 105, this.topPos + 23);
		int heartLevel = Math.min(14, (foodLevel * 14) / 12);
		ResourceLocation heart = ResourceLocation.fromNamespaceAndPath("villager_inventory", "textures/screens/breed_" + heartLevel + ".png");
		if (heartLevel > 0)
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, heart, this.leftPos + 84, this.topPos + 13, 0, 0, 18, 18, 18, 18);
	}

	public void renderHunger(GuiGraphics guiGraphics, int food, int Xpos, int Ypos) {
		for (int i = 2; i < 13; i += 2) {
			String HungerBar = "";
			if (food > i - 2)
				HungerBar = "hunger_half";
			if (food > i - 1)
				HungerBar = "hunger_full";
			if (HungerBar != "")
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.fromNamespaceAndPath("villager_inventory", "textures/screens/" + HungerBar + ".png"), Xpos, Ypos, 0, 0, 9, 9, 9, 9);
			Xpos += 10;
		}
	}

	public static final ResourceLocation untrusted = ResourceLocation.fromNamespaceAndPath("villager_inventory", "textures/screens/untrusted.png");

	@Override
	protected void renderSlot(GuiGraphics guiGraphics, Slot slot) {
		int requirement = slot.index < 4 ? 10 : 25;
		if (slot.container != this.entity.getInventory() && this.menu.playerRep <= requirement)
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, untrusted, slot.x, slot.y, 0, 0, 16, 16, 16, 16);
		super.renderSlot(guiGraphics, slot);
	}

	@Override
	public boolean keyPressed(int key, int b, int c) {
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(key, b, c);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
	}
}
