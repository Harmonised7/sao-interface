package harmonised.annotfig.network;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageConfig
{
    public CompoundNBT config;

    public MessageConfig( CompoundNBT config )
    {
        this.config = config;
    }

    public MessageConfig()
    {
    }

    public static MessageConfig decode(PacketBuffer buf )
    {
        MessageConfig packet = new MessageConfig();

        packet.config = buf.readNbt();

        return packet;
    }

    public static void encode(MessageConfig packet, PacketBuffer buf )
    {
        buf.writeNbt( packet.config );
    }

    public static void handlePacket(MessageConfig packet, Supplier<NetworkEvent.Context> ctx )
    {
        ctx.get().enqueueWork(() ->
        {
            //Set
        });
        ctx.get().setPacketHandled( true );
    }
}