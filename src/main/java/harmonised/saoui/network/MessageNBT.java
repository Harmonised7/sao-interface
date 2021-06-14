package harmonised.saoui.network;

import harmonised.saoui.client.gui.Renderer;
import harmonised.saoui.util.Util;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class MessageNBT
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
        packet.nbt = buf.readNbt();

        return packet;
    }

    public static void encode(MessageNBT packet, PacketBuffer buf )
    {
        buf.writeInt( packet.type );
        buf.writeNbt( packet.nbt );
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
