package util;
import java.util.ArrayList;

/**
 * SimpleMap is a very crude Map implementation.
 * It uses the equal function in <tt>Object</tt> only, compare to HashTable or HashMap 
 * 
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class SimpleMap<K,V> {
	private ArrayList<K> keys;
	private ArrayList<V> values; 
	
	/**
	 * The default constructor
	 */
	public SimpleMap()
	{
		keys = new ArrayList<K>();
		values = new ArrayList<V>();
	}
	
	/**
	 * Return the size of the map
	 * @return the size of the map
	 */
	public int size()
	{
		return keys.size();
	}
	
	/**
	 * Returns the value to which the specified key is mapped in this map, or null if the map contains no mapping for this key
	 * 
	 * @param key the key whose associated value is to be returned.
	 * @return the value to which this map maps the specified key, or null if the map contains no mapping for this key.
	 */
	public V get(K key)
	{
		int i = keys.indexOf(key);
		if(i == -1)
			return null;
		
		return values.get(i);
		
	}
	
	/**
	 *  Associates the specified value with the specified key in this map. If the map previously contained a mapping for this key, the old value is replaced.
	 * @param key key with which the specified value is to be associated.
	 * @param value value to be associated with the specified key.
	 * @return previous value associated with specified key, or null if there was no mapping for key.
	 */
	public V put(K key, V value)
	{
		if(value == null || key == null) return null;
		int i = keys.indexOf(key);
		if(i == -1)
		{
			keys.add(key);
			values.add(value);
			return null;
		}
		return values.set(i, value);
	}
	
	/**
	 * Returns <tt>true</tt> if this map contains a mapping for the specified key.
	 * @param key The key whose presence in this map is to be tested
	 * @return <tt>true</tt> if this map contains a mapping for the specified key.
	 */
	public boolean containsKey(K key)
	{
		return keys.contains(key);
	}
	
	/**
	 * Removes the mapping for this key from this map if present.
	 * @param key key whose mapping is to be removed from the map.
	 * @return previous value associated with specified key, or null if there was no mapping for key.
	 */
	public V remove(K key)
	{
		int i = keys.indexOf(key);
		if(i == -1) return null;
		keys.remove(i);
		return values.remove(i);
	}
	
	/**
	 * Removes all mappings from this map.
	 */
	public void clear()
	{
		values.clear();
		keys.clear();
	}
}
