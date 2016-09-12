package mrriegel.limelib.tile;

import java.util.List;

import mrriegel.limelib.LimeLib;
import mrriegel.limelib.helper.InvHelper;
import mrriegel.limelib.helper.NBTHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

import com.google.common.collect.Lists;

public class CommonTileInventory extends CommonTile implements IInventory {

	private ItemStack[] stacks;
	public final int SIZE, STACKLIMIT;

	public CommonTileInventory(int size) {
		this(size, 64);
	}

	public CommonTileInventory(int size, int limit) {
		SIZE = size;
		STACKLIMIT = limit;
		stacks = new ItemStack[SIZE];
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public int getSizeInventory() {
		return SIZE;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return stacks[index];
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStack itemstack = ItemStackHelper.getAndSplit(stacks, index, count);
		if (itemstack != null) {
			markDirty();
		}
		return itemstack;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		ItemStack stack = ItemStackHelper.getAndRemove(stacks, index);
		markDirty();
		return stack;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		stacks[index] = stack;
		if (stack != null && stack.stackSize > getInventoryStackLimit()) {
			stack.stackSize = getInventoryStackLimit();
			LimeLib.log.warn("Size of itemstack is too high for inventory.");
		}
		markDirty();

	}

	@Override
	public int getInventoryStackLimit() {
		return STACKLIMIT;
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return true;
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {
	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
		stacks = new ItemStack[SIZE];
	}

	@Override
	public ItemStack[] getDroppingItems() {
		return stacks;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return InvHelper.hasItemHandler(this, facing);
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return (T) InvHelper.getItemHandler(this, facing);
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		List<ItemStack> lis = NBTHelper.getItemStackList(compound, "Items");
		for (int i = 0; i < lis.size(); i++) {
			stacks[i] = lis.get(i);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTHelper.setItemStackList(compound, "Items", Lists.newArrayList(stacks));
		return super.writeToNBT(compound);
	}

}