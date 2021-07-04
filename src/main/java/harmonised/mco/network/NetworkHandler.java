package harmonised.mco.network;

import harmonised.mco.MCOMod;
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

        MCOMod.HANDLER.registerMessage( index++, MessageIntArray.class, MessageIntArray::encode, MessageIntArray::decode, MessageIntArray::handlePacket );
        MCOMod.HANDLER.registerMessage( index++, MessageSwapItems.class, MessageSwapItems::encode, MessageSwapItems::decode, MessageSwapItems::handlePacket );
        MCOMod.HANDLER.registerMessage( index++, MessageCraft.class, MessageCraft::encode, MessageCraft::decode, MessageCraft::handlePacket );
        MCOMod.HANDLER.registerMessage( index++, MessageConfefeg.class, MessageConfefeg::encode, MessageConfefeg::decode, MessageConfefeg::handlePacket );
        MCOMod.HANDLER.registerMessage( index++, MessageNBT.class, MessageNBT::encode, MessageNBT::decode, MessageNBT::handlePacket );
    }

    public static void sendToPlayer( MessageIntArray packet, ServerPlayerEntity player )
    {
        MCOMod.HANDLER.sendTo( packet, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT );
    }

    public static void sendToPlayer( MessageNBT packet, ServerPlayerEntity player )
    {
        MCOMod.HANDLER.sendTo( packet, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT );
    }

    public static void sendToServer( MessageSwapItems packet )
    {
        MCOMod.HANDLER.sendToServer( packet );
    }

    public static void sendToServer( MessageCraft packet )
    {
        MCOMod.HANDLER.sendToServer( packet );
    }

    public static void sendToPlayer( MessageConfefeg packet, ServerPlayerEntity player )
    {
        MCOMod.HANDLER.sendTo( packet, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT );
    }
}
