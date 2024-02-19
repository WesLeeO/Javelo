package ch.epfl.javelo.gui;

import ch.epfl.javelo.LRUCache;
import ch.epfl.javelo.Preconditions;
import javafx.scene.image.Image;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;

/**
 * Tile Manager class
 * This class manages the access to the tiles constituting parts of Switzerland
 *
 * @author Wesley Nana Davies (344592)
 * @author David Farah (341017)
 */
public final class TileManager {

    private static final int CACHE_SIZE = 100;
    private static final float LOAD_FACTOR = .75f;
    private final Path cachePath;
    private final String hostName;
    private final LinkedHashMap<TileId, Image> memoryCache;

    /**
     * Constructor
     *
     * @param cachePath the folder containing cache
     * @param hostName  the hostname of the server to get tiles from
     */
    public TileManager(Path cachePath, String hostName) {
        this.cachePath = cachePath;
        this.hostName = hostName;
        memoryCache = new LRUCache<>(CACHE_SIZE, LOAD_FACTOR);
    }

    /**
     * Returns the image corresponding to a certain TileId either from the web, from a memory cache or from a storage cache
     *
     * @param tile id of the tile
     * @return The image corresponding to the tile
     * @throws IOException in case of an input/output error
     */
    public Image getTileImage(TileId tile) throws IOException {
        String fileName = tile.getFileNameLocal();
        Path filePath = cachePath.resolve(fileName).normalize();

        if (memoryCache.containsKey(tile)) {
            return memoryCache.get(tile);

        } else if (Files.exists(filePath)) {
            return getImageFromFile(filePath, tile);

        } else {
            URL u = tile.getURL(hostName);
            URLConnection c = u.openConnection();
            c.setRequestProperty("User-Agent", "JaVelo");
            Files.createDirectories(filePath.getParent());

            try (InputStream i = c.getInputStream();
                 FileOutputStream o = new FileOutputStream(filePath.toFile())) {
                i.transferTo(o);
            }

            return getImageFromFile(filePath, tile);
        }
    }

    /**
     * Returns the contents of an image file, and adds the image to the memory cache
     *
     * @param filePath Full path of the file
     * @param tile     id of the tile in question
     * @return Image
     * @throws IOException in case of input/output errors
     */
    private Image getImageFromFile(Path filePath, TileId tile) throws IOException {
        try (InputStream i = new FileInputStream(filePath.toFile())) {
            Image tileImage = new Image(i);
            memoryCache.put(tile, tileImage);
            return tileImage;
        }
    }


    /**
     * Inner class representing the identity of a Tile
     */
    record TileId(int zoomLevel, int xIndex, int yIndex) {
        private static final int ZOOM_LEVEL_MIN = 0;
        private static final int ZOOM_LEVEL_MAX = 19;

        /**
         * Constructor, checks if the arguments are valid
         *
         * @param zoomLevel Current zoom level
         * @param xIndex    x index of the tile
         * @param yIndex    y index of the tile
         */
        TileId {
            Preconditions.checkArgument(isValid(zoomLevel, xIndex, yIndex));
        }

        /**
         * Returns true iff the arguments given are valid
         *
         * @param zoomLevel Current zoom level
         * @param xIndex    x index of the tile
         * @param yIndex    y index of the tile
         * @return boolean representing validity
         */
        public static boolean isValid(int zoomLevel, int xIndex, int yIndex) {
            int maxIndex = (int) Math.pow(2, zoomLevel) - 1;
            return (zoomLevel >= ZOOM_LEVEL_MIN && zoomLevel <= ZOOM_LEVEL_MAX &&
                    xIndex >= 0 && xIndex <= maxIndex &&
                    yIndex >= 0 && yIndex <= maxIndex);
        }

        /**
         * Returns the exact location of a file in URL format (without the host name)
         *
         * @return the formatted string
         */
        private String getFileNameURL() {
            return "/%d/%d/%d.png".formatted(zoomLevel, xIndex, yIndex);
        }

        /**
         * Returns the exact location of a file in the user machine format (without the base path)
         *
         * @return the formatted string
         */
        private String getFileNameLocal() {
            return "%d%s%d%s%d.png".formatted(zoomLevel, File.separator, xIndex, File.separator, yIndex);
        }

        /**
         * Returns the URL object corresponding to a host name
         *
         * @param hostName the webserver host name
         * @return new URL object
         * @throws MalformedURLException if the argument is incorrect
         */
        private URL getURL(String hostName) throws MalformedURLException {
            return new URL("https", hostName, getFileNameURL());
        }
    }
}
