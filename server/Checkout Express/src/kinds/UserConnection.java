package kinds;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;

import utils.DSConverter;
import utils.TicketItem;

import com.google.appengine.api.channel.ChannelFailureException;
import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;

public class UserConnection extends AbstractKind
{
	protected String kindName() { return getKind(); }
	public static String getKind() { return "user_connection"; }
	protected Set<String> itemsToPay;
	protected String username;
	protected Map<String, Date> startTimes;
	protected String platform;
	public UserConnection(Key k, DatastoreService ds) throws EntityNotFoundException { super(k, ds); }
	public UserConnection(Entity e) { super(e); }

	public UserConnection(Key k, String plat)
	{
		setKey(k);
		username = null;
		startTimes = new HashMap<String, Date>();
		platform = plat;
		removeItems();
	}
	
	protected UserConnection(Key k, Set<String> itemsToPay, String username, Map<String, Date> startTimes)
	{
		setKey(k);
		this.itemsToPay = itemsToPay;
		this.username = username;
		this.startTimes = startTimes;
	}

	public UserConnection(TableKey table, ClosedUserConnection cuc)
	{
		setKey(table.getKey().getChild(getKind(), cuc.getKey().getName()));
		this.itemsToPay = cuc.itemsToPay;
		this.username = cuc.username;
		this.startTimes = cuc.startTimes;
	}

	public void addItem(String id)
	{
		itemsToPay.add(id);
	}

	public void removeItem(String id)
	{
		itemsToPay.remove(id);
	}

	public void removeItems()
	{
		itemsToPay = new HashSet<String>();
	}

	public Set<String> getItems()
	{
		return itemsToPay;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String u)
	{
		username = u;
	}

	public void logPosition(String pos)
	{
		startTimes.put(pos, new Date());
	}

	private static void sendMsg(String cmd, String param, Key k, ChannelService channelService)
	{
		try {
			channelService.sendMessage(new ChannelMessage(k.getName(), cmd+"\n"+param));
		} catch(ChannelFailureException e) {}//We must have missed the disconnect
	}

	public static void sendItemsUpdateAndRemoveSplit(List<TicketItem> items, String splitID, Key k, ChannelService channelService)
	{
		sendMsg("items_and_remove_split", items+"\n"+splitID, k, channelService);
	}

	public static void sendItemsUpdateAndRestoreSplit(List<TicketItem> items, String splitID, List<String> splitItems, Key k, ChannelService channelService)
	{
		StringBuffer splitItemsStr = new StringBuffer();
		for(String splitItem : splitItems) {
			if(splitItemsStr.length() > 0)
				splitItemsStr.append(",");
			splitItemsStr.append("\""+splitItem+"\"");
		}
		sendMsg("items_and_restore_split", items+"\n"+splitID+"\n["+splitItemsStr+"]", k, channelService);
	}

	public static void sendSplitUpdate(String splitID, JSONArray splitItems, Key k, ChannelService channelService)
	{
		sendMsg("split", splitID+"\n"+splitItems, k, channelService);
	}

	public static void sendStartSplit(Key k, ChannelService channelService)
	{
		sendMsg("start_split", "", k, channelService);
	}

	public static void sendCancelSplit(Key k, ChannelService channelService)
	{
		sendMsg("cancel_split", "", k, channelService);
	}

	public static void sendErrMessage(String msg, Key k, ChannelService channelService)
	{
		sendMsg("err", msg, k, channelService);
	}

	public static void sendCloseMessage(Key k, ChannelService channelService)
	{
		sendMsg("done", k.getName(), k, channelService);
	}

	public Entity toEntity()
	{
		Entity e = new Entity(getKey());
		DSConverter.set(e, "itemsToPay", itemsToPay, DSConverter.DataTypes.SET);
		e.setProperty("username", username);
		DSConverter.set(e, "startTimes", startTimes, DSConverter.DataTypes.MAP);
		e.setProperty("platform", platform);
		return e;
	}
	@SuppressWarnings("unchecked")
	public void fromEntity(Entity e)
	{
		itemsToPay = (Set<String>) DSConverter.get(e, "itemsToPay", DSConverter.DataTypes.SET);
		username = (String) e.getProperty("username");
		startTimes = (Map<String, Date>) DSConverter.get(e, "startTimes", DSConverter.DataTypes.MAP);
		platform = (String) e.getProperty("platform");
	}
}
