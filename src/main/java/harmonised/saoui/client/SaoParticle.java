package harmonised.saoui.client;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SaoParticle extends SpriteTexturedParticle
{
    public SaoParticle(ClientWorld world, double x, double y, double z )
    {
        super( world, x, y, z, 0.0D, 0.0D, 0.0D);
        this.xd *= (double)0.8F;
        this.yd *= (double)0.8F;
        this.zd *= (double)0.8F;
        this.yd = (double)(this.random.nextFloat() * 0.4F + 0.05F);
        this.quadSize *= this.random.nextFloat() * 2.0F + 0.2F;
        this.lifetime = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
    }

    public IParticleRenderType getRenderType()
    {
        return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public int getLightColor(float p_189214_1_) {
        int i = super.getLightColor(p_189214_1_);
        int j = 240;
        int k = i >> 16 & 255;
        return 240 | k << 16;
    }

    public float getQuadSize(float p_217561_1_)
    {
        float f = ((float)this.age + p_217561_1_) / (float)this.lifetime;
        return this.quadSize * (1.0F - f * f);
    }

    public void tick()
    {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        float f = (float)this.age / (float)this.lifetime;
        if (this.random.nextFloat() > f)
        {
            this.level.addParticle(ParticleTypes.SMOKE, this.x, this.y, this.z, this.xd, this.yd, this.zd);
        }

        if (this.age++ >= this.lifetime)
        {
            this.remove();
        } else {
            this.yd -= 0.03D;
            this.move(this.xd, this.yd, this.zd);
            this.xd *= (double)0.999F;
            this.yd *= (double)0.999F;
            this.zd *= (double)0.999F;
            if (this.onGround) {
                this.xd *= (double)0.7F;
                this.zd *= (double)0.7F;
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<BasicParticleType>
    {
        private final IAnimatedSprite sprite;

        public Factory( IAnimatedSprite sprite )
        {
            this.sprite = sprite;
        }

        public Particle createParticle(BasicParticleType p_199234_1_, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed )
        {
            SaoParticle particle = new SaoParticle(world, x, y, z);
            particle.pickSprite( this.sprite );
            return particle;
        }
    }
}
