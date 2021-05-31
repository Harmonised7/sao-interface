package harmonised.saoui.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.saoui.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.*;

public class Renderer
{
    public static final Map<LivingEntity, HPBar> hpBars = new HashMap<>();
    private static final Minecraft mc = Minecraft.getInstance();

    private static int blitOffset = 0;

    //HP Bar
    private static final int polyCount = 64;
    private static final int fullBarWidth = 1024;
    private static final int barHeight = 160;
    private static final int barElements = 4;
    private static final float polyWidth = fullBarWidth/(float)polyCount;
    private static final int renderDistance = 128;
    private static final int outsideWallWidth = 2;
    private static final float playerPitch = -25;

    //Indicator
    private static int indicatorWidth = 128;
    private static int indicatorHeight = 256;
    public static Set<Integer> attackers = new HashSet<>();

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
        IRenderTypeBuffer.Impl buffer = mc.renderBuffers().bufferSource();
        RenderSystem.enableBlend();
        //Align to world
        stack.translate( -cameraCenter.get( Direction.Axis.X ) + 0.5, -cameraCenter.get( Direction.Axis.Y ) + 0.5, -cameraCenter.get( Direction.Axis.Z ) + 0.5 );
        stack.mulPose(Vector3f.XP.rotationDegrees(180.0F));

        for( LivingEntity livingEntity : world.getEntitiesOfClass( LivingEntity.class, new AxisAlignedBB( originBlockPos.above( renderDistance ).north( renderDistance ).east( renderDistance ), originBlockPos.below( renderDistance ).south( renderDistance ).west( renderDistance ) ) ) )
        {
            stack.pushPose();
            Vector3d livingEntityEyePos = livingEntity.getEyePosition( partialTicks );
            stack.translate( livingEntityEyePos.get( Direction.Axis.X ) - 0.5, -livingEntityEyePos.get( Direction.Axis.Y ) + 0.5, -livingEntityEyePos.get( Direction.Axis.Z ) + 0.5 );

            drawHpBar( stack, livingEntity, partialTicks );
            drawNPCIndicator( stack, livingEntity );

            stack.popPose();
        }

        stack.popPose();
        RenderSystem.disableDepthTest();
        buffer.endBatch();
    }

    public static void drawHpBar( MatrixStack stack, LivingEntity livingEntity, float partialTicks )
    {
        PlayerEntity player = mc.player;
        float w, h;

        if( !hpBars.containsKey( livingEntity ) || hpBars.get( livingEntity ).getLivingEntity() != livingEntity )
            hpBars.put( livingEntity, new HPBar( livingEntity ) );
        HPBar hpBar = hpBars.get( livingEntity );
        if( !mc.isPaused() )
            hpBar.update( partialTicks );

        boolean isPlayer = livingEntity == player;
//            if( isPlayer )
//                return;

        float scale;
        if( isPlayer )
        {
//            RenderSystem.disableDepthTest();
            scale = 0.3f;
        }
        else
        {
            scale = 1;
        }

        stack.mulPose( Vector3f.YP.rotationDegrees( hpBar.getYRot() ) );    //Vertical Rotation
        stack.mulPose( Vector3f.XP.rotationDegrees( hpBar.getXRot() + (isPlayer ? playerPitch : 0) ) ); //Horizontal Rotation
        float maxHp = livingEntity.getMaxHealth();
        float currHp = livingEntity.getHealth();
        float hpRatio = currHp / maxHp;
        float hpBarHpRatio = hpBar.getHpPos();

        float polyDegRange = ( Math.max( 20, Math.min( 270, 60 * maxHp * 0.1f ) ) ) * scale;
        float degOffset = 180 - polyDegRange/2 - 30;
        float polyDegStep = polyDegRange / polyCount;

        float livingEntityWidth = livingEntity.getBbWidth();
        float offset = livingEntityWidth * 1.2f;
        w = (float) ( 2*offset*Math.tan( Math.toRadians( polyDegStep/2 ) ) );
        h = livingEntity.getBbHeight() * 0.1f * scale;

        ITextComponent livingEntityNameComp = livingEntity.getName();
        String livingEntityName = livingEntityNameComp.getString();


        //Name
        if( isPlayer )
        {
            {
                float nameTextScale = 100 / livingEntityWidth / ( isPlayer ? 0.7f : 3 );
                Vector3f nameTextPos = new Vector3f( -w/2f, -h/2f - 0.1f, offset+0.02f );
//                drawCurvedText( stack, livingEntityName, textPos, textScale, offset, degOffset, isPlayer );
                drawCurvedText( stack, livingEntityName, nameTextPos, nameTextScale, degOffset + livingEntityWidth, isPlayer );

                float xpTextScale = 100 / livingEntityWidth / 0.3f;
                Vector3f levelTextPos = new Vector3f( -w/2f, -h/2f - 0.04f, offset+0.02f );

                drawCurvedText( stack, (int) ( player.getXpNeededForNextLevel() * player.experienceProgress ) + "/" + player.getXpNeededForNextLevel() + " LV: " + player.experienceLevel, levelTextPos, xpTextScale, degOffset + 17, isPlayer );
            }
            {
                stack.pushPose();

                stack.popPose();
            }

            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
        }

        mc.getTextureManager().bind( Icons.HP_BAR );

        //Draw bar outside
        for( int i = 0; i < polyCount; i++ )
        {
            stack.pushPose();
            stack.mulPose( Vector3f.YP.rotationDegrees( polyDegStep*i + degOffset ) );
            stack.translate( -w/2f, -h/2f - 0.1f, offset );
            mirrorBlitColor( stack, 0, w, 0, h, 0, polyWidth, barHeight, polyWidth*i, isPlayer ? barHeight : 0, fullBarWidth, barHeight*barElements, 0x777777, 255 );
            stack.popPose();
        }

        float drawnRatio, nextDrawRatio, minPolyU = 0, maxPolyU = 1;

        boolean hpLoss = hpRatio < hpBarHpRatio;

        float tempHpBarHpRatio = hpLoss ? hpRatio : hpBarHpRatio;

        //Draw bar inside
        for( int i = 0; i < polyCount; i++ )
        {
            drawnRatio = i / (float) polyCount;
            nextDrawRatio = (i+1) / (float) polyCount;
            if( tempHpBarHpRatio < nextDrawRatio )
                maxPolyU = (float) Util.map( tempHpBarHpRatio, drawnRatio, nextDrawRatio, 0, 1 );
            stack.pushPose();
            stack.mulPose( Vector3f.YP.rotationDegrees( polyDegStep*i + degOffset ) );
            stack.translate( -w/2f, -h/2f - 0.1f, offset );
            mirrorBlitColor( stack, w*minPolyU, w*maxPolyU, 0, h, 0, polyWidth*maxPolyU - polyWidth*minPolyU, barHeight, polyWidth*i + polyWidth*minPolyU, barHeight*2, fullBarWidth, barHeight*barElements, Util.hueToRGB( (float) Util.map( hpBarHpRatio, 0, 1, 360, 240 ), 1, 1 ), 200 );
            stack.popPose();
            if( maxPolyU < 1 )
                break;
        }

        minPolyU = maxPolyU;
        maxPolyU = 1;

        //Draw Gain/Loss Indicator
        if( hpBarHpRatio < hpRatio )
        {
            int firstIndicatorPoly = (int) ( polyCount*hpBarHpRatio );
            int lastIndicatorPoly = (int) ( polyCount*hpRatio )+1;
            //Gain
            for( int i = firstIndicatorPoly; i < lastIndicatorPoly; i++ )
            {
                drawnRatio = i / (float) polyCount;
                nextDrawRatio = (i+1) / (float) polyCount;

                if( i+1 == lastIndicatorPoly )
                    maxPolyU = (float) Util.map( hpRatio, drawnRatio, nextDrawRatio, 0, 1 );

                stack.pushPose();
                stack.mulPose( Vector3f.YP.rotationDegrees( polyDegStep*i + degOffset ) );
                stack.translate( -w/2f, -h/2f - 0.1f, offset );
                mirrorBlitColor( stack, w*minPolyU, w*maxPolyU, 0, h, 0, polyWidth*maxPolyU - polyWidth*minPolyU, barHeight, polyWidth*i + polyWidth*minPolyU, barHeight*2, fullBarWidth, barHeight*barElements, 0xffffff, 200 );
                minPolyU = 0;
                stack.popPose();
            }
        }
        else if( hpRatio < hpBarHpRatio )
        {
            int firstIndicatorPoly = (int) ( polyCount*hpRatio );
            int lastIndicatorPoly = (int) ( polyCount*hpBarHpRatio )+1;
            //Loss
            for( int i = firstIndicatorPoly; i < lastIndicatorPoly; i++ )
            {
                drawnRatio = i / (float) polyCount;
                nextDrawRatio = (i+1) / (float) polyCount;

                if( i+1 == lastIndicatorPoly )
                    maxPolyU = (float) Util.map( hpBarHpRatio, drawnRatio, nextDrawRatio, 0, 1 );

                stack.pushPose();
                stack.mulPose( Vector3f.YP.rotationDegrees( polyDegStep*i + degOffset ) );
                stack.translate( -w/2f, -h/2f - 0.1f, offset );
                mirrorBlitColor( stack, w*minPolyU, w*maxPolyU, 0, h, 0, polyWidth*maxPolyU - polyWidth*minPolyU, barHeight, polyWidth*i + polyWidth*minPolyU, barHeight*2, fullBarWidth, barHeight*barElements, 0xff0000, 200 );
                minPolyU = 0;
                stack.popPose();
            }
        }

        //Hunger
        if( isPlayer )
        {
            float hungerRatio = hpBar.getHungerPos();
            float saturationRatio = hpBar.getSaturationPos();

            Item mainitem = player.getMainHandItem().getItem();
            Food food = mainitem.getFoodProperties();
            if( food != null )
            {
                hungerRatio = (float) Math.min( 0.5, hungerRatio + food.getNutrition() / 40f );
                saturationRatio = (float) Math.min( 0.5, saturationRatio + food.getSaturationModifier() / 4f );
            }
            else
                saturationRatio = Math.min( hungerRatio, saturationRatio );

            //Draw Warning
            if( hungerRatio <= 0.1 && ( System.currentTimeMillis() / 1000 ) % 2 == 0 )
            {
                for( int i = 0; i < polyCount; i++ )
                {
                    stack.pushPose();
                    stack.mulPose( Vector3f.YP.rotationDegrees( polyDegStep*i + degOffset ) );
                    stack.translate( -w/2f, -h/2f - 0.1f, offset );
                    mirrorBlitColor( stack, 0, w, 0, h, 0, polyWidth, barHeight, polyWidth*i, barHeight*3, fullBarWidth, barHeight*barElements, 0xff0000, 255 );
                    stack.popPose();
                }
            }

            minPolyU = 0;
            maxPolyU = 1;

            //Draw Saturation
            for( int i = 0; i < polyCount; i++ )
            {
                drawnRatio = i / (float) polyCount;
                nextDrawRatio = (i+1) / (float) polyCount;
                if( saturationRatio < nextDrawRatio )
                    maxPolyU = (float) Util.map( saturationRatio, drawnRatio, nextDrawRatio, 0, 1 );
                stack.pushPose();
                stack.mulPose( Vector3f.YP.rotationDegrees( polyDegStep*i + degOffset ) );
                stack.translate( -w/2f, -h/2f - 0.1f, offset );
                mirrorBlitColor( stack, w*minPolyU, w*maxPolyU, 0, h, 0, polyWidth*maxPolyU - polyWidth*minPolyU, barHeight, polyWidth*i + polyWidth*minPolyU, barHeight*3, fullBarWidth, barHeight*barElements, 0xffff00, 200 );
                stack.popPose();
                if( maxPolyU < 1 )
                    break;
            }

            minPolyU = maxPolyU;
            maxPolyU = 1;

            //Draw Hunger

            int firstIndicatorPoly = (int) ( polyCount*saturationRatio );
            int lastIndicatorPoly = (int) ( polyCount*hungerRatio )+1;
            for( int i = firstIndicatorPoly; i < lastIndicatorPoly; i++ )
            {
                drawnRatio = i / (float) polyCount;
                nextDrawRatio = (i+1) / (float) polyCount;

                if( i+1 == lastIndicatorPoly )
                    maxPolyU = (float) Util.map( hungerRatio, drawnRatio, nextDrawRatio, 0, 1 );

                stack.pushPose();
                stack.mulPose( Vector3f.YP.rotationDegrees( polyDegStep*i + degOffset ) );
                stack.translate( -w/2f, -h/2f - 0.1f, offset );
                mirrorBlitColor( stack, w*minPolyU, w*maxPolyU, 0, h, 0, polyWidth*maxPolyU - polyWidth*minPolyU, barHeight, polyWidth*i + polyWidth*minPolyU, barHeight*3, fullBarWidth, barHeight*barElements, Util.hueToRGB( (float) Util.map( hungerRatio, 0, 0.5, 360, 320 ), 1f, 1f ), 200 );
                minPolyU = 0;
                stack.popPose();
            }
        }
    }

    public static void drawNPCIndicator( MatrixStack stack, LivingEntity livingEntity )
    {
        mc.getTextureManager().bind( Icons.INDICATOR );
        stack.pushPose();
        int color = 0xfffff;
        if( livingEntity instanceof AnimalEntity )
            color = 0x00ff00;
        else if( livingEntity instanceof MobEntity )
        {
            if( attackers.contains( livingEntity.getId() ) )
                color = 0xff0000;
            else
                color = 0xffaa00;
        }
        else if( livingEntity instanceof PlayerEntity )
            color = 0x0000ff;
        float scale = livingEntity.getScale()*0.1523f;
        float height = scale*2;
        stack.translate( 0, -livingEntity.getBbHeight()*0.3251f - height, 0 );
        mirrorBlitColor( stack, -scale, scale, -height, height, 0, indicatorWidth, indicatorHeight, 0, 0, indicatorWidth, indicatorHeight, color, 255 );
        stack.mulPose( Vector3f.YP.rotationDegrees( 90 ) );
        mirrorBlitColor( stack, -scale, scale, -height, height, 0, indicatorWidth, indicatorHeight, 0, 0, indicatorWidth, indicatorHeight, color, 255 );
        stack.popPose();
    }

    public static void drawCurvedText( MatrixStack stack, String text, Vector3f pos, float scale, float degOffset, boolean isPlayer )
    {
        stack.pushPose();

        stack.scale( 1/scale, 1/scale, 1/scale );
        float x = pos.x()*scale, y = pos.y()*scale, z = pos.z()*scale;
        stack.translate( x, y, 0 );
        float compound = 0;
        for( int i = 0; i < text.length(); i++ )
        {
            char c = text.charAt(i);
            stack.pushPose();
            stack.mulPose( Vector3f.YP.rotationDegrees( 3 + degOffset + compound*(float) Math.atan( 1/z )*60 ) );
            stack.translate( 0, 0, z );
            drawString( stack, mc.font, new StringTextComponent( "" + c ), 0, isPlayer ? -5 : -7, 0xffffff );
            compound += mc.font.width( "" + c );
            stack.popPose();
        }
        stack.popPose();
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

    public static void drawCenteredString( MatrixStack stack, FontRenderer font, ITextComponent msg, float x, float y, int color )
    {
        IReorderingProcessor ireorderingprocessor = msg.getVisualOrderText();
        font.drawShadow(stack, ireorderingprocessor, x - font.width(ireorderingprocessor) / 2f, y, color );
    }

    public static void drawString( MatrixStack stack, FontRenderer font, String msg, float x, float y, int color )
    {
        font.drawShadow(stack, msg, x, y, color);
    }

    public static void drawString( MatrixStack stack, FontRenderer font, ITextComponent msg, float x, float y, int color )
    {
        font.drawShadow(stack, msg, x, y, color);
    }

    public static void renderTooltip( MatrixStack stack, ItemStack itemStack, int x, int y )
    {
        net.minecraftforge.fml.client.gui.GuiUtils.preItemToolTip(itemStack);
        renderWrappedToolTip( stack, itemStack.getTooltipLines( mc.player, mc.options.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL ), x, y );
        net.minecraftforge.fml.client.gui.GuiUtils.postItemToolTip();
    }

    public static void renderWrappedToolTip( MatrixStack matrixStack, List<? extends ITextProperties> tooltips, int mouseX, int mouseY )
    {
        GuiUtils.drawHoveringText(matrixStack, tooltips, mouseX, mouseY, getScaledWidth(), getScaledHeight(), -1, mc.font );
    }

    public static int getScaledWidth()
    {
        return mc.getWindow().getGuiScaledWidth();
    }

    public static int getScaledHeight()
    {
        return mc.getWindow().getGuiScaledHeight();
    }

    public static void renderGuiItem( ItemStack itemStack, float x, float y )
    {
        Minecraft mc = Minecraft.getInstance();
        TextureManager textureManager = mc.getTextureManager();
        ItemRenderer itemRenderer = mc.getItemRenderer();
        IBakedModel bakedModel = itemRenderer.getModel( itemStack, null, null );
        RenderSystem.pushMatrix();
        textureManager.bind(AtlasTexture.LOCATION_BLOCKS);
        textureManager.getTexture(AtlasTexture.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.translatef( x, y, 100.0F + itemRenderer.blitOffset );
        RenderSystem.translatef(8.0F, 8.0F, 0.0F);
        RenderSystem.scalef(1.0F, -1.0F, 1.0F);
        RenderSystem.scalef(16.0F, 16.0F, 16.0F);
        MatrixStack matrixstack = new MatrixStack();
        IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean flag = !bakedModel.usesBlockLight();
        if (flag) {
            RenderHelper.setupForFlatItems();
        }

        itemRenderer.render( itemStack, ItemCameraTransforms.TransformType.GUI, false, matrixstack, irendertypebuffer$impl, 15728880, OverlayTexture.NO_OVERLAY, bakedModel );
        irendertypebuffer$impl.endBatch();
        RenderSystem.enableDepthTest();
        if (flag) {
            RenderHelper.setupFor3DItems();
        }

        RenderSystem.disableAlphaTest();
        RenderSystem.disableRescaleNormal();
        RenderSystem.popMatrix();
    }
}