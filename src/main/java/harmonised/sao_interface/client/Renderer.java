package harmonised.sao_interface.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import harmonised.sao_interface.util.Reference;
import harmonised.sao_interface.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class Renderer
{
    private static final Map<LivingEntity, HPBar> hpBars = new HashMap<>();
    private static final ResourceLocation HP_BAR = Util.getResLoc( Reference.MOD_ID, "textures/gui/hpbar.png" );
    private static final ResourceLocation HP_BAR_INSIDE = Util.getResLoc( Reference.MOD_ID, "textures/gui/hpbarinside.png" );
    private static final Minecraft mc = Minecraft.getInstance();
    private static int blitOffset = 0;

    @SubscribeEvent
    public void handleRender( RenderWorldLastEvent event )
    {
        PlayerEntity player = mc.player;
        float partialTicks = event.getPartialTicks();
        World world = mc.level;
        Vector3d cameraCenter = mc.gameRenderer.getMainCamera().getPosition();
        BlockPos originBlockPos = mc.gameRenderer.getMainCamera().getBlockPosition();
        MatrixStack stack = event.getMatrixStack();
        stack.pushPose();
        stack.translate( -cameraCenter.get( Direction.Axis.X ) + 0.5, -cameraCenter.get( Direction.Axis.Y ) + 0.5, -cameraCenter.get( Direction.Axis.Z ) + 0.5 );
        stack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
//        stack.mulPose(Vector3f.YP.rotationDegrees(180.0F));

        IRenderTypeBuffer.Impl buffer = mc.renderBuffers().bufferSource();

//        blit( matrix4f, -width/2, width/2, -height/2, height/2, 0, 0, 1, 0, 0.5f );

        float w, h;
        double polyDegRange;
        double degOffset;
        int polyCount = 1;
//        float diameter = w*polyCount;
        double polyDegStep;
        int fullBarWidth = 256;
        float polyWidth = fullBarWidth/(float)polyCount;
        int renderDistance = 128;
        float playerPitch = -25;
        float scale;
        RenderSystem.enableBlend();

        for( LivingEntity livingEntity : world.getEntitiesOfClass( LivingEntity.class, new AxisAlignedBB( originBlockPos.above( renderDistance ).north( renderDistance ).east( renderDistance ), originBlockPos.below( renderDistance ).south( renderDistance ).west( renderDistance ) ) ) )
        {
            if( !hpBars.containsKey( livingEntity ) )
                hpBars.put( livingEntity, new HPBar( livingEntity ) );
            HPBar hpBar = hpBars.get( livingEntity );
            hpBar.update( partialTicks );

            boolean isPlayer = livingEntity == player;
            if( isPlayer )
                continue;

            if( isPlayer )
                scale = 0.3f;
            else
                scale = 1;

            stack.pushPose();
            Vector3d livingEntityEyePos = livingEntity.getEyePosition( partialTicks );
            //Translate to eyes
            stack.translate( livingEntityEyePos.get( Direction.Axis.X ) - 0.5, -livingEntityEyePos.get( Direction.Axis.Y ) + 0.5, -livingEntityEyePos.get( Direction.Axis.Z ) + 0.5 );
            //Rotate to head rotation
//            float yaw = livingEntity == player ? livingEntity.getViewYRot( partialTicks ) : livingEntity.getYHeadRot();
            stack.mulPose( Vector3f.YP.rotationDegrees( hpBar.getYRot() ) );
            stack.mulPose( Vector3f.XP.rotationDegrees( hpBar.getXRot() + (isPlayer ? playerPitch : 0) ) );
            float maxHp = livingEntity.getMaxHealth();
            float currHp = livingEntity.getHealth();
            float hpRatio = currHp / maxHp;
            float hpBarHpRatio = hpBar.getHpPos();
            polyDegRange = ( Math.max( 20, Math.min( 270, 60 * maxHp * 0.1f ) ) ) * scale;
            degOffset = 180 - polyDegRange/2 - 30;
            polyDegStep = polyDegRange / polyCount;

            mc.getTextureManager().bind( HP_BAR );

            float offset = livingEntity.getBbWidth() * 1.2f;
            w = (float) ( 2*offset*Math.tan( Math.toRadians( polyDegStep/2 ) ) );
            h = livingEntity.getBbHeight() * 0.1523f * scale;

            //Draw bar outside
            for( int i = 0; i < polyCount; i++ )
            {
                stack.pushPose();
                stack.mulPose( Vector3f.YP.rotationDegrees( (float) ( polyDegStep*i + degOffset ) ) );
                stack.translate( -w/2f, -h/2f - 0.1f, offset );
                mirrorBlitColor( stack, 0, w, 0, h, 0, polyWidth, 28, polyWidth*i, 0, 256, 256, 0x777777, 255 );
                stack.popPose();
            }

            mc.getTextureManager().bind( HP_BAR_INSIDE );

            float drawnRatio, nextDrawRatio, toDraw = 1;

            //Draw bar inside
            for( int i = 0; i < polyCount; i++ )
            {
                drawnRatio = i / (float) polyCount;
                nextDrawRatio = (i+1) / (float) polyCount;
                if( hpBarHpRatio < nextDrawRatio )
                    toDraw = (float) Util.map( hpBarHpRatio, drawnRatio, nextDrawRatio, 0, 1 );
                stack.pushPose();
                stack.mulPose( Vector3f.YP.rotationDegrees( (float) ( polyDegStep*i + degOffset ) ) );
                stack.translate( -w/2f, -h/2f - 0.1f, offset );
                mirrorBlitColor( stack, 0, w*toDraw, 0, h, 0, polyWidth*toDraw, 28, polyWidth*i, 0, 256, 256, Util.hueToRGB( (float) Util.map( hpRatio, 0, 1, 360, 240 ), 1, 1 ), 200 );
                stack.popPose();
                if( toDraw < 1 )
                    break;
            }

            //Draw bar inside guide
            for( int i = 0; i < polyCount; i++ )
            {
                drawnRatio = i / (float) polyCount;
                nextDrawRatio = (i+1) / (float) polyCount;
                if( hpBarHpRatio < nextDrawRatio )
                    toDraw = (float) Util.map( hpBarHpRatio, drawnRatio, nextDrawRatio, 0, 1 );
                stack.pushPose();
                stack.mulPose( Vector3f.YP.rotationDegrees( (float) ( polyDegStep*i + degOffset ) ) );
                stack.translate( -w/2f, -h/2f - 0.1f, offset );
                mirrorBlitColor( stack, 0, w*toDraw, 0, h, 0, polyWidth*toDraw, 28, polyWidth*i, 32, 256, 256, Util.hueToRGB( (float) Util.map( hpRatio, 0, 1, 360, 240 ), 1, 1 ), 200 );
                stack.popPose();
                if( toDraw < 1 )
                    break;
            }


            stack.popPose();
        }

        stack.popPose();
        RenderSystem.disableDepthTest();
        buffer.endBatch();
    }

    public static void mirrorBlit( MatrixStack matrixStack, float x1, float x2, float y1, float y2, float blitOffset, float uWidth, float vHeight, float uOffset, float vOffset, float textureWidth, float textureHeight )
    {
        mirrorBlit(matrixStack.last().pose(), x1, x2, y1, y2, blitOffset, (uOffset + 0.0F) / textureWidth, (uOffset + uWidth) / textureWidth, (vOffset + 0.0F) / textureHeight, (vOffset + vHeight) / textureHeight );
    }

    public static void mirrorBlit( Matrix4f matrix, float x1, float x2, float y1, float y2, float blitOffset, float minU, float maxU, float minV, float maxV)
    {
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();

        bufferbuilder.begin( 7, DefaultVertexFormats.POSITION_TEX );
        bufferbuilder.vertex( matrix, x1, y2, blitOffset ).uv( minU, maxV ).endVertex();
        bufferbuilder.vertex( matrix, x2, y2, blitOffset ).uv( maxU, maxV ).endVertex();
        bufferbuilder.vertex( matrix, x2, y1, blitOffset ).uv( maxU, minV ).endVertex();
        bufferbuilder.vertex( matrix, x1, y1, blitOffset ).uv( minU, minV ).endVertex();
        bufferbuilder.end();
        WorldVertexBufferUploader.end(bufferbuilder);

        bufferbuilder.begin( 7, DefaultVertexFormats.POSITION_TEX );
        bufferbuilder.vertex( matrix, x1, y1, blitOffset ).uv( minU, minV ).endVertex();
        bufferbuilder.vertex( matrix, x2, y1, blitOffset ).uv( maxU, minV ).endVertex();
        bufferbuilder.vertex( matrix, x2, y2, blitOffset ).uv( maxU, maxV ).endVertex();
        bufferbuilder.vertex( matrix, x1, y2, blitOffset ).uv( minU, maxV ).endVertex();
        bufferbuilder.end();

        RenderSystem.enableAlphaTest();
        WorldVertexBufferUploader.end( bufferbuilder );
    }

    public static void mirrorBlitColor( MatrixStack matrixStack, float x1, float x2, float y1, float y2, float blitOffset, float uWidth, float vHeight, float uOffset, float vOffset, float textureWidth, float textureHeight, int color, int alpha )
    {
        mirrorBlitColor(matrixStack.last().pose(), x1, x2, y1, y2, blitOffset, (uOffset + 0.0F) / textureWidth, (uOffset + uWidth) / textureWidth, (vOffset + 0.0F) / textureHeight, (vOffset + vHeight) / textureHeight, color, alpha );
    }

    public static void mirrorBlitColorHorizontalFlip( MatrixStack matrixStack, float x1, float x2, float y1, float y2, float blitOffset, float uWidth, float vHeight, float uOffset, float vOffset, float textureWidth, float textureHeight, int color, int alpha )
    {
        mirrorBlitColor(matrixStack.last().pose(), x1, x2, y1, y2, blitOffset, 1 - ( (uOffset + 0.0F) / textureWidth ), 1 - ( (uOffset + uWidth) / textureWidth ), (vOffset + 0.0F) / textureHeight, (vOffset + vHeight) / textureHeight, color, alpha );
    }

    public static void mirrorBlitColor( Matrix4f matrix, float x1, float x2, float y1, float y2, float blitOffset, float minU, float maxU, float minV, float maxV, int color, int alpha )
    {
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();

        int red = color >> 16;
        int green = ( color & 0x00ff00 ) >> 8;
        int blue = color & 0x0000ff;

        bufferbuilder.begin( 7, DefaultVertexFormats.POSITION_COLOR_TEX );
        bufferbuilder.vertex( matrix, x1, y2, blitOffset ).color( red, green, blue, alpha ).uv( minU, maxV ).endVertex();
        bufferbuilder.vertex( matrix, x2, y2, blitOffset ).color( red, green, blue, alpha ).uv( maxU, maxV ).endVertex();
        bufferbuilder.vertex( matrix, x2, y1, blitOffset ).color( red, green, blue, alpha ).uv( maxU, minV ).endVertex();
        bufferbuilder.vertex( matrix, x1, y1, blitOffset ).color( red, green, blue, alpha ).uv( minU, minV ).endVertex();
        bufferbuilder.end();
        WorldVertexBufferUploader.end(bufferbuilder);

        bufferbuilder.begin( 7, DefaultVertexFormats.POSITION_COLOR_TEX );
        bufferbuilder.vertex( matrix, x1, y1, blitOffset ).color( red, green, blue, alpha ).uv( minU, minV ).endVertex();
        bufferbuilder.vertex( matrix, x2, y1, blitOffset ).color( red, green, blue, alpha ).uv( maxU, minV ).endVertex();
        bufferbuilder.vertex( matrix, x2, y2, blitOffset ).color( red, green, blue, alpha ).uv( maxU, maxV ).endVertex();
        bufferbuilder.vertex( matrix, x1, y2, blitOffset ).color( red, green, blue, alpha ).uv( minU, maxV ).endVertex();
        bufferbuilder.end();

        RenderSystem.enableAlphaTest();
        WorldVertexBufferUploader.end( bufferbuilder );
    }
}
