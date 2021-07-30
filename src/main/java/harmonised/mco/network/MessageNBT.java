package harmonised.mco.network;

import harmonised.mco.client.gui.Renderer;
import harmonised.mco.util.Util;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageNBT implements Message
{
    int type;
    CompoundNBT nbt;

    public MessageNBT( int type, CompoundNBT nbt )
    {
        this.type = type;
        this.nbt = nbt;

    }

    public MessageNBT()
    {
    }

    public static MessageNBT decode(PacketBuffer buf )
    {
        MessageNBT packet = new MessageNBT();

        packet.type = buf.readInt();
        packet.nbt = buf.readCompoundTag();

        return packet;
    }

    public static void encode(MessageNBT packet, PacketBuffer buf )
    {
        buf.writeInt( packet.type );
        buf.writeCompoundTag( packet.nbt );
    }

    public static void handlePacket(MessageNBT packet, Supplier<NetworkEvent.Context> ctx )
    {
        ctx.get().enqueueWork(() ->
        {
            switch( packet.type )
            {
                case 0:
                    Renderer.effects = Util.nbtToEntityEffectInstanceMap( packet.nbt );
                    break;
            }
        });
        ctx.get().setPacketHandled( true );
    }
}
