package harmonised.mco.events;

import harmonised.mco.confefeg.McoConfefeg;
import harmonised.mco.network.NetworkHandler;
import harmonised.mco.util.Reference;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventHandler
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void modsLoading(FMLCommonSetupEvent event)
    {
        McoConfefeg.init();
        NetworkHandler.registerPackets();
    }
}
