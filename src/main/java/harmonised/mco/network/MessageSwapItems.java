package harmonised.mco.network;

import harmonised.mco.util.Util;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageSwapItems
{
    public int a, b;

    public MessageSwapItems( int a, int b )
    {
        this.a = a;
        this.b = b;
    }

    public MessageSwapItems()
    {
    }

    public static MessageSwapItems decode( PacketBuffer buf )
    {
        MessageSwapItems packet = new MessageSwapItems();

        packet.a = buf.readInt();
        packet.b = buf.readInt();

        return packet;
    }

    public static void encode( MessageSwapItems packet, PacketBuffer buf )
    {
        buf.writeInt( packet.a );
        buf.writeInt( packet.b );
    }

    public static void handlePacket( MessageSwapItems packet, Supplier<NetworkEvent.Context> ctx )
    {
        ctx.get().enqueueWork(() ->
        {
            Util.swapItems( ctx.get().getSender(), packet.a, packet.b );
        });
        ctx.get().setPacketHandled( true );
    }
}