package harmonised.sao_interface.client;

import harmonised.sao_interface.client.gui.Renderer;
import harmonised.sao_interface.client.gui.SAOScreen;
import harmonised.sao_interface.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientHandler
{
    private static boolean wasShowSaoInterface = false;
    public static final KeyBinding SHOW_SAO_INTERFACE = new KeyBinding( "key.sao-interface.showSaoInterface", GLFW.GLFW_KEY_H, "category.sao-interfface" );

    public static void init()
    {
        MinecraftForge.EVENT_BUS.register( new Renderer() );
        ClientRegistry.registerKeyBinding( SHOW_SAO_INTERFACE );
    }

    @SubscribeEvent
    public static void keyPressEvent( net.minecraftforge.client.event.InputEvent.KeyInputEvent event )
    {
        if( !wasShowSaoInterface && SHOW_SAO_INTERFACE.isDown() )
        {
            Minecraft.getInstance().setScreen( new SAOScreen( new TranslationTextComponent( "" ) ) );
        }
        wasShowSaoInterface = SHOW_SAO_INTERFACE.isDown();
    }
}