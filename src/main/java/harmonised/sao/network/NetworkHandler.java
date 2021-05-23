package harmonised.sao.network;

import harmonised.sao.SAOMod;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkDirection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetworkHandler
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static void registerPackets()
    {
        int index = 0;

        SAOMod.HANDLER.registerMessage( index++, MessageIntArray.class, MessageIntArray::encode, MessageIntArray::decode, MessageIntArray::handlePacket );
    }

    public static void sendToPlayer( MessageIntArray packet, ServerPlayerEntity player )
    {
        SAOMod.HANDLER.sendTo( packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT );
    }
}
