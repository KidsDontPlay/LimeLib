package kdp.limelib.gui;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.MapMaker;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

public class InventoryWrapper {

    private final IInventory inventory;
    private final IItemHandler itemHandler;

    private static final Map<Object, InventoryWrapper> CACHE = new MapMaker().weakKeys().makeMap();

    private InventoryWrapper(Object inventory) {
        this.inventory = Objects.requireNonNull(inventory) instanceof IInventory ? (IInventory) inventory : null;
        this.itemHandler = inventory instanceof IItemHandler ? (IItemHandler) inventory : null;
    }

    public static InventoryWrapper of(Object inventory) {
        return CACHE.computeIfAbsent(inventory, InventoryWrapper::new);
    }

    public boolean isItemHandler() {
        return itemHandler != null;
    }

    public boolean isInventory() {
        return inventory != null;
    }

    public Optional<IInventory> getInventory() {
        return Optional.ofNullable(inventory);
    }

    public Optional<IItemHandler> getItemHandler() {
        return Optional.ofNullable(itemHandler);
    }

    public Object getInv() {
        return itemHandler != null ? itemHandler : inventory;
    }

    // Wrapper methods

    public int getSize() {
        return itemHandler != null ? itemHandler.getSlots() : inventory.getSizeInventory();
    }

    public ItemStack getStackInSlot(int index) {
        return isItemHandler() ? itemHandler.getStackInSlot(index) : inventory.getStackInSlot(index);
    }

    public void setStackInSlot(int index, ItemStack stack) {
        if (isItemHandler()) {
            if (itemHandler instanceof IItemHandlerModifiable) {
                ((IItemHandlerModifiable) itemHandler).setStackInSlot(index, stack);
            } else {
                throw new IllegalArgumentException(itemHandler + " is not modifiable");
            }
        } else {
            inventory.setInventorySlotContents(index, stack);
        }
    }
}
