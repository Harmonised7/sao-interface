package harmonised.saoui.events;

import harmonised.saoui.config.SaouiConfefeg;
import harmonised.saoui.network.NetworkHandler;
import harmonised.saoui.util.Reference;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber( modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD )
public class ModEventHandler
{
    @SubscribeEvent( priority = EventPriority.LOWEST )
    public static void modsLoading( FMLCommonSetupEvent event )
    {
        SaouiConfefeg.init();
        NetworkHandler.registerPackets();
    }
}
