package mrriegel.limelib.util;

import java.util.List;

import mrriegel.limelib.helper.NBTStackHelper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;

import com.google.common.collect.Lists;

public class ItemInvWrapper extends InvWrapper {

	private final ItemStack stack;
	private static final String NAME = "invwrapper";
	private String name;

	public ItemInvWrapper(ItemStack stack, int size, String name) {
		super(getInv(stack, size, name));
		this.stack = stack;
		this.name = name;
	}

	public ItemInvWrapper(ItemStack stack, int size) {
		this(stack, size, NAME);
	}

	private static IInventory getInv(ItemStack stack, int size, String name) {
		InventoryBasic inv = new InventoryBasic("null", false, size);
		List<ItemStack> lis = NBTStackHelper.getItemStackList(stack, name);
		if (lis.size() < size) {
			List<ItemStack> l = Lists.newArrayList();
			for (int i = 0; i < size; i++)
				l.add(null);
			NBTStackHelper.setItemStackList(stack, name, l);
			lis = Lists.newArrayList(l);
		}
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			inv.setInventorySlotContents(i, lis.get(i));
		}
		return inv;
	}

	public ItemStack getStack() {
		return stack;
	}

	@Override
	public int getSlots() {
		return NBTStackHelper.getItemStackList(stack, name).size();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return NBTStackHelper.getItemStackList(stack, name).get(slot);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		ItemStack s = super.insertItem(slot, stack, simulate);
		save();
		return s;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack s = super.extractItem(slot, amount, simulate);
		save();
		return s;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		super.setStackInSlot(slot, stack);
		save();
	}

	private void save() {
		List<ItemStack> l = Lists.newArrayList();
		for (int i = 0; i < getInv().getSizeInventory(); i++)
			l.add(getInv().getStackInSlot(i));
		NBTStackHelper.setItemStackList(this.stack, name, l);
	}

}
