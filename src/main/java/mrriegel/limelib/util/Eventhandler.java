package mrriegel.limelib.util;

import mrriegel.limelib.tile.CommonTile;
import mrriegel.limelib.tile.IOwneable;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class Eventhandler {

	@SubscribeEvent
	public void left(LeftClickBlock event) {
		if (event.getEntityPlayer() != null) {
			TileEntity tile = event.getWorld().getTileEntity(event.getPos());
			if (tile instanceof IOwneable) {
				IOwneable o = (IOwneable) tile;
				if (!o.canAccess(event.getEntityPlayer().getName())) {
					// if (!event.getWorld().isRemote)
					// event.getEntityPlayer().addChatComponentMessage(new
					// TextComponentString("No permission!"));
					event.setCanceled(true);
					event.setResult(Result.DENY);
					event.setUseBlock(Result.DENY);
				}
			}
		}
	}

	@SubscribeEvent
	public void right(RightClickBlock event) {
		if (event.getEntityPlayer() != null) {
			TileEntity tile = event.getWorld().getTileEntity(event.getPos());
			if (tile instanceof IOwneable) {
				IOwneable o = (IOwneable) tile;
				if (!o.canAccess(event.getEntityPlayer().getName())) {
					// if (!event.getWorld().isRemote)
					// event.getEntityPlayer().addChatComponentMessage(new
					// TextComponentString("No permission!"));
					// event.setCanceled(true);
					event.setResult(Result.DENY);
					event.setUseBlock(Result.DENY);
				}
			}
		}
	}

	@SubscribeEvent
	public void tick(WorldTickEvent event) {
		if(event.world == null || event.world.loadedTileEntityList == null)
			return;
		if (event.phase == Phase.END && event.side == Side.SERVER) {
			TileEntity tile = null;
			for(Iterator<TileEntity> iter = event.world.loadedTileEntityList.iterator(); iter.hasNext(); tile = iter.next()) {
				if (tile instanceof CommonTile) {
					if (((CommonTile) tile).needsSync()) {
						((CommonTile) tile).sync();
						((CommonTile) tile).setSyncDirty(false);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void attachCapa(AttachCapabilitiesEvent<?> event) {
		if (event.getObject() instanceof TileEntity) {

		} else if (event.getObject() instanceof Entity) {

		} else if (event.getObject() instanceof Item) {

		} else if (event.getObject() instanceof World) {

		}
	}

}
