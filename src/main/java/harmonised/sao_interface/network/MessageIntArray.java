package harmonised.sao_interface.network;

import harmonised.sao_interface.client.Renderer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class MessageIntArray
{
    public int length;
    Set<Integer> items;

    public MessageIntArray( Set<Integer> items )
    {
        this.items = new HashSet<>( items );
    }

    public MessageIntArray()
    {
        items = new HashSet<>();
    }

    public static MessageIntArray decode( PacketBuffer buf )
    {
        MessageIntArray packet = new MessageIntArray();

        int length = buf.readInt();
        for( int i = 0; i < length; i++ )
        {
            packet.items.add( buf.readInt() );
        }

        return packet;
    }

    public static void encode( MessageIntArray packet, PacketBuffer buf )
    {
        buf.writeInt( packet.items.size() );
        for( int item : packet.items )
        {
            buf.writeInt( item );
        }
    }

    public static void handlePacket( MessageIntArray packet, Supplier<NetworkEvent.Context> ctx )
    {
        ctx.get().enqueueWork(() ->
        {
            Renderer.attackers = packet.items;
        });
        ctx.get().setPacketHandled( true );
    }
}