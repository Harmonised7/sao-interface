package harmonised.mco.network;

import harmonised.mco.confefeg.Confefeger;
import net.minecraft.world.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class MessageConfefeg implements Message
{
    public static final Logger LOGGER = LogManager.getLogger();

    public CompoundNBT confefeg;

    public MessageConfefeg(CompoundNBT config)
    {
        this.confefeg = config;
    }

    public MessageConfefeg()
    {
    }

    public static MessageConfefeg decode(PacketBuffer buf)
    {
        MessageConfefeg packet = new MessageConfefeg();

        packet.confefeg = buf.readCompoundTag();

        return packet;
    }

    public static void encode(MessageConfefeg packet, PacketBuffer buf)
    {
        buf.writeCompoundTag(packet.confefeg);
    }

    public static void handlePacket(MessageConfefeg packet, Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() ->
        {
            CompoundNBT nbt = packet.confefeg;
            Confefeger confefeger = Confefeger.confefegers.get(nbt.getString("key"));
            if(confefeger != null)
            {
                Confefeger.Confefeg confefeg = confefeger.getConfefeg(nbt.getString("name"));
                if(confefeg != null)
                {
                    Object value = confefeg.get();
                    if(ctx.get().getDirection().getReceptionSide().equals(LogicalSide.CLIENT))
                    {
                        if(value instanceof Integer)
                            confefeg.setFromServer(nbt.getInt("value"));
                        else if(value instanceof Float)
                            confefeg.setFromServer(nbt.getFloat("value"));
                        else if(value instanceof Double)
                            confefeg.setFromServer(nbt.getDouble("value"));
                        else if(value instanceof String)
                            confefeg.setFromServer(nbt.getString("value"));
                        else if(value instanceof Boolean)
                            confefeg.setFromServer(nbt.getBoolean("value"));
                        else
                            LOGGER.error("Received an invalid type Config! How!?");
                    }
                    else
                    {
                        if(value instanceof Integer)
                            confefeg.setLocal(nbt.getInt("value"));
                        else if(value instanceof Float)
                            confefeg.setLocal(nbt.getFloat("value"));
                        else if(value instanceof Double)
                            confefeg.setLocal(nbt.getDouble("value"));
                        else if(value instanceof String)
                            confefeg.setLocal(nbt.getString("value"));
                        else if(value instanceof Boolean)
                            confefeg.setLocal(nbt.getBoolean("value"));
                        else
                            LOGGER.error("Received an invalid type Config! How!?");
                        confefeger.saveConfefegs();
                        for(ServerPlayerEntity player : ctx.get().getSender().getServer().getPlayerList().getPlayers())
                        {
                            Confefeger.syncConfefeg(player, confefeg);
                        }
                    }
                }
                else
                    LOGGER.error("Confefeg \"" + nbt.getString("name") + "\" not found in Confefeger \"" + nbt.getString("key") + "\"! Make sure your Client and Server mods are synced!");
            }
            else
                LOGGER.error("Confefeger \"" + nbt.getString("key") + "\" not found! Make sure your Client and Server mods are synced!");
        });
        ctx.get().setPacketHandled(true);
    }
}