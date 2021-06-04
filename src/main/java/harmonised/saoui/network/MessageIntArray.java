package harmonised.saoui.network;

import harmonised.saoui.client.gui.Renderer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class MessageIntArray
{
    int type;
    Set<Integer> items;

    public MessageIntArray( int type, Set<Integer> items )
    {
        this.type = type;
        this.items = new HashSet<>( items );
    }

    public MessageIntArray()
    {
        items = new HashSet<>();
    }

    public static MessageIntArray decode(PacketBuffer buf )
    {
        MessageIntArray packet = new MessageIntArray();

        packet.type = buf.readInt();
        int length = buf.readInt();
        for( int i = 0; i < length; i++ )
        {
            packet.items.add( buf.readInt() );
        }

        return packet;
    }

    public static void encode(MessageIntArray packet, PacketBuffer buf )
    {
        buf.writeInt( packet.type );
        buf.writeInt( packet.items.size() );
        for( int item : packet.items )
        {
            buf.writeInt( item );
        }
    }

    public static void handlePacket(MessageIntArray packet, Supplier<NetworkEvent.Context> ctx )
    {
        ctx.get().enqueueWork(() ->
        {
            switch( packet.type )
            {
                case 0:
                    Renderer.attackers = packet.items;
                    break;

                case 1:
                    Renderer.invisibles = packet.items;
                    break;
            }
        });
        ctx.get().setPacketHandled( true );
    }
}