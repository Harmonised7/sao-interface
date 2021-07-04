package harmonised.mco.network;

import harmonised.mco.server.ServerHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageCraft
{
    public ResourceLocation recipe;
    public int amount;

    public MessageCraft( ResourceLocation recipe, int amount )
    {
        this.recipe = recipe;
        this.amount = amount;
    }

    public MessageCraft()
    {
    }

    public static MessageCraft decode(PacketBuffer buf )
    {
        MessageCraft packet = new MessageCraft();

        packet.recipe = new ResourceLocation( buf.readString() );
        packet.amount = buf.readInt();

        return packet;
    }

    public static void encode(MessageCraft packet, PacketBuffer buf )
    {
        buf.writeString( packet.recipe.toString() );
        buf.writeInt( packet.amount );
    }

    public static void handlePacket(MessageCraft packet, Supplier<NetworkEvent.Context> ctx )
    {
        ctx.get().enqueueWork(() ->
        {
            ServerHandler.craftItem( ctx.get().getSender(), packet.recipe, packet.amount);
        });
        ctx.get().setPacketHandled( true );
    }
}