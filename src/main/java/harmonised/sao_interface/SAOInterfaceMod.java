package harmonised.sao_interface;

import harmonised.sao_interface.client.ClientHandler;
import harmonised.sao_interface.events.EventHandler;
import harmonised.sao_interface.util.Reference;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod( Reference.MOD_ID )
public class SAOInterfaceMod
{
    private static final Logger LOGGER = LogManager.getLogger();

    public SAOInterfaceMod()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener( this::modsLoading );
        FMLJavaModLoadingContext.get().getModEventBus().addListener( this::clientLoading );
    }

    private void modsLoading( FMLCommonSetupEvent event )
    {
        MinecraftForge.EVENT_BUS.register( EventHandler.class );
    }

    private void clientLoading( FMLClientSetupEvent event )
    {
        ClientHandler.init();
    }
}
