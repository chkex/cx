package kinds;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;

public class BluetoothUUIDToRestaurantPointer extends AbstractKind
{
	protected String kindName() { return getKind(); }
	public static String getKind() { return "bu_to_r_ptr"; }
	String restaurant;
	public BluetoothUUIDToRestaurantPointer(Key k, DatastoreService ds) throws EntityNotFoundException { super(k, ds); }
	public BluetoothUUIDToRestaurantPointer(Entity e) { super(e); }

	public BluetoothUUIDToRestaurantPointer(Key k, String restr)
	{
		setKey(k);
		this.restaurant = restr;
	}

	public String getKeyName()
	{
		return restaurant;
	}

	public Entity toEntity()
	{
		Entity e = new Entity(getKey());
		e.setProperty("restr", restaurant);
		return e;
	}
	public void fromEntity(Entity e)
	{
		restaurant = (String) e.getProperty("restr");
	}
}
