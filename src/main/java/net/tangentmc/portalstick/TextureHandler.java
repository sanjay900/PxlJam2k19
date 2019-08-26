package net.tangentmc.portalstick;

import net.tangentmc.portalstick.renderer.ImageData;
import org.bukkit.map.MapPalette;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

public class TextureHandler {
    public static final TextureHandler textureHandler = new TextureHandler();
    private HashMap<String, ImageData> images = new HashMap<>();
    private TextureHandler() {
        try {
            Enumeration<URL> enumeration = Main.class.getClassLoader().getResources("assets/minecraft/textures/block");
            while (enumeration.hasMoreElements()) {
                URI uri = enumeration.nextElement().toURI();
                try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                    Files.list(fileSystem.getPath("assets/minecraft/textures/block")).forEach(path -> {
                        try {
                            String name = path.getName(path.getNameCount() - 1).toString();
                            if (name.endsWith(".png")) {
                                images.put("minecraft:" + name.substring(0, name.indexOf(".png")), new ImageData(ImageIO.read(path.toUri().toURL())));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public ImageData getImage(String minecraftId) {
        return images.get(minecraftId);
    }
}
