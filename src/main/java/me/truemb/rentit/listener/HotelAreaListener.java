package me.truemb.rentit.listener;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.inventory.EquipmentSlot;

import me.truemb.rentit.enums.CategorySettings;
import me.truemb.rentit.enums.RentTypes;
import me.truemb.rentit.handler.RentTypeHandler;
import me.truemb.rentit.main.Main;

public class HotelAreaListener implements Listener {

	private Main instance;
	private RentTypes type = RentTypes.HOTEL;
	
	public HotelAreaListener(Main plugin) {
		this.instance = plugin;
		this.instance.getServer().getPluginManager().registerEvents(this, this.instance);
	}
	
	@EventHandler
    public void onPermissionForBreak(BlockBreakEvent e) {
		
		Player p = e.getPlayer();
		Block b = e.getBlock();
		Location loc = b.getLocation();
		
		boolean canceled = this.protectedRegion(p, loc, true);
		if(canceled)
			e.setCancelled(canceled);
    }
	
	@EventHandler
    public void onPermissionForPlace(BlockPlaceEvent e) {
		
		Player p = e.getPlayer();
		Block b = e.getBlockPlaced();
		Location loc = b.getLocation();
		
		boolean canceled = this.protectedRegion(p, loc, true);
		if(canceled)
			e.setCancelled(canceled);
    }

	@EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleDMG(VehicleDamageEvent e) {
		
		Player p = null;
		if(e.getAttacker() instanceof Player)
			p = (Player) e.getAttacker();
		else if(e.getAttacker() instanceof AbstractArrow && ((AbstractArrow) e.getAttacker()).getShooter() instanceof Player)
			p = (Player) ((AbstractArrow) e.getAttacker()).getShooter();
		
		if(p == null) return;
		
		Vehicle target = e.getVehicle();
		Location loc = target.getLocation();

		boolean canceled = this.protectedRegion(p, loc, true);
		if(canceled)
			e.setCancelled(canceled);
    }

	@EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDMG(EntityDamageByEntityEvent e) {

		Player p = null;
		if(e.getDamager() instanceof Player)
			p = (Player) e.getDamager();
		else if(e.getDamager() instanceof AbstractArrow && ((AbstractArrow) e.getDamager()).getShooter() instanceof Player)
			p = (Player) ((AbstractArrow) e.getDamager()).getShooter();
		
		if(p == null) return;
		
		Entity target = e.getEntity();
		Location loc = target.getLocation();

		boolean canceled = this.protectedRegion(p, loc, true);
		if(canceled)
			e.setCancelled(canceled);
    }

	@EventHandler(priority = EventPriority.HIGHEST)
    public void onHangingDMG(HangingBreakByEntityEvent e) {
		
		Player p = null;
		if(e.getRemover() instanceof Player)
			p = (Player) e.getRemover();
		else if(e.getRemover() instanceof AbstractArrow && ((AbstractArrow) e.getRemover()).getShooter() instanceof Player)
			p = (Player) ((AbstractArrow) e.getRemover()).getShooter();
		
		if(p == null) return;
		
		Entity target = e.getEntity();
		Location loc = target.getLocation();

		boolean canceled = this.protectedRegion(p, loc, true);
		if(canceled)
			e.setCancelled(canceled);
    }
	

	@EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityInteraction(PlayerInteractAtEntityEvent e) {
		
		if(e.getHand() != EquipmentSlot.HAND)
			return;
		
		Player p = e.getPlayer();
		Entity target = e.getRightClicked();
		Location loc = target.getLocation();
		
		boolean canceled = this.protectedRegion(p, loc, true);
		if(canceled)
			e.setCancelled(canceled);
    }
	
	@EventHandler(priority = EventPriority.HIGHEST)
    public void onInteraction(PlayerInteractEvent e) {
		
		if(e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		Block b = e.getClickedBlock();
		if(b == null)
			return;
		
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		Location loc = b.getLocation();
		
		if(p.hasPermission(this.instance.manageFile().getString("Permissions.build")))
			return;
		
		if((b.getBlockData() instanceof Door || b.getBlockData() instanceof TrapDoor || b.getBlockData() instanceof Gate) && this.instance.getDoorFileManager().isProtectedDoor(loc) && this.instance.getDoorFileManager().getTypeFromDoor(loc).equals(this.type)){
			//DOOR LOCKED?
			
			int shopId = this.instance.getDoorFileManager().getIdFromDoor(loc);
	
		    RentTypeHandler rentHandler = instance.getMethodes().getTypeHandler(this.type, shopId);
	
			if (rentHandler == null)
				return;
			
			if(!this.instance.getAreaFileManager().isDoorStatusSet(this.type, shopId)) {

				if(!p.hasPermission(this.instance.manageFile().getString("Permissions.bypass.doors")) 
						&& this.instance.manageFile().isSet("Options.categorySettings.HotelCategory." + rentHandler.getCatID() + "." + CategorySettings.doorsClosedUntilBuy.toString()) 
						&& this.instance.manageFile().getBoolean("Options.categorySettings.HotelCategory." + rentHandler.getCatID() + "." + CategorySettings.doorsClosedUntilBuy.toString())) {
					
					e.setCancelled(true);
					e.setUseInteractedBlock(Result.DENY);
					
					if(rentHandler.getOwnerUUID() != null)
						if(e.getHand() == EquipmentSlot.HAND)
							p.sendMessage(this.instance.getMessage("hotelDoorStillClosed"));
					return;
				}else {
					e.setCancelled(false);
					e.setUseInteractedBlock(Result.ALLOW);
					return;
				}
			}
			if(this.instance.getAreaFileManager().isDoorClosed(this.type, shopId)) {
				if(p.hasPermission(this.instance.manageFile().getString("Permissions.bypass.doors")) 
						|| ((!this.instance.manageFile().isSet("Options.categorySettings.HotelCategory." + rentHandler.getCatID() + "." + CategorySettings.ownerBypassLock.toString()) 
						|| this.instance.manageFile().getBoolean("Options.categorySettings.HotelCategory." + rentHandler.getCatID() + "." + CategorySettings.ownerBypassLock.toString())) 
						&& (this.instance.getMethodes().hasPermission(this.type, shopId, uuid, this.instance.manageFile().getString("UserPermissions.hotel.Door")) 
						|| this.instance.getMethodes().hasPermission(this.type, shopId, uuid, this.instance.manageFile().getString("UserPermissions.hotel.Admin"))))) {
					
					e.setCancelled(false);
					e.setUseInteractedBlock(Result.ALLOW);
					return;
				}else {
					e.setCancelled(true);
					e.setUseInteractedBlock(Result.DENY);
					
					if(e.getHand() == EquipmentSlot.HAND)
						p.sendMessage(this.instance.getMessage("hotelDoorStillClosed"));
					
					return;
				}
			}
		}else if(b.getType() == Material.ENDER_CHEST) {

			int shopId = this.instance.getAreaFileManager().getIdFromArea(this.type, loc);

		    RentTypeHandler rentHandler = this.instance.getMethodes().getTypeHandler(this.type, shopId);

			if (rentHandler == null)
				return; //DOES SHOP EXISTS?
			
			//ENDER CHEST INTERACTION
			if(this.instance.manageFile().isSet("Options.categorySettings.HotelCategory." + rentHandler.getCatID() + "." + CategorySettings.useEnderchest.toString()) 
					&& this.instance.manageFile().getBoolean("Options.categorySettings.HotelCategory." + rentHandler.getCatID() + "." + CategorySettings.useEnderchest.toString())) {
				
				if(this.instance.getWorldGuard() != null) {
					if(!this.instance.getMethodes().isMemberFromRegion(this.type, shopId, p.getWorld(), uuid)) {
						e.setCancelled(true);
						p.sendMessage(this.instance.getMessage("notShopOwner"));
						return;
					}
					return;
				}

				if(rentHandler.getOwnerUUID() == null || !rentHandler.getOwnerUUID().equals(uuid) && !this.instance.getAreaFileManager().isMember(this.type, shopId, uuid)) {
					e.setCancelled(true);
					p.sendMessage(this.instance.getMessage("notShopOwner"));
					return;
				}
			}
			return;
			
		}else if(b.getType().toString().contains("_BED")) {

			int shopId = this.instance.getAreaFileManager().getIdFromArea(this.type, loc);

		    RentTypeHandler rentHandler = this.instance.getMethodes().getTypeHandler(this.type, shopId);

			if (rentHandler == null)
				return; //DOES SHOP EXISTS?
			
			//ENDER CHEST INTERACTION
			if(this.instance.manageFile().isSet("Options.categorySettings.HotelCategory." + rentHandler.getCatID() + "." + CategorySettings.useBed.toString()) 
					&& this.instance.manageFile().getBoolean("Options.categorySettings.HotelCategory." + rentHandler.getCatID() + "." + CategorySettings.useBed.toString())) {
				
				if(this.instance.getWorldGuard() != null) {
					if(!this.instance.getMethodes().isMemberFromRegion(this.type, shopId, p.getWorld(), uuid)) {
						e.setCancelled(true);
						p.sendMessage(this.instance.getMessage("notShopOwner"));
						return;
					}
					return;
				}

				if(rentHandler.getOwnerUUID() == null || !rentHandler.getOwnerUUID().equals(uuid) && !this.instance.getAreaFileManager().isMember(this.type, shopId, uuid)) {
					e.setCancelled(true);
					p.sendMessage(this.instance.getMessage("notShopOwner"));
					return;
				}
			}
			return;
			
		}else if(b.getType() == Material.CHEST || b.getType() == Material.BARREL) {
			//CHEST INTERACTION
			
			int hotelId = this.instance.getAreaFileManager().getIdFromArea(this.type, loc);

		    RentTypeHandler rentHandler = this.instance.getMethodes().getTypeHandler(this.type, hotelId);

			if (rentHandler == null)
				return; //DOES SHOP EXISTS?
		    
			if(!p.hasPermission(this.instance.manageFile().getString("Permissions.bypass.chests")) 
					&& !this.instance.getMethodes().hasPermission(this.type, hotelId, uuid, this.instance.manageFile().getString("UserPermissions.hotel.Admin"))) {
				
				e.setCancelled(true);
				
				if(e.getHand() == EquipmentSlot.HAND)
					p.sendMessage(this.instance.getMessage("notHotelOwner"));
			}
		}else{
			
			//INTERACTION
			boolean canceled = this.protectedRegion(p, loc, e.getHand() == EquipmentSlot.HAND);
			if(canceled)
				e.setCancelled(canceled);
			else {
				//SPAWNING

				if(p.getInventory().getItemInMainHand() == null && p.getInventory().getItemInOffHand() == null || p.getInventory().getItemInMainHand().getType().isBlock() && p.getInventory().getItemInOffHand().getType().isBlock())
					return;
				
				BlockFace facing = e.getBlockFace();
				Block facingBlock = b.getRelative(facing);
				
				if(facingBlock == null)
					return;

				boolean canceledSpawn = this.protectedRegion(p, facingBlock.getLocation(), e.getHand() == EquipmentSlot.HAND);
				if(canceledSpawn)
					e.setCancelled(canceledSpawn);
			}
		}
    }
	
	private boolean protectedRegion(Player p, Location loc, boolean withMessages) {
		
		if(p.hasPermission(this.instance.manageFile().getString("Permissions.build")))
			return false;
		
		UUID uuid = p.getUniqueId();
		int hotelId = this.instance.getAreaFileManager().getIdFromArea(this.type, loc);

	    RentTypeHandler rentHandler = instance.getMethodes().getTypeHandler(this.type, hotelId);

		if (rentHandler == null)
			return false;

		if(!this.instance.manageFile().getBoolean("Options.defaultPermissions.hotel.build")) {
			if(withMessages)
				p.sendMessage(this.instance.getMessage("featureDisabled"));
			return true;
		}
		
		if(this.instance.getWorldGuard() != null) {
			if(!this.instance.getMethodes().isMemberFromRegion(this.type, hotelId, p.getWorld(), uuid)) {
				if(withMessages)
					p.sendMessage(this.instance.getMessage("notHotelOwner"));
				return true;
			}
			return false;
		}

		if(rentHandler.getOwnerUUID() == null || !rentHandler.getOwnerUUID().equals(uuid) && !this.instance.getAreaFileManager().isMember(this.type, hotelId, uuid)) {
			if(withMessages)
				p.sendMessage(this.instance.getMessage("notHotelOwner"));
			return true;
		}
		return false;
    }
}
