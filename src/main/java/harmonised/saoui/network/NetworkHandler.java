package harmonised.saoui.network;

import harmonised.saoui.SAOMod;
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
        SAOMod.HANDLER.registerMessage( index++, MessageSwapItems.class, MessageSwapItems::encode, MessageSwapItems::decode, MessageSwapItems::handlePacket );
        SAOMod.HANDLER.registerMessage( index++, MessageCraft.class, MessageCraft::encode, MessageCraft::decode, MessageCraft::handlePacket );
        SAOMod.HANDLER.registerMessage( index++, MessageConfig.class, MessageConfig::encode, MessageConfig::decode, MessageConfig::handlePacket );
        SAOMod.HANDLER.registerMessage( index++, MessageNBT.class, MessageNBT::encode, MessageNBT::decode, MessageNBT::handlePacket );
    }

    public static void sendToPlayer( MessageIntArray packet, ServerPlayerEntity player )
    {
        SAOMod.HANDLER.sendTo( packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT );
    }

    public static void sendToPlayer( MessageNBT packet, ServerPlayerEntity player )
    {
        SAOMod.HANDLER.sendTo( packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT );
    }

    public static void sendToServer( MessageSwapItems packet )
    {
        SAOMod.HANDLER.sendToServer( packet );
    }

    public static void sendToServer( MessageCraft packet )
    {
        SAOMod.HANDLER.sendToServer( packet );
    }

    public static void sendToPlayer(MessageConfig packet, ServerPlayerEntity player )
    {
        SAOMod.HANDLER.sendTo( packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT );
    }
}
