package net.tangentmc.portalstick.renderer;

import org.bukkit.map.MapPalette;

import java.awt.image.BufferedImage;

public class ImageData {
    byte[] bytes;
    int width;
    int height;

    public ImageData(BufferedImage read) {
        this.bytes = MapPalette.imageToBytes(read);
        this.width = read.getWidth();
        this.height = read.getHeight();
    }
}
