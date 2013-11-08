package utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import utils.ServletBase.LoginType;

import javax.servlet.http.HttpServletRequest;

import kinds.AbstractAuthKey;
import kinds.Globals;
import kinds.UserAuthKey;

import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class Loader implements ParamWrapper
{
	private HttpServletRequest req;
	private DatastoreService ds;
	private AbstractAuthKey authKey;

	private List<String> strs;
	private List<List<String>> strLists;
	private List<List<List<String>>> str2DLists;
	private List<Long> longs;
	private List<List<Long>> longLists;
	private List<List<List<Long>>> long2DLists;
	private List<Boolean> bools;
	private List<List<Boolean>> boolLists;
	private List<List<List<Boolean>>> bool2DLists;
	private List<Long> keyIDs;
	private List<List<Long>> keyIDLists;
	private List<List<List<Long>>> keyID2DLists;
	private List<String> keyNames;
	private List<List<String>> keyNameLists;
	private List<List<List<String>>> keyName2DLists;
	private List<Key> keys;
	private List<Entity> entities;
	
	public Loader(HttpServletRequest req)
	{
		init(req, null);
	}
	
	public Loader(HttpServletRequest req, DatastoreService ds)
	{
		init(req, ds);
	}
	
	private void init(HttpServletRequest req, DatastoreService ds)
	{
		this.req = req;
		if(ds != null)
			setDS(ds);
		keys = new ArrayList<Key>();
		entities = new ArrayList<Entity>();
	}

	public void setDS(DatastoreService ds)
	{
		this.ds = ds;
	}

	  ////////////////////////////////////////////////////////////
	 ///////////		0-D Loading Functions		/////////////
	////////////////////////////////////////////////////////////

	private <E> void params(Parser<E> parser, List<E> trgt, String... names) throws HttpErrMsg
	{
		for(String name : names) {
			boolean optional = name.startsWith("?");
			name = optional ? name.substring(1) : name;
			String rawArg = req.getParameter(name);
			trgt.add(optional && rawArg == null ? null : parser.parse(name, rawArg));
		}
	}

	public void strs(String... names) throws HttpErrMsg
	{
		strs = new ArrayList<String>(names.length);
		params(new StrParser(), strs, names);
	}

	public void longs(String... names) throws HttpErrMsg
	{
		longs = new ArrayList<Long>(names.length);
		params(new LongParser(), longs, names);
	}

	public void bools(String... names) throws HttpErrMsg
	{
		bools = new ArrayList<Boolean>(names.length);
		params(new BoolParser(), bools, names);
	}

	public void keyIDs(String... names) throws HttpErrMsg
	{
		keyIDs = new ArrayList<Long>(names.length);
		params(new KeyIDParser(), keyIDs, names);
	}

	public void keyNames(String... names) throws HttpErrMsg
	{
		keyNames = new ArrayList<String>(names.length);
		params(new KeyNameParser(), keyNames, names);
	}

	  ////////////////////////////////////////////////////////////
	 ///////////		1-D Loading Functions		/////////////
	////////////////////////////////////////////////////////////

	private <E> void paramLists(Parser<E> parser, List<List<E>> trgt, String... names) throws HttpErrMsg
	{
		for(String name : names) {
			boolean optional = name.startsWith("?");
			name = optional ? name.substring(1) : name;
			List<String> rawArg = DSConverter.getList(req, name);
			if(rawArg == null) {
				if(optional)
					trgt.add(null);
				else
					throw new HttpErrMsg("Missing parameter "+name);
			} else {
				List<E> arg = new ArrayList<E>(rawArg.size());
				for(int i = 0; i < rawArg.size(); i++)
					arg.add(optional && rawArg.get(i) == null ? null : parser.parse(DSConverter.getIthName(name, i), rawArg.get(i)));
				trgt.add(arg);
			}
		}
	}

	public void strLists(String... names) throws HttpErrMsg
	{
		strLists = new ArrayList<List<String>>(names.length);
		paramLists(new StrParser(), strLists, names);
	}

	public void longLists(String... names) throws HttpErrMsg
	{
		longLists = new ArrayList<List<Long>>(names.length);
		paramLists(new LongParser(), longLists, names);
	}
	
	public void boolLists(String... names) throws HttpErrMsg
	{
		boolLists = new ArrayList<List<Boolean>>(names.length);
		paramLists(new BoolParser(), boolLists, names);
	}

	public void keyIDLists(String... names) throws HttpErrMsg
	{
		keyIDLists = new ArrayList<List<Long>>(names.length);
		paramLists(new KeyIDParser(), keyIDLists, names);
	}
	
	public void keyNameLists(String... names) throws HttpErrMsg
	{
		keyNameLists = new ArrayList<List<String>>(names.length);
		paramLists(new KeyNameParser(), keyNameLists, names);
	}

	  ////////////////////////////////////////////////////////////
	 ///////////		2-D Loading Functions		/////////////
	////////////////////////////////////////////////////////////

	private <E> void param2DLists(Parser<E> parser, List<List<List<E>>> trgt, String... names) throws HttpErrMsg
	{
		for(String name : names) {
			boolean optional = name.startsWith("?");
			name = optional ? name.substring(1) : name;
			List<List<String>> rawArg = DSConverter.get2DList(req, name);
			if(rawArg == null) {
				if(optional)
					trgt.add(null);
				else
					throw new HttpErrMsg("Missing parameter "+name);
			} else {
				List<List<E>> arg = new ArrayList<List<E>>(rawArg.size());
				for(int i = 0; i < rawArg.size(); i++) {
					if(rawArg.get(i) == null) {
						if(optional)
							arg.add(null);
						else
							throw new HttpErrMsg("Sublist "+i+" is missing");
					} else {
						List<E> elem = new ArrayList<E>(rawArg.get(i).size());
						for(int j = 0; j < rawArg.get(i).size(); j++)
							elem.add(optional && rawArg.get(i).get(j) == null ? null :
								parser.parse(DSConverter.getIthName(DSConverter.getIthName(name, i), j), rawArg.get(i).get(j)));
						arg.add(elem);
					}
				}
				trgt.add(arg);
			}
		}
	}

	public void str2DLists(String... names) throws HttpErrMsg
	{
		str2DLists = new ArrayList<List<List<String>>>(names.length);
		param2DLists(new StrParser(), str2DLists, names);
	}

	public void long2DLists(String... names) throws HttpErrMsg
	{
		long2DLists = new ArrayList<List<List<Long>>>(names.length);
		param2DLists(new LongParser(), long2DLists, names);
	}
	
	public void bool2DLists(String... names) throws HttpErrMsg
	{
		bool2DLists = new ArrayList<List<List<Boolean>>>(names.length);
		param2DLists(new BoolParser(), bool2DLists, names);
	}

	public void keyID2DLists(String... names) throws HttpErrMsg
	{
		keyID2DLists = new ArrayList<List<List<Long>>>(names.length);
		param2DLists(new KeyIDParser(), keyID2DLists, names);
	}
	
	public void keyName2DLists(String... names) throws HttpErrMsg
	{
		keyName2DLists = new ArrayList<List<List<String>>>(names.length);
		param2DLists(new KeyNameParser(), keyName2DLists, names);
	}

	  ////////////////////////////////////////////////////////////
	 ///////////	  Other Loading Functions		/////////////
	////////////////////////////////////////////////////////////

	public void path(String... elems) throws HttpErrMsg
	{
		path(true, elems);
	}

	public void path(boolean verify, String... elems) throws HttpErrMsg
	{
		Key key = getAccountKey();
		int i = 0;
		if(elems[0].equals("/")) {
			key = null;
			i++;
		} else while(elems[i].equals("..")) {
			key = key.getParent();
			i++;
		}

		if((elems.length - i) % 2 == 1)
			throw new IllegalArgumentException("Must exactly one kind per ID/name");

		Parser<Long> keyIDParser = new KeyIDParser();
		Parser<String> keyNameParser = new KeyNameParser();
		for(; i < elems.length; i += 2) {
			String kind = elems[i];
			List<String> nameOrIds;
			String pName = elems[i+1];
			boolean isLong = false;
			if(pName == null) {
				nameOrIds = new ArrayList<String>(1);
				nameOrIds.add(""+Globals.defaultID);
				isLong = true;
			} else {
				if(pName.startsWith("#")) {
					pName = pName.substring(1);
					isLong = true;
				}
				if(pName.endsWith("*")) {
					nameOrIds = utils.DSConverter.getList(req, pName.substring(0, pName.length()-1));
					if(nameOrIds == null)
						throw new HttpErrMsg("Invalid list in path: "+pName.substring(0, pName.length()-1));
				} else {
					nameOrIds = new ArrayList<String>(1);
					nameOrIds.add(req.getParameter(pName));
				}
			}
			for(int j = 0; j < nameOrIds.size(); j++) {
				if(isLong)
					key = KeyFactory.createKey(key, kind, keyIDParser.parse(pName, nameOrIds.get(j)));
				else
					key = KeyFactory.createKey(key, kind, keyNameParser.parse(pName, nameOrIds.get(j)));
				if(verify && (i+2 < elems.length || j+1 < nameOrIds.size()))
					try {
						ds.get(key);
					} catch (EntityNotFoundException e) {
						throw new HttpErrMsg("Key \""+key+"\" does not match with anything in the database");
					}
			}
		}
		keys.add(key);
	}

	  ///////////////////////////////////////////////////////////
	 /////	Other Public Functions Additional Requirements	////
	///////////////////////////////////////////////////////////

	public void login(LoginType loginType) throws HttpErrMsg
	{
		String httpPN;
		String authKeyKind;
		switch(loginType) {
			case USER:
				httpPN = UserAuthKey.httpPN;
				authKeyKind = UserAuthKey.getKind();
				break;
			default:
				throw new HttpErrMsg(500, "Unknown Login Type");
		}
		String key = (String) req.getSession().getAttribute(httpPN);
		if(key == null)
			throw new HttpErrMsg(401, "Must be logged in");
		else try {
			authKey = AbstractAuthKey.build(
						DatastoreServiceFactory.getDatastoreService().get(
								KeyFactory.createKey(authKeyKind, key)));
			if(authKey.getExprDate().before(new Date()))
				throw new HttpErrMsg(401, "Login expired");
			else {
				authKey.updateExpr();
				authKey.commit(DatastoreServiceFactory.getDatastoreService());
			}
		} catch (EntityNotFoundException e) {
			throw new HttpErrMsg(401, "Login Invalid");
		}
	}

	public void dne() throws HttpErrMsg {dne(0);}
	public void dne(int i) throws HttpErrMsg
	{
		Key k = keys.get(i);
		try {
			ds.get(k);
			throw new HttpErrMsg("Entity with key \""+k+"\" already exists");
		} catch(EntityNotFoundException e) {}
	}

	public void exists() throws HttpErrMsg {exists(0);}
	public void exists(int i) throws HttpErrMsg
	{
		Key k = keys.get(i);
		try {
			Entity e = ds.get(k);
			if(entities.size() > i)
				entities.set(i, e);
			else {
				while(entities.size() < i)
					entities.add(null);
				entities.add(e);
			}
		} catch(EntityNotFoundException e) {
			throw new HttpErrMsg("Entity with key \""+k+"\" does not exist");
		}
	}

	  ///////////////////////////////////////////////////////////
	 //////////////		Getters Functions		////////////////
	///////////////////////////////////////////////////////////

	public String getStr(int i)
	{
		return strs.get(i);
	}

	public List<String> getStrList(int i)
	{
		return strLists.get(i);
	}

	public List<List<String>> getStr2DList(int i)
	{
		return str2DLists.get(i);
	}

	public Long getLong(int i)
	{
		return longs.get(i);
	}

	public List<Long> getLongList(int i)
	{
		return longLists.get(i);
	}

	public List<List<Long>> getLong2DList(int i)
	{
		return long2DLists.get(i);
	}

	public Boolean getBool(int i)
	{
		return bools.get(i);
	}

	public List<Boolean> getBoolList(int i)
	{
		return boolLists.get(i);
	}

	public List<List<Boolean>> getBool2DList(int i)
	{
		return bool2DLists.get(i);
	}

	public Long getKeyID(int i)
	{
		return keyIDs.get(i);
	}

	public List<Long> getKeyIDList(int i)
	{
		return keyIDLists.get(i);
	}

	public List<List<Long>> getKeyID2DList(int i)
	{
		return keyID2DLists.get(i);
	}

	public String getKeyName(int i)
	{
		return keyNames.get(i);
	}

	public List<String> getKeyNameList(int i)
	{
		return keyNameLists.get(i);
	}

	public List<List<String>> getKeyName2DList(int i)
	{
		return keyName2DLists.get(i);
	}

	public Key getKey() {return getKey(0);}
	public Key getKey(int i)
	{
		return keys.get(i);
	}

	public Entity getEntity() {return getEntity(0);}
	public Entity getEntity(int i)
	{
		return entities.get(i);
	}

	public Key getAccountKey()
	{
		if(authKey == null)
			return null;
		return authKey.getAccountKey();
	}

	public AbstractAuthKey getAuthKey()
	{
		return authKey;
	}

	public String getQueryString()
	{
		return req.getQueryString();
	}

	public String getClientID() throws IOException
	{
		return ChannelServiceFactory.getChannelService().parsePresence(req).clientId();
	}

	  ///////////////////////////////////////////////////////////
	 ///////////////		Parse Functions		////////////////
	///////////////////////////////////////////////////////////

	//These check that the string is of the correct form (if necessary)
	//and then parse it (if necessary)
	
	private static interface Parser<E> {
		public E parse(String name, String val) throws HttpErrMsg;
	}

	private class StrParser implements Parser<String>
	{
		public String parse(String name, String val) throws HttpErrMsg
		{
			if(val == null)
				throw new HttpErrMsg("Parameter \""+name+"\" is missing");
			return val;
		}
	}

	private class LongParser implements Parser<Long>
	{
		public Long parse(String name, String val) throws HttpErrMsg
		{
			if(val == null)
				throw new HttpErrMsg("Parameter \""+name+"\" is missing");
			try {
				return Long.parseLong(val);
			} catch(NumberFormatException e) {
				throw new HttpErrMsg("Parameter \""+name+"\" cannot be parsed into a long");
			}
		}
	}

	private class BoolParser implements Parser<Boolean>
	{
		public Boolean parse(String name, String val) throws HttpErrMsg
		{
			if(val == null)
				throw new HttpErrMsg("Parameter \""+name+"\" is missing");
			if(val.equalsIgnoreCase("t"))
				return true;
			else if(val.equalsIgnoreCase("f"))
				return false;
			else if(val.equalsIgnoreCase("true"))
				return true;
			else if(val.equalsIgnoreCase("false"))
				return false;
			else try {
				return Long.parseLong(val) != 0;
			} catch(NumberFormatException e) {
				throw new HttpErrMsg("Parameter \""+name+"\" cannot be parsed into a boolean");
			}
		}
	}

	private class KeyIDParser implements Parser<Long>
	{
		public Long parse(String name, String val) throws HttpErrMsg
		{
			if(val == null)
				throw new HttpErrMsg("Parameter \""+name+"\" is missing");
			long id;
			try {
				id = Long.parseLong(val);
			} catch(NumberFormatException e) {
				throw new HttpErrMsg("Parameter \""+name+"\" cannot be parsed into a long");
			}
			if(id == 0L)
				throw new HttpErrMsg("Parameter \""+name+"\" cannot be zero");
			return id;
		}
	}

	private class KeyNameParser implements Parser<String>
	{
		public String parse(String name, String val) throws HttpErrMsg
		{
			if(val == null)
				throw new HttpErrMsg("Parameter \""+name+"\" is missing");
			if(val.length() == 0)
				throw new HttpErrMsg("Parameter \""+name+"\" is of zero-length");
			return val;
		}
	}
}