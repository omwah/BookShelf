package me.Pew446.BookShelf;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
//import org.bukkit.event.block.BlockPistonExtendEvent;
//import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import me.Pew446.BookShelf.BookShelf;
import net.minecraft.server.EntityItem;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;
public class BookListener implements Listener {
	public static BookShelf plugin;
	public BookListener(BookShelf instance) {
		plugin = instance;
	}	
	private String author;
    private String title;
    private String[] pages;
    HashMap<Location, InventoryHolder> map = new HashMap<Location, InventoryHolder>();
    HashMap<Location, Inventory> map2 = new HashMap<Location, Inventory>();
    HashMap<Location, Boolean> map3 = new HashMap<Location, Boolean>();
    static ResultSet r;
	@EventHandler
	public void onClick(PlayerInteractEvent j)
	{
		Player p = j.getPlayer();
		if(j.getClickedBlock() != null)
		{
			if(j.getClickedBlock().getType() == Material.BOOKSHELF && j.getAction() == Action.RIGHT_CLICK_BLOCK)
			{
				plugin.getLogger().info("Clicked");
				Location loc = j.getClickedBlock().getLocation();
				if(j.getBlockFace() == BlockFace.NORTH || j.getBlockFace() == BlockFace.EAST || j.getBlockFace() == BlockFace.SOUTH || j.getBlockFace() == BlockFace.WEST)
				{
					r = BookShelf.mysql.query("SELECT * FROM copy WHERE x="+loc.getX()+" AND y="+loc.getY()+" AND z="+loc.getZ()+";");
					try {
						if(!r.next())
						{
							r.close();
							BookShelf.mysql.query("INSERT INTO copy (x,y,z,bool) VALUES ("+loc.getX()+","+loc.getY()+","+loc.getZ()+",0);");
						}
						else
						{
							r.close();
						}
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					if(!map.containsKey(j.getClickedBlock().getLocation()))
					{
						plugin.getLogger().info("not had");
						Inventory inv = Bukkit.createInventory(p, plugin.getConfig().getInt("rows")*9, "BookShelf");
						//inv.setMaxStackSize(1);
						Block cl = j.getClickedBlock();
						int x = cl.getX();
						int y = cl.getY();
						int z = cl.getZ();
						map.put(cl.getLocation(), inv.getHolder());
						map2.put(cl.getLocation(), inv);
						
						r = BookShelf.mysql.query("SELECT COUNT(*) FROM items WHERE x=" + x + " AND y=" + y + " AND z=" + z + ";");
						try {
							if(!r.next())
							{
								r.close();
								p.openInventory(inv);
								if(!map3.containsKey(loc))
									map3.put(loc, true);
								return;
							}
							else
							{
								r.close();
								r = BookShelf.mysql.query("SELECT * FROM items WHERE x=" + x + " AND y=" + y + " AND z=" + z + ";");
								ArrayList<String> auth = new ArrayList<String>();
								ArrayList<String> titl = new ArrayList<String>();
								ArrayList<Integer> type = new ArrayList<Integer>();
								ArrayList<Integer> id = new ArrayList<Integer>();
								ArrayList<Integer> loca = new ArrayList<Integer>();
								ArrayList<Integer> amt = new ArrayList<Integer>();
		 						while(r.next())
								{
									auth.add(r.getString("author"));
									titl.add(r.getString("title"));
									id.add(r.getInt("id"));
									type.add(r.getInt("type"));
									loca.add(r.getInt("loc"));
									amt.add(r.getInt("amt"));
								}
								r.close();
								ArrayList<String> pages = new ArrayList<String>();
								for(int i=0;i<id.size();i++)
								{
									if(type.get(i) == Material.BOOK.getId())
									{
										inv.setItem(loca.get(i), new ItemStack(Material.BOOK, amt.get(i)));
									}
									else
									{
										r = BookShelf.mysql.query("SELECT * FROM pages WHERE id="+id.get(i)+";");
										while(r.next())
										{
											pages.add(r.getString("text"));
										}
										r.close();
										String[] thepages = new String[pages.size()];
										thepages = pages.toArray(thepages);
										if(type.get(i) == Material.WRITTEN_BOOK.getId())
										{
											Book(titl.get(i), auth.get(i), thepages);
											inv.setItem(loca.get(i), generateItemStack(0));
											pages.clear();
										}
										else if(type.get(i) == Material.BOOK_AND_QUILL.getId())
										{
											Book("null", "null", thepages);
											inv.setItem(loca.get(i), generateItemStack(1));
											pages.clear();
										}
									}
								}
								auth.clear();
								titl.clear();
								type.clear();
								id.clear();
								loca.clear();
								amt.clear();
								p.openInventory(inv);
								if(!map3.containsKey(loc))
									map3.put(loc, true);
							}
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else
					{
						plugin.getLogger().info("had");
						Inventory inv = map2.get(j.getClickedBlock().getLocation());
						Player player = (Player) inv.getHolder();
						if(player.getName() == p.getName())
						{
							j.setCancelled(true);
						}
						else
						{
							p.openInventory(inv);
						}
					}
				}
			}
		}
	}
	@SuppressWarnings("rawtypes")
	public Location getKey(HashMap map, InventoryHolder inv) 
	{
		Set key = map.keySet();
		for (Iterator i = key.iterator(); i.hasNext();) 
			{
				Location next = (Location) i.next();
				if (map.get(next).equals(inv)) 
				{
					return next;
				}
			}
		return null;
	}
	@EventHandler
	public void onAdd(InventoryCloseEvent j)
	{
		if(map.containsValue(j.getInventory().getHolder())){
			//plugin.getLogger().info("contained");
			Location loc = getKey(map,j.getInventory().getHolder());
			ItemStack[] cont = j.getInventory().getContents();
			int x = loc.getBlockX();
			int y = loc.getBlockY();
			int z = loc.getBlockZ();
			if(j.getInventory().getHolder() instanceof Player)
			{
				//plugin.getLogger().info("yes");
				Player player = (Player) j.getInventory().getHolder();
				if(j.getPlayer().getName() == player.getName())
				{
					//plugin.getLogger().info("same name");
					map.remove(loc);
					j.getInventory().getViewers().remove(j.getInventory().getHolder());
					if(!j.getInventory().getViewers().isEmpty())
					{
						//plugin.getLogger().info(j.getInventory().getViewers()+"");
						//plugin.getLogger().info("not empty");
						map.put(loc, (Player) j.getInventory().getViewers().iterator().next());
					}
					else
					{
						//plugin.getLogger().info("empty");
						r = BookShelf.mysql.query("SELECT * FROM copy WHERE x="+loc.getX()+" AND y="+loc.getY()+" AND z="+loc.getZ()+";");
						try {
							if(r.getInt("bool") == 0)
							{
								r.close();
								if(map3.get(loc))
								{
									BookShelf.mysql.query("DELETE FROM items WHERE x=" + x + " AND y=" + y + " AND z=" + z + ";");
									plugin.getLogger().info("Deleted");
									for(int i=0;i<cont.length;i++)
									{
										if(cont[i] != null)
										{
											if(cont[i].getType() == Material.BOOK_AND_QUILL || cont[i].getType() == Material.WRITTEN_BOOK)
											{
												Book(cont[i]);
												String title = getTitle().replaceAll("'", "''");
												String author = getAuthor().replaceAll("'", "''");
												int type = cont[i].getTypeId(); 
												if(cont[i].getType() == Material.BOOK_AND_QUILL)
												{
													BookShelf.mysql.query("INSERT INTO items (x,y,z,author,title,type,loc,amt) VALUES ("+x+","+y+","+z+", 'null', 'null',"+type+","+i+",1);");
												}
												else
												{
													BookShelf.mysql.query("INSERT INTO items (x,y,z,author,title,type,loc,amt) VALUES ("+x+","+y+","+z+",'"+author+"','"+title+"',"+type+","+i+",1);");	
												}
												int id = getidxyz(x,y,z);
												BookShelf.mysql.query("DELETE FROM pages WHERE id="+id+";");
												//plugin.getLogger().info("Length is "+getPages().length+" and id is "+id+" and 0 is "+getPages()[0]);
												for(int k=0;k<getPages().length;k++)
												{
													BookShelf.mysql.query("INSERT INTO pages (id, text) VALUES ("+id+",'"+getPages()[k].replaceAll("'", "''")+"');");
												}
											}
											else if(cont[i].getType() == Material.BOOK)
											{
												int type = cont[i].getTypeId(); 
												BookShelf.mysql.query("INSERT INTO items (x,y,z,author,title,type,loc,amt) VALUES ("+x+","+y+","+z+", 'null', 'null',"+type+","+i+","+cont[i].getAmount()+");");
											}
										}
									}
									map3.remove(loc);
								}
							}
							else
							{
								r.close();
							}
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						map2.remove(loc);
					}
				}
			}
		}
	}
	@EventHandler
	public void onBreak(BlockBreakEvent j)
	{
		if(map.containsKey(j.getBlock().getLocation()))
		{
			Inventory inv = map2.get(j.getBlock().getLocation());
			List<HumanEntity> viewers = inv.getViewers();
			for(int i = 0;i<viewers.size();i++)
			{
				viewers.get(i).closeInventory();
			}
		}
		//plugin.getLogger().info("broke");
		if(j.getBlock().getType() == Material.BOOKSHELF)
		{
			//plugin.getLogger().info("book yes");
			r = BookShelf.mysql.query("SELECT * FROM items WHERE x=" + j.getBlock().getX() + " AND y=" + j.getBlock().getY() + " AND z=" + j.getBlock().getZ() + ";");
			try {
				ArrayList<String> auth = new ArrayList<String>();
				ArrayList<String> titl = new ArrayList<String>();
				ArrayList<Integer> type = new ArrayList<Integer>();
				ArrayList<Integer> id = new ArrayList<Integer>();
				ArrayList<Integer> amt = new ArrayList<Integer>();
				while(r.next())
				{
					//plugin.getLogger().info("ya next");
					auth.add(r.getString("author"));
					titl.add(r.getString("title"));
					id.add(r.getInt("id"));
					type.add(r.getInt("type"));
					amt.add(r.getInt("amt"));
				}
				r.close();
				ArrayList<String> pages = new ArrayList<String>();
				//plugin.getLogger().info("k1 "+id.size());
				for(int i=0;i<id.size();i++)
				{
					if(type.get(i) == Material.BOOK.getId())
					{
						//plugin.getLogger().info("drop book.");
						BookShelf.mysql.query("DELETE FROM items WHERE id=" + id.get(i) + ";");
						j.getBlock().getWorld().dropItem(j.getBlock().getLocation(), new ItemStack(Material.BOOK, amt.get(i)));
					}
					else
					{
						//plugin.getLogger().info(type.get(i)+" and " +Material.BOOK.getId());
						r = BookShelf.mysql.query("SELECT * FROM pages WHERE id="+id.get(i)+";");
						while(r.next())
						{
							pages.add(r.getString("text"));
						}
						r.close();
						String[] thepages = new String[pages.size()];
						thepages = pages.toArray(thepages);
						if(type.get(i) == Material.WRITTEN_BOOK.getId())
						{
							Book(titl.get(i), auth.get(i), thepages);
							Location loc = j.getBlock().getLocation();
							Random gen = new Random();
							double xs = gen.nextFloat() * 0.7F + (1.0F - 0.7F) * 0.5D;
							double ys = gen.nextFloat() * 0.7F + (1.0F - 0.7F) * 0.5D;
							double zs = gen.nextFloat() * 0.7F + (1.0F - 0.7F) * 0.5D;
							EntityItem entity = new EntityItem(((CraftWorld) loc.getWorld()).getHandle(), loc.getX() + xs, loc.getY() + ys, loc.getZ() + zs, ((CraftItemStack) generateItemStack(0)).getHandle());
							((CraftWorld) loc.getWorld()).getHandle().addEntity(entity);
							pages.clear();
						}
						else if(type.get(i) == Material.BOOK_AND_QUILL.getId())
						{
							Book("null", "null", thepages);
							Location loc = j.getBlock().getLocation();
							Random gen = new Random();
							double xs = gen.nextFloat() * 0.7F + (1.0F - 0.7F) * 0.5D;
							double ys = gen.nextFloat() * 0.7F + (1.0F - 0.7F) * 0.5D;
							double zs = gen.nextFloat() * 0.7F + (1.0F - 0.7F) * 0.5D;
							EntityItem entity = new EntityItem(((CraftWorld) loc.getWorld()).getHandle(), loc.getX() + xs, loc.getY() + ys, loc.getZ() + zs, ((CraftItemStack) generateItemStack(1)).getHandle());
							((CraftWorld) loc.getWorld()).getHandle().addEntity(entity);
							pages.clear();
						}
						BookShelf.mysql.query("DELETE FROM items WHERE id=" + id.get(i) + ";");
						BookShelf.mysql.query("DELETE FROM pages WHERE id=" + id.get(i) + ";");
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Location loc = j.getBlock().getLocation();
			BookShelf.mysql.query("DELETE FROM copy WHERE x="+loc.getX()+" AND y="+loc.getY()+" AND z="+loc.getZ()+";");
		}
	}
	@EventHandler
	public void onInv(InventoryClickEvent j)
	{
		if(j.getInventory().getTitle() == "BookShelf")
		{
			if(j.getCurrentItem() == null)
			{
				return;
			}
			if(plugin.getConfig().getBoolean("permissions.allow_book") == false || !Bukkit.getPlayer(j.getWhoClicked().getName()).hasPermission("bookshelf.book"))
			{
				if(j.getCurrentItem().getType() == Material.BOOK)
				{
					j.setCancelled(true);
					return;
				}
				else if(j.getCursor().getType() == Material.BOOK)
				{
					j.setCancelled(true);
					return;
				}
			}
			if(plugin.getConfig().getBoolean("permissions.allow_book_and_quill") == false || !Bukkit.getPlayer(j.getWhoClicked().getName()).hasPermission("bookshelf.baq"))
			{
				if(j.getCurrentItem().getType() == Material.BOOK_AND_QUILL)
				{
					j.setCancelled(true);
					return;
				}
				else if(j.getCursor().getType() == Material.BOOK_AND_QUILL)
				{
					j.setCancelled(true);
					return;
				}
			}
			if(plugin.getConfig().getBoolean("permissions.allow_signed") == false || !Bukkit.getPlayer(j.getWhoClicked().getName()).hasPermission("bookshelf.signed"))
			{
				if(j.getCurrentItem().getType() == Material.WRITTEN_BOOK)
				{
					j.setCancelled(true);
					return;
				}
				else if(j.getCursor().getType() == Material.WRITTEN_BOOK)
				{
					j.setCancelled(true);
					return;
				}
			}
			else
			{
				j.setCancelled(true);
			}
		}
	}
	@EventHandler
	public void onPlace(BlockPlaceEvent j)
	{
		if(j.getBlock().getType() == Material.BOOKSHELF)
		{
			if(j.getBlockAgainst().getType() == Material.BOOKSHELF)
			{
				if(j.getBlockAgainst().getFace(j.getBlock()) == BlockFace.UP || j.getBlockAgainst().getFace(j.getBlock()) == BlockFace.DOWN)
				{
					Location loc = j.getBlock().getLocation();
					BookShelf.mysql.query("INSERT INTO copy (x,y,z,bool) VALUES ("+loc.getX()+","+loc.getY()+","+loc.getZ()+", 0);");
					return;
				}
				j.setBuild(false);
				j.setCancelled(true);
			}
			else
			{
				Location loc = j.getBlock().getLocation();
				BookShelf.mysql.query("INSERT INTO copy (x,y,z,bool) VALUES ("+loc.getX()+","+loc.getY()+","+loc.getZ()+", 0);");
			}
		}
		if(j.getBlockAgainst().getType() == Material.BOOKSHELF)
		{
			if(j.getBlockAgainst().getFace(j.getBlock()) == BlockFace.UP || j.getBlockAgainst().getFace(j.getBlock()) == BlockFace.DOWN)
				return;
			j.setBuild(false);
			j.setCancelled(true);
		}
	}
	@EventHandler
	public void onDrop(PlayerDropItemEvent j)
	{
		Player p = j.getPlayer();
		if(p.getTargetBlock(null, 10).getType() == Material.BOOKSHELF)
		{
			if(j.getItemDrop().getItemStack().getType() == Material.BOOK || j.getItemDrop().getItemStack().getType() == Material.WRITTEN_BOOK || j.getItemDrop().getItemStack().getType() == Material.BOOK_AND_QUILL)
			{
				Location loc = p.getTargetBlock(null, 10).getLocation();
				r = BookShelf.mysql.query("SELECT * FROM copy WHERE x="+loc.getX()+" AND y="+loc.getY()+" AND z="+loc.getZ()+";");
				try {
					if(r.getInt("bool") == 1)
					{
						j.setCancelled(true);
					}
					r.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
			
	}
	int getidxyz(int x, int y, int z)
	{
		int last = -1;
		r = BookShelf.mysql.query("SELECT * FROM items WHERE x=" + x + " AND y=" + y + " AND z=" + z + " ORDER BY id DESC LIMIT 1;");
		try {
			last = r.getInt("id");
			r.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return last;
	}
	public void Book(ItemStack bookItem){
        NBTTagCompound bookData = ((CraftItemStack) bookItem).getHandle().tag;
        if(bookItem.getType() == Material.WRITTEN_BOOK)
        {
	        this.author = bookData.getString("author");
	        this.title = bookData.getString("title");
        }
        else
        {
        	this.author = "null";
        	this.title = "null";
        }
        if(bookData == null)
        {
        	String[] sPages = {""};
                    
            this.pages = sPages;
            return;
        }
        NBTTagList nPages;
        if(bookData.getList("pages") != null)
        	nPages = bookData.getList("pages");
        else
        	nPages = new NBTTagList("");
        String[] sPages = new String[nPages.size()];
        for(int i = 0;i<nPages.size();i++)
        {
            sPages[i] = nPages.get(i).toString();
        }
                
        this.pages = sPages;
    }
    void Book(String title, String author, String[] pages) {
        this.title = title;
        this.author = author;
        this.pages = pages;
    }
    public String getAuthor()
    {
        return author;
    }
    public void setAuthor(String sAuthor)
    {
        author = sAuthor;
    }
    public String getTitle()
    {
        return title;
    }
    public String[] getPages()
    {
        return pages;
    }
    public ItemStack generateItemStack(int type){
    	CraftItemStack newbook = new CraftItemStack(Material.WRITTEN_BOOK);
  		CraftItemStack newbook1 = new CraftItemStack(Material.BOOK_AND_QUILL);
    	
        NBTTagCompound newBookData = new NBTTagCompound();
        
        newBookData.setString("author",author);
        newBookData.setString("title",title);
                
        NBTTagList nPages = new NBTTagList();
        for(int i = 0;i<pages.length;i++)
        {  
            nPages.add(new NBTTagString(pages[i],pages[i]));
        }
        
        newBookData.set("pages", nPages);
        
        if(type == 1)
        {
	        newbook1.getHandle().tag = newBookData;
	        return (ItemStack) newbook1;
        }
        else
        {
        	newbook.getHandle().tag = newBookData;
            return (ItemStack) newbook;
        }
    }
	
}
