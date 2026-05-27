package villagerinventory;

import net.minecraft.world.entity.player.Player;

public interface ViewableInventory {
	abstract Player getViewer();

	abstract void setViewer(Player player);
}
