package mrriegel.limelib.item;

import java.util.Collections;
import java.util.Set;

import mrriegel.limelib.helper.StackHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class CommonItemTool extends ItemTool {

	protected Set<String> toolClasses;
	protected final Set<Block> effectiveBlocks;

	public CommonItemTool(String name, ToolMaterial material, String... toolClasses) {
		super(0F, 0F, material, null);
		setRegistryName(name);
		this.toolClasses = toolClasses != null ? Sets.newHashSet(toolClasses) : Collections.EMPTY_SET;
		effectiveBlocks = effectives(this.toolClasses);
		setUnlocalizedName(getRegistryName().toString());
	}

	private static Set<Block> effectives(Set<String> toolClasses) {
		Set<Block> blocks = Sets.newHashSet();
		if (toolClasses.contains("pickaxe"))
			blocks.addAll(ReflectionHelper.getPrivateValue(ItemPickaxe.class, null, 0));
		if (toolClasses.contains("shovel"))
			blocks.addAll(ReflectionHelper.getPrivateValue(ItemSpade.class, null, 0));
		if (toolClasses.contains("axe"))
			blocks.addAll(ReflectionHelper.getPrivateValue(ItemAxe.class, null, 0));
		return blocks;
	}

	@Override
	public Set<String> getToolClasses(ItemStack stack) {
		return toolClasses;
	}

	protected final double getBaseDamage(ItemStack stack) {
		if (getToolClasses(stack).contains("axe"))
			return 5.0f;
		else if (getToolClasses(stack).contains("shovel"))
			return 1.5f;
		else if (getToolClasses(stack).contains("pickaxe"))
			return 1.0f;
		return 0f;
	}

	protected final double getBaseSpeed(ItemStack stack) {
		if (getToolClasses(stack).contains("pickaxe"))
			return -2.8f;
		else if (getToolClasses(stack).contains("shovel") || getToolClasses(stack).contains("axe"))
			return -3.0f;
		return 0f;
	}

	protected double getAttackDamage(ItemStack stack) {
		return getBaseDamage(stack) + toolMaterial.getDamageVsEntity();
	};

	protected double getAttackSpeed(ItemStack stack) {
		return getBaseSpeed(stack);
	};

	protected float getDigSpeed(ItemStack stack, float efficiencyOnProperMaterial) {
		return efficiencyOnProperMaterial;
	};

	public float getStrVsBlock(ItemStack stack, IBlockState state) {
		if (toolClasses.contains("pickaxe"))
			return getDigSpeed(stack, Items.DIAMOND_PICKAXE.getStrVsBlock(stack, state));
		if (toolClasses.contains("axe"))
			return getDigSpeed(stack, Items.DIAMOND_AXE.getStrVsBlock(stack, state));
		for (String type : getToolClasses(stack)) {
			if (state.getBlock().isToolEffective(type, state))
				return getDigSpeed(stack, efficiencyOnProperMaterial);
		}
		return this.effectiveBlocks.contains(state.getBlock()) ? getDigSpeed(stack, efficiencyOnProperMaterial) : 1.0F;
	}

	@Override
	public int getHarvestLevel(ItemStack stack, String toolClass, EntityPlayer player, IBlockState blockState) {
		return getToolClasses(stack).contains(toolClass) ? toolMaterial.getHarvestLevel() : super.getHarvestLevel(stack, toolClass, player, blockState);
	}

	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		boolean sup = super.getIsRepairable(toRepair, repair);
		if (!sup) {
			sup = StackHelper.equalOreDict(toolMaterial.getRepairItemStack(), repair);
		}
		return sup;
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot equipmentSlot, ItemStack stack) {
		Multimap<String, AttributeModifier> multimap = HashMultimap.create();
		if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", getAttackDamage(stack), 0));
			multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", getAttackSpeed(stack), 0));
		}
		return multimap;
	}

	@Override
	public boolean canHarvestBlock(IBlockState state, ItemStack stack) {
		if (toolClasses.contains("pickaxe"))
			return Items.DIAMOND_PICKAXE.canHarvestBlock(state, stack);
		if (toolClasses.contains("shovel"))
			return Items.DIAMOND_SHOVEL.canHarvestBlock(state, stack);
		return super.canHarvestBlock(state, stack);
		//		return state.getBlock().getMaterial(state).isToolNotRequired();
	}

	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!player.getHeldItem(hand).getItem().getToolClasses(player.getHeldItem(hand)).contains("shovel"))
			return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
		return Items.DIAMOND_SHOVEL.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}

}
