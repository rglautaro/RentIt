package me.truemb.rentit.handler;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import me.truemb.rentit.enums.RentTypes;
import me.truemb.rentit.gui.UserShopGUI;
import me.truemb.rentit.main.Main;

public class RentTypeHandler {
	
	private RentTypes type;
	private int id = -1;
	private String alias = "";
	private int catID = -1;
	
	private UUID ownerUUID;
	private String ownerName;
	private Timestamp nextPayment;
	private boolean autoPayment;
	
	private Inventory sellInv; //INVENTORY IS ALREADY LOADED AND KEEPS THE INSTANCE. SO EVERYBODY UPDATE, IF SOMETHING CHANGES
	private Inventory buyInv;
	
	public RentTypeHandler(RentTypes type, int id, int catID, UUID ownerUUID, String ownerName, Timestamp nextPayment, boolean autoPayment) {
		this.type = type;
		this.id = id;
		this.catID = catID;

		this.ownerUUID = ownerUUID;
		this.ownerName = ownerName;
		this.nextPayment = nextPayment;
		this.autoPayment = autoPayment;
	}
	
	//GET METHODES
	public int getID() {
		return this.id;
	}
	
	public String getAlias() {
		return this.alias;
	}

	public int getCatID() {
		return this.catID;
	}

	public UUID getOwnerUUID() {
		return this.ownerUUID;
	}

	public String getOwnerName() {
		return this.ownerName;
	}

	public Timestamp getNextPayment() {
		return this.nextPayment;
	}
	
	public boolean isAutoPayment() {
		return this.autoPayment;
	}

	public RentTypes getType() {
		return this.type;
	}



	//SET METHODES
	public void setAlias(String alias) {
		this.alias = alias;
	}

	public void setOwner(UUID ownerUUID, String ingameName) {
		this.ownerUUID = ownerUUID;
		this.ownerName = ingameName;
	}
	
	public void setAutoPayment(boolean autoPayment) {
		this.autoPayment = autoPayment;
	}

	public void setNextPayment(Timestamp nextPayment) {
		this.nextPayment = nextPayment;
	}

	//OTHERS
	public void reset(Main plugin) {
		
		//PERMISSIONS RESET IN CACHE
		for(Player all : Bukkit.getOnlinePlayers()) {
		
			UUID uuid = all.getUniqueId();
			if(!plugin.playerHandlers.containsKey(uuid))
				continue;
			
			PlayerHandler playerHandler = plugin.getMethodes().getPlayerHandler(uuid);
			
			if(playerHandler == null)
				continue;
			
			PermissionsHandler permsHandler = playerHandler.getPermsHandler(type);

			if(permsHandler == null)
				continue;
			
			permsHandler.reset(this.getID());
		}
		
		//REMOVE OWNING LIST ENTRY FROM OWNER IN CACHE
		UUID uuid = this.getOwnerUUID();

		PlayerHandler playerHandler = plugin.getMethodes().getPlayerHandler(uuid);
		
		if(playerHandler != null) {
			playerHandler.removeOwningRent(type, this.getID());
		}
		
		//RESET CACHE
		this.setOwner(null, null);
		
		//INVENTORY
		this.setSellInv(UserShopGUI.getSellInv(plugin, id, null));
		this.setBuyInv(UserShopGUI.getBuyInv(plugin, id, null));
	}
	
	public boolean isOwned() {
		return this.getOwnerUUID() != null;
	}

	public void setBuyInv(Inventory buyInv) {
		if(this.buyInv != null) {
			List<HumanEntity> list = this.buyInv.getViewers();
			for(int i = list.size() - 1; i >= 0; i--)
				list.get(i).closeInventory();
		}
		this.buyInv = buyInv;
	}

	public void setSellInv(Inventory sellInv) {
		if(this.sellInv != null) {
			List<HumanEntity> list = this.sellInv.getViewers();
			for(int i = list.size() - 1; i >= 0; i--)
				list.get(i).closeInventory();
		}
		this.sellInv = sellInv;
	}

	public Inventory getBuyInv() {
		return this.buyInv;
	}

	public Inventory getSellInv() {
		return this.sellInv;
	}

}
