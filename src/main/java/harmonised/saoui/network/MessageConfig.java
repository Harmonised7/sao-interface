package harmonised.saoui.network;

import harmonised.saoui.config.ConfigEntry;
import harmonised.saoui.config.Configs;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Supplier;

public class MessageConfig
{
    public static final Logger LOGGER = LogManager.getLogger();

    public String configKey;
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

    public static void handlePacket( MessageConfig packet, Supplier<NetworkEvent.Context> ctx )
    {
        ctx.get().enqueueWork(() ->
        {
            ConfigEntry config = Configs.getConfig( packet.configKey );
            if( config == null )
                LOGGER.error( "Invalid config key " + packet.configKey );
            else
            {
                for( String key : packet.config.getAllKeys() )
                {
                    config.setField( key, packet.config.getDouble( key ) );
                }
            }
        });
        ctx.get().setPacketHandled( true );
    }
}