package me.aroze.simplectf.team;

import me.aroze.simplectf.util.ItemUtil;
import me.aroze.simplectf.util.text.CtfMiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

/**
 * Represents a set of constant, color matched kit items for a team
 */
public class TeamKit {

    private static final ItemStack[] SHARED_ITEMS = new ItemStack[] {
        ItemUtil.setBasicFlags(new ItemStack(Material.STONE_SWORD)),
        ItemUtil.setBasicFlags(new ItemStack(Material.BOW)),
        ItemUtil.setBasicFlags(new ItemStack(Material.ARROW, 32))
    };

    private final TeamColor teamColor;

    private final ItemStack flagInventoryItem;
    private final ItemStack chestplate;
    private final ItemStack leggings;
    private final ItemStack boots;

    /**
     * Constructs a new TeamKit for the specified team color, initializing the kit items with appropriate colors and
     * metadata.
     *
     * @param teamColor the {@link TeamColor} of this team
     */
    public TeamKit(TeamColor teamColor) {
        this.teamColor = teamColor;

        this.flagInventoryItem = this.createFlagItem();
        this.chestplate = this.createDyedLeather(Material.LEATHER_CHESTPLATE);
        this.leggings = this.createDyedLeather(Material.LEATHER_LEGGINGS);
        this.boots = this.createDyedLeather(Material.LEATHER_BOOTS);
    }

    /**
     * Applies this kit to a player, clearing their existing inventory, adding the kit items and equipping to armor
     * slots where applicable.
     *
     * @param player the {@link Player} to apply the kit to
     */
    public void applyKit(final Player player) {
        player.getInventory().clear();

        for (final ItemStack sharedItem : SHARED_ITEMS) {
            player.getInventory().addItem(sharedItem);
        }

        player.getInventory().setArmorContents(new ItemStack[] {this.boots, this.leggings, this.chestplate});
    }

    /**
     * Retrieves the inventory flag item for this team
     *
     * @return a copy of the team's inventory flag item, safe to modify if needed
     */
    public ItemStack retrieveFlagItem() {
        return this.flagInventoryItem.clone();
    }

    private ItemStack createFlagItem() {
        final ItemStack item = new ItemStack(this.teamColor.flagType());

        item.editMeta(meta -> meta.displayName(CtfMiniMessage.getInstance().deserialize("<i:false><color><team>'s Flag</color>",
            Placeholder.styling("color", this.teamColor.color()),
            Placeholder.component("team", this.teamColor.formattedDisplayName())
        )));

        return item;
    }

    private ItemStack createDyedLeather(final Material material) {
        final ItemStack item = new ItemStack(material);

        item.editMeta(meta -> {
            if (meta instanceof LeatherArmorMeta leatherMeta) {
                leatherMeta.setColor(this.teamColor.bukkitColor());
            }
        });

        return ItemUtil.setBasicFlags(item);
    }

}
