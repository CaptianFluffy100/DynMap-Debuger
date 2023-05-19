package captianfluffy100.dynmap_debuger;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.Marker;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;

public class DynMapDebuger implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("dynmapdebuger");
	private MarkerAPI markerAPI;
    private String logHeader = "[DynMap Debuger]: ";

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
        onEnable();
	}

    public void onEnable() {
        DynmapCommonAPIListener.register(new DynmapCommonAPIListener() {
            public void apiEnabled(DynmapCommonAPI api) {
                // Get the MarkerAPI instance
                markerAPI = api.getMarkerAPI();
                LOGGER.info(logHeader + "(API)" + markerAPI);
            }
        });
        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            onChunkLoad(world, chunk);
        });
        ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
            onChunkUnload(world, chunk);
        });
		LOGGER.info(logHeader + "Hello Fabric world!");
    }

    // Listener for chunk load/unload events
    public void onChunkLoad(ServerWorld world, WorldChunk chunk) {
        if (markerAPI == null) {
            // initDynmap();
            LOGGER.info(logHeader + "Failed");
            return; // API not yet available
        }
    
        // Get the DimensionType and ChunkPos
        String dimension = world.getRegistryKey().getValue().toString();
        ChunkPos chunkPos = chunk.getPos();
    
        // Create a marker ID based on the dimension and chunk coordinates
        String markerId = dimension + "_" + chunkPos.x + "_" + chunkPos.z;
        LOGGER.info(logHeader + "CHUNK LOADED: " + markerId);
    
        // Check if the marker already exists
        MarkerSet set = markerAPI.getMarkerSet(markerId);
        if (set == null) {
            // Create a new MarkerSet
            set = markerAPI.createMarkerSet(markerId, "Chunk " + chunkPos.x + ", " + chunkPos.z, null, false);
        } else {
            // Update existing marker (in case of chunk reload)
            set.setMarkerSetLabel("Chunk " + chunkPos.x + ", " + chunkPos.z);
            set.setHideByDefault(false);
            set.setLayerPriority(0);
            set.setMinZoom(0);
            set.setMaxZoom(10);
        }
    
        // Create a green square area marker for the chunk
        double[] coordinates_1 = {(chunkPos.x + 1) * 16, 0, (chunkPos.z + 1) * 16};
        double[] coordinates_2 = {chunkPos.x * 16, 0, chunkPos.z * 16};
        AreaMarker marker = set.createAreaMarker(
            markerId,
            "Chunk " + chunkPos.x + ", " + chunkPos.z,
            true,
            world.getRegistryKey().getValue().toString(),
            coordinates_1,
            coordinates_2,
            true
        );
        marker.setFillStyle(0.5, 0x00FF00); // Green color with 50% transparency
        
        // Marker marker = set.createMarker(markerId, "<div>"+markerId+"</div>", true, "world", chunkPos.x * 16, (chunkPos.x + 1) * 16, chunkPos.z * 16, null, false);
    }

    public void onChunkUnload(ServerWorld world, WorldChunk chunk) {
        if (markerAPI == null) {
            // initDynmap();
            LOGGER.info(logHeader + "Failed");
            return; // API not yet available
        }
    
        // Get the DimensionType and ChunkPos
        String dimension = world.getRegistryKey().getValue().toString();
        ChunkPos chunkPos = chunk.getPos();
    
        // Create a marker ID based on the dimension and chunk coordinates
        String markerId = dimension + "_" + chunkPos.x + "_" + chunkPos.z;
        LOGGER.info("CHUNK UNLOADED: " + markerId);
    
        // Check if the marker exists and remove it
        MarkerSet markerSet = markerAPI.getMarkerSet(markerId);
        if (markerSet != null) {
            markerSet.deleteMarkerSet();
        }
    }
}
