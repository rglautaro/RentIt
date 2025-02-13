package me.truemb.rentit.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import me.truemb.rentit.enums.RentTypes;
import me.truemb.rentit.handler.CategoryHandler;
import me.truemb.rentit.handler.RentTypeHandler;
import me.truemb.rentit.main.Main;
import net.wesjd.anvilgui.AnvilGUI;
import net.wesjd.anvilgui.AnvilGUI.Builder;

public class AdminShopListener implements Listener {

	private Main instance;

	public AdminShopListener(Main plugin) {
		this.instance = plugin;
		this.instance.getServer().getPluginManager().registerEvents(this, this.instance);
	}

	@EventHandler
	public void onEditClick(InventoryClickEvent e) {

		Player p = (Player) e.getWhoClicked();

		if (e.getClickedInventory() == null)
			return;

		if (!e.getView().getTitle().equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', this.instance.manageFile().getString("GUI.shopAdmin.displayName"))))
			return;

		e.setCancelled(true);

		if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
			return;

		if (!p.hasPermission(this.instance.manageFile().getString("Permissions.shop")))
			return;

		ItemStack item = e.getCurrentItem();
		ItemMeta meta = item.getItemMeta();

		if (!meta.getPersistentDataContainer().has(this.instance.guiItem, PersistentDataType.STRING))
			return;

		if (!meta.getPersistentDataContainer().has(this.instance.idKey, PersistentDataType.INTEGER))
			return;

		int shopId = meta.getPersistentDataContainer().get(this.instance.idKey, PersistentDataType.INTEGER);

		Builder builder = new Builder();

		builder.onComplete((completion) -> {
			String text = completion.getText();
			if (this.instance.getMethodes().removeIDKeyFromItem(item).isSimilar(this.instance.getMethodes().getGUIItem("shopAdmin", "changePriceItem")) 
					|| this.instance.getMethodes().removeIDKeyFromItem(item).isSimilar(this.instance.getMethodes().getGUIItem("shopAdmin", "changeSizeItem"))) {
				try {
					Integer.parseInt(text);
				} catch (NumberFormatException ex) {
					return Arrays.asList(AnvilGUI.ResponseAction.replaceInputText(this.instance.getMessage("notANumber")));
				}
			}

			RentTypeHandler rentHandler = this.instance.getMethodes().getTypeHandler(RentTypes.SHOP, shopId);

			if (rentHandler == null)
				return null;

			int catID = rentHandler.getCatID();

			CategoryHandler catHandler = this.instance.getMethodes().getCategory(RentTypes.SHOP, catID);

			if (catHandler == null)
				return null;

			if (this.instance.getMethodes().removeIDKeyFromItem(item).isSimilar(this.instance.getMethodes().getGUIItem("shopAdmin", "changeTimeItem"))) {
				// PLAYER WANTS TO CHANGE RENT TIME
				this.instance.getMethodes().setTime(p, RentTypes.SHOP, catID, text);
			} else if (this.instance.getMethodes().removeIDKeyFromItem(item).isSimilar(this.instance.getMethodes().getGUIItem("shopAdmin", "changePriceItem"))) {
				// PLAYER WANTS TO CHANGE RENT PRICE
				this.instance.getMethodes().setPrice(p, RentTypes.SHOP, catID, text);
			} else if (this.instance.getMethodes().removeIDKeyFromItem(item).isSimilar(this.instance.getMethodes().getGUIItem("shopAdmin", "changeSizeItem"))) {
				// PLAYER WANTS TO CHANGE SHOP SIZE
				this.instance.getMethodes().setSize(p, shopId, catID, text);
			} else if (this.instance.getMethodes().removeIDKeyFromItem(item).isSimilar(this.instance.getMethodes().getGUIItem("shopAdmin", "changeAliasItem"))) {
				// PLAYER WANTS TO CHANGE THE ALIAS
				text = text.substring(0, text.length() > 100 ? 100 : text.length());
				
				this.instance.getShopsSQL().setAlias(shopId, text);
				rentHandler.setAlias(text);
				
				p.sendMessage(this.instance.getMessage("shopChangedAlias")
						.replaceAll("(?i)%" + "shopId" + "%", String.valueOf(shopId))
						.replaceAll("(?i)%" + "alias" + "%", text));	
			} else if (this.instance.getMethodes().removeIDKeyFromItem(item).isSimilar(this.instance.getMethodes().getGUIItem("shopAdmin", "changeCategoryAliasItem"))) {
				// PLAYER WANTS TO CHANGE THE CATEGORY ALIAS
				text = text.substring(0, text.length() > 100 ? 100 : text.length());
					
				this.instance.getCategorySQL().setAlias(catHandler.getCatID(), RentTypes.SHOP, text);
				catHandler.setAlias(text);
					
				p.sendMessage(this.instance.getMessage("shopCategoryChangedAlias")
						.replaceAll("(?i)%" + "catId" + "%", String.valueOf(catHandler.getCatID()))
						.replaceAll("(?i)%" + "catAlias" + "%", text));
			}
			return Arrays.asList(AnvilGUI.ResponseAction.close());

		}).itemLeft(this.getAcceptItem(meta.getDisplayName())).plugin(this.instance).open(p);
	}

	private ItemStack getAcceptItem(String displayName) {
		ItemStack acceptItem = new ItemStack(Material.valueOf(this.instance.manageFile().getString("GUI.anvil.acceptItem.type").toUpperCase()));

		ItemMeta acceptItemMeta = acceptItem.getItemMeta();
		acceptItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

		List<String> lore = new ArrayList<>();
		for (String s : instance.manageFile().getStringList("GUI.anvil.acceptItem.lore")) {
			lore.add(ChatColor.translateAlternateColorCodes('&', s));
		}
		acceptItemMeta.setLore(lore);
		acceptItem.setItemMeta(acceptItemMeta);
		return acceptItem;
	}
}
