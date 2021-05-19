package harmonised.sao_interface.network;

import harmonised.sao_interface.SAOInterfaceMod;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTTypes;
import net.minecraft.nbt.NBTUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetworkHandler
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static void registerPackets()
    {
        int index = 0;

        SAOInterfaceMod.HANDLER.registerMessage( index++, MessageIntArray.class, MessageIntArray::encode, MessageIntArray::decode, MessageIntArray::handlePacket );
    }
}
