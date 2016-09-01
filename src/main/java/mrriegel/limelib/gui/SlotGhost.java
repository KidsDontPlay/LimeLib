package mrriegel.limelib.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotGhost extends Slot {
	public SlotGhost(IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean canTakeStack(EntityPlayer playerIn) {
		ItemStack holding = playerIn.inventory.getItemStack();

		if (holding != null) {
			holding = holding.copy();
			holding.stackSize = 1;
		}
		this.putStack(holding);
		return false;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		ItemStack copy = stack.copy();
		copy.stackSize = 1;
		this.putStack(copy);
		return false;
	}

	@Override
	public ItemStack decrStackSize(int amount) {
		this.putStack(null);
		return null;
	}
}
