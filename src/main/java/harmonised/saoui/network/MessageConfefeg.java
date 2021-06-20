package harmonised.saoui.network;

import harmonised.saoui.config.Confefeger;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class MessageConfefeg
{
    public static final Logger LOGGER = LogManager.getLogger();

    public CompoundNBT confefeg;

    public MessageConfefeg(CompoundNBT config )
    {
        this.confefeg = config;
    }

    public MessageConfefeg()
    {
    }

    public static MessageConfefeg decode(PacketBuffer buf )
    {
        MessageConfefeg packet = new MessageConfefeg();

        packet.confefeg = buf.readCompoundTag();

        return packet;
    }

    public static void encode(MessageConfefeg packet, PacketBuffer buf )
    {
        buf.writeCompoundTag( packet.confefeg );
    }

    public static void handlePacket(MessageConfefeg packet, Supplier<NetworkEvent.Context> ctx )
    {
        ctx.get().enqueueWork(() ->
        {
            CompoundNBT nbt = packet.confefeg;
            Confefeger confefeger = Confefeger.confefegers.get( nbt.getString( "key" ) );
            if( confefeger != null )
            {
                Confefeger.Confefeg confefeg = confefeger.getConfefeg( nbt.getString( "name" ) );
                if( confefeg != null )
                {
                    Object value = confefeg.get();
                    if( value instanceof Integer )
                        confefeg.setFromServer( nbt.getInt( "value" ) );
                    else if( value instanceof Float )
                        confefeg.setFromServer( nbt.getFloat( "value" ) );
                    else if( value instanceof Double )
                        confefeg.setFromServer( nbt.getDouble( "value" ) );
                    else if( value instanceof String )
                        confefeg.setFromServer( nbt.getString( "value" ) );
                    else
                        LOGGER.error( "Received an invalid type Config! How!?" );
                }
                else
                    LOGGER.error( "Confefeg \"" + nbt.getString( "name" ) + "\" not found in Confefeger \"" + nbt.getString( "key" ) + "\"! Make sure your Client and Server mods are synced!" );
            }
            else
                LOGGER.error( "Confefeger \"" + nbt.getString( "key" ) + "\" not found! Make sure your Client and Server mods are synced!" );
        });
        ctx.get().setPacketHandled( true );
    }
}