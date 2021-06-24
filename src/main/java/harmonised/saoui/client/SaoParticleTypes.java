package harmonised.saoui.client;

import harmonised.saoui.util.Reference;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class SaoParticleTypes
{
    private static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Reference.MOD_ID );
    public static final RegistryObject<BasicParticleType> TRIANGLE = PARTICLES.register("triangle", () -> new BasicParticleType( true ) );

    public static void init()
    {
        PARTICLES.register( FMLJavaModLoadingContext.get().getModEventBus() );
    }
}
