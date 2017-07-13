package mrriegel.limelib.block;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.reflect.ConstructorUtils;

import com.google.common.collect.Lists;

import mrriegel.limelib.helper.NBTStackHelper;
import mrriegel.limelib.helper.RecipeHelper;
import mrriegel.limelib.tile.CommonTile;
import mrriegel.limelib.tile.IDataKeeper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public abstract class CommonBlockContainer<T extends CommonTile> extends CommonBlock {

	protected boolean clearRecipe = true;

	public CommonBlockContainer(Material materialIn, String name) {
		super(materialIn, name);
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		TileEntity t = worldIn.getTileEntity(pos);
		if (t instanceof CommonTile)
			for (ItemStack stack : ((CommonTile) t).getDroppingItems())
				spawnAsEntity(worldIn, pos, stack.copy());
		worldIn.removeTileEntity(pos);
		worldIn.updateComparatorOutputLevel(pos, this);
	}

	@Override
	public void registerBlock() {
		super.registerBlock();
		if (!Stream.of(getTile().getConstructors()).anyMatch((c) -> c.getParameterCount() == 0))
			throw new IllegalStateException(getTile() + " needs a public default constructor.");
		GameRegistry.registerTileEntity(getTile(), getUnlocalizedName());
		if (clearRecipe && IDataKeeper.class.isAssignableFrom(getTile()) && !getItemBlock().getHasSubtypes()) {
			final ItemStack result = new ItemStack(this);
			ShapelessRecipes r = new ShapelessRecipes("", NBTStackHelper.set(new ItemStack(this), "ClEaR", true), NonNullList.from(Ingredient.EMPTY, RecipeHelper.getIngredient(new ItemStack(this.getItemBlock())))) {
				@Override
				public ItemStack getCraftingResult(InventoryCrafting inv) {
					return result;
				}
			};
			RecipeHelper.add(r);
		}
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		try {
			return ConstructorUtils.invokeConstructor(getTile());
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
			e.printStackTrace();
			return null;
		}
	}

	protected abstract Class<? extends T> getTile();

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			return true;
		} else {
			TileEntity tile = worldIn.getTileEntity(pos);
			if (tile instanceof CommonTile) {
				return ((CommonTile) tile).openGUI((EntityPlayerMP) playerIn);
			}
			return false;
		}
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if (worldIn.getTileEntity(pos) instanceof IDataKeeper && NBTStackHelper.get(stack, "idatakeeper", Boolean.class)) {
			IDataKeeper tile = (IDataKeeper) worldIn.getTileEntity(pos);
			tile.readFromStack(stack);
			((TileEntity) tile).markDirty();
		}
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		List<ItemStack> lis = super.getDrops(world, pos, state, fortune);
		ItemStack stack = ItemStack.EMPTY;
		TileEntity t = world.getTileEntity(pos);
		if (t instanceof IDataKeeper && lis.size() == 1 && lis.get(0).getItem() == Item.getItemFromBlock(state.getBlock())) {
			IDataKeeper tile = (IDataKeeper) t;
			stack = lis.get(0).copy();
			NBTStackHelper.set(stack, "idatakeeper", true);
			tile.writeToStack(stack);
		}
		return !stack.isEmpty() ? Lists.newArrayList(stack) : lis;
	}

	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
		if (!(worldIn.getTileEntity(pos) instanceof IDataKeeper))
			return;
		List<ItemStack> lis = getDrops(worldIn, pos, state, 0);
		if (!player.capabilities.isCreativeMode && lis.size() == 1) {
			worldIn.setBlockToAir(pos);
			spawnAsEntity(worldIn, pos, lis.get(0));
		}
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		ItemStack stack = super.getPickBlock(state, target, world, pos, player);
		if (player.isSneaking() && player.capabilities.isCreativeMode && world.getTileEntity(pos) instanceof IDataKeeper && !stack.isEmpty()) {
			IDataKeeper tile = (IDataKeeper) world.getTileEntity(pos);
			NBTStackHelper.set(stack, "idatakeeper", true);
			tile.writeToStack(stack);
		}
		return stack;
	}

	@Override
	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if (tileentity instanceof CommonTile)
			((CommonTile) tileentity).neighborChanged(state, blockIn, fromPos);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
		super.addInformation(stack, world, tooltip, advanced);
		if (clearRecipe && NBTStackHelper.get(stack, "ClEaR", Boolean.class))
			tooltip.add(TextFormatting.YELLOW + "Clear content");
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return IInventory.class.isAssignableFrom(getTile());
	}

	@Override
	public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
		TileEntity t = worldIn.getTileEntity(pos);
		if (t instanceof IInventory)
			return Container.calcRedstoneFromInventory((IInventory) t);
		return 0;
	}
}
