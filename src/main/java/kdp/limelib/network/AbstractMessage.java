package kdp.limelib.network;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import org.apache.commons.lang3.Validate;

import kdp.limelib.ClientHelper;

public abstract class AbstractMessage {
    protected CompoundNBT nbt = new CompoundNBT();

    public AbstractMessage() {
    }

    public AbstractMessage(CompoundNBT nbt) {
        this.nbt = nbt;
    }

    public final void encode(PacketBuffer buffer) {
        buffer.writeCompoundTag(nbt);
    }

    public final void decode(PacketBuffer buffer) {
        nbt = buffer.readCompoundTag();
    }

    public final void handleMessage(AbstractMessage message, Context context) {
        nbt = message.nbt;
        context.enqueueWork(() -> {
            Validate.isTrue(context.getDirection().getReceptionSide() != context.getDirection().getOriginationSide(),
                    "Why from %s to %s?", context.getDirection().getOriginationSide().toString(),
                    context.getDirection().getReceptionSide().toString());
            PlayerEntity player = context.getDirection().getReceptionSide() == LogicalSide.SERVER ?
                    context.getSender() :
                    DistExecutor.callWhenOn(Dist.CLIENT,
                            () -> () -> Minecraft.getInstance().player) /*getClientPlayer().get().get()*/;
            handleMessage(player);
            context.setPacketHandled(true);
        });
    }

    public abstract void handleMessage(PlayerEntity player);

    @Deprecated
    private static Supplier<Supplier<PlayerEntity>> getClientPlayer() {
        return () -> () -> ClientHelper.getClientPlayer();
    }
}
