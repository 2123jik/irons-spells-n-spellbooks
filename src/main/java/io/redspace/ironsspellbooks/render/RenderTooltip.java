package io.redspace.ironsspellbooks.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.awt.*;
import java.math.BigInteger;

public class RenderTooltip
{
    public static void register()
    {
        NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RenderGuiEvent.Post.class, RenderTooltip::onHudRender);
    }

    public static void onHudRender(RenderGuiEvent.Post event)
    {
        Minecraft mc = Minecraft.getInstance();
        double guiScale = mc.getWindow().getGuiScale();
        event.getGuiGraphics().pose().setIdentity();
        VertexConsumer vertexConsumer = event.getGuiGraphics().bufferSource().getBuffer(RenderType.LINE_STRIP);
        int y0 = mc.getWindow().getGuiScaledHeight() / 2;
        int x0 = mc.getWindow().getGuiScaledWidth() / 2;
        double x = 0;
        double y = 0;
        double scale = mc.player.getX();
        double factor;
        int bits = 16;
        int dimensions = 2;
        int length = bits * dimensions;
        long offset = 1 << bits / 2 - 1;
        long[] p;
        long px;
        long py;
        double upper;
        for (long i = 0; i < 1L << bits; )
        {
            p = transposedIndexToPoint(bits, transpose(BigInteger.valueOf(i), dimensions, bits, length));
            px = ((p[0] - offset) );
            py = ((p[1] - offset) );
            if (scale > 0)
            {
                factor = (i % (scale))/guiScale;
                upper = 2.5;
                if (x0 + factor * px / guiScale < upper * x0 && y0 + factor * py / guiScale < upper * y0)
                {
                    x = x0 + factor * px;
                    y = y0 + factor * py;
                }
                vertexConsumer.addVertex((float) x, (float) y, 1).
                        setColor(Color.HSBtoRGB(((float) i /offset) % 1.0f, 0.4f, 0.6f)).setNormal(1, 1, 0);
            }
            i = i + 1;
        }
    }

    static long[] transposedIndexToPoint(int bits, long... x)
    {
        final long N = 2L << (bits - 1);
        // Note that x is mutated by this method (as a performance improvement
        // to avoid allocation)
        int n = x.length; // number of dimensions
        long p, q, t;
        int i;
        // Gray decode by H ^ (H/2)
        t = x[n - 1] >> 1;
        // Corrected error in Skilling's paper on the following line. The
        // appendix had i >= 0 leading to negative array index.
        for (i = n - 1; i > 0; i--)
            x[i] ^= x[i - 1];
        x[0] ^= t;
        // Undo excess work
        for (q = 2; q != N; q <<= 1)
        {
            p = q - 1;
            for (i = n - 1; i >= 0; i--)
                if ((x[i] & q) != 0L)
                {
                    x[0] ^= p; // invert
                }
                else
                {
                    t = (x[0] ^ x[i]) & p;
                    x[0] ^= t;
                    x[i] ^= t;
                }
        } // exchange
        return x;
    }

    static long[] transpose(BigInteger index, int dimension, int bits, int length)
    {
        long[] x = new long[dimension];
        byte[] b = index.toByteArray();
        for (int idx = 0; idx < 8 * b.length; idx++)
        {
            if ((b[b.length - 1 - idx / 8] & (1L << (idx % 8))) != 0)
            {
                int dim = (length - idx - 1) % dimension;
                int shift = (idx / dimension) % bits;
                x[dim] |= 1L << shift;
            }
        }
        return x;
    }

    public static void main(String[] args)
    {
        System.out.println(14 % 4 - 4 >> 1);
    }

}
