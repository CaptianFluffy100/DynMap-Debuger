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
import org.dynmap.markers.CircleMarker;

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
	MarkerAPI markerAPI;
    private String logHeader = "[DynMap Debuger]: ";
    private DynmapCommonAPI dynampAPI;
    Integer count = 0;
    MarkerSet set;

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
                dynampAPI = api;
                // Get the MarkerAPI instance
                markerAPI = api.getMarkerAPI();
                set = markerAPI.getMarkerSet("Loaded Chunks");
                if (set == null) {
                    set = markerAPI.createMarkerSet("Loaded Chunks", "Loaded Chunks", null, true);
                }
                LOGGER.info(logHeader + "(API) " + markerAPI);
                String version = api.getDynmapCoreVersion();
                LOGGER.info(logHeader + "(API) Version: " + version);
            }
            public void apiDisabled(DynmapCommonAPI api) {
                LOGGER.info(logHeader + "(API) Dynmap closed or crashed");
            }
            public void apiListenerAdded() {
                LOGGER.info(logHeader + "API listener added");
                // Perform additional initialization or configuration tasks here
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
        if (!dynampAPI.markerAPIInitialized()) {
            LOGGER.info(logHeader + "Marker API not Initialized");
            return;
        }
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
        // LOGGER.info(logHeader + "CHUNK LOADED: " + markerId + " || " + count);
        // Check if the marker already exists in the marker set
        CircleMarker existingMarker = set.findCircleMarker(markerId);
        if (existingMarker != null) {
            // Marker already exists, no need to create a new one
            LOGGER.info(logHeader + "AREA MARKER ALL READY CREATED: " + markerId);
            existingMarker.setFillStyle(0.5, 0x00FF00); // Green color with 50% transparency
            existingMarker.setLineStyle(1, 1.0, 0x00FF00);
            return;
        }
        ++count;
        // Create a green square area marker for the chunk
        CircleMarker marker = set.createCircleMarker(
            markerId,
            "Chunk " + chunkPos.x + ", " + chunkPos.z,
            true,
            "world",
            (chunkPos.x * 16) + 8,
            0,
            (chunkPos.z * 16) + 8,
            8,
            8,
            false
        );
        // LOGGER.info(logHeader + "MARKER: " + world.getRegistryKey().getValue().toString());
        marker.setFillStyle(0.5, 0x00FF00); // Green color with 50% transparency
        marker.setLineStyle(1, 1.0, 0x00FF00);
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
        // LOGGER.info(logHeader + "CHUNK UNLOADED: " + markerId + " || " + count);
    
        // Check if the marker exists and remove it
        CircleMarker marker = set.findCircleMarker(markerId);
        if (marker != null) {
            // marker.setLineStyle(1, 0.0, 0x00FF00);
            // marker.setFillStyle(0.0, 0x00FF00);
            marker.deleteMarker();
        }
        --count;
    }
}
