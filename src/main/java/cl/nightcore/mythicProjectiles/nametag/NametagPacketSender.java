package cl.nightcore.mythicProjectiles.nametag;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NametagPacketSender {
    private final ProtocolManager protocolManager;
    private final JavaPlugin plugin;

    public NametagPacketSender(JavaPlugin plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    /**
     * Sends a custom nametag packet using Adventure Components
     *
     * @param viewer        The player who will see the nametag
     * @param entity        The entity whose nametag is being modified
     * @param nameComponent The custom name component
     * @param alwaysVisible Whether the nametag should be visible always
     */
    public void sendCustomNametag(Player viewer, Entity entity, Component nameComponent, boolean alwaysVisible) {
        try {
            // Create a packet to update entity metadata
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);

            // Set the entity ID
            packet.getIntegers().write(0, entity.getEntityId());

            // Prepare the DataWatcher (metadata)
            WrappedDataWatcher dataWatcher = WrappedDataWatcher.getEntityWatcher(entity);

            // Create the custom name metadata entry
            WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.getChatComponentSerializer(true);
            WrappedDataWatcher.WrappedDataWatcherObject nameObject =
                    new WrappedDataWatcher.WrappedDataWatcherObject(2, serializer);

            // Optional wrapper for the chat component
            Optional<Object> optionalName = Optional.of(
                    WrappedChatComponent.fromJson(
                            GsonComponentSerializer.gson().serialize(nameComponent)
                    ).getHandle()
            );

            // Create metadata entries
            List<WrappedDataValue> dataValues = new ArrayList<>();

            // Custom Name
            dataValues.add(new WrappedDataValue(2, serializer, optionalName));

            // Nametag visibility
            WrappedDataWatcher.Serializer booleanSerializer = WrappedDataWatcher.Registry.get(Boolean.class);
            dataValues.add(new WrappedDataValue(3, booleanSerializer, alwaysVisible));

            // Write the metadata to the packet
            packet.getDataValueCollectionModifier().write(0, dataValues);

            // Send the packet to the specific viewer
            protocolManager.sendServerPacket(viewer, packet);

        } catch (Exception e) {
            plugin.getLogger().severe("Error sending nametag packet: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Sends a nametag with translation key and optional components
     *
     * @param viewer       The player who will see the nametag
     * @param entity       The entity whose nametag is being modified
     * @param prefix       Optional prefix component
     * @param suffix       Optional suffix component
     * @param placeholders Components to use as placeholders
     */
    public void sendNametagWithTranslationKey(Player viewer, Entity entity, Component prefix, Component suffix, Component... placeholders) {
        Component entityNameComponent = entity.customName();

        if (entityNameComponent == null) {
            // Create translatable component for entity type
            entityNameComponent = Component.translatable(
                    entity.getType().translationKey(),
                    placeholders
            );
        }
        // Compose final nametag component
        Component nametagComponent = Component.empty();

        if (prefix != null) {
            nametagComponent = nametagComponent.append(prefix);
        }

        nametagComponent = nametagComponent
                .append(entityNameComponent);

        if (suffix != null) {
            nametagComponent = nametagComponent.append(suffix);
        }
        // Send the custom nametag
        sendCustomNametag(viewer, entity, nametagComponent, true);
    }
}