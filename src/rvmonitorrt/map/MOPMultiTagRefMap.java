package rvmonitorrt.map;

import rvmonitorrt.map.hashentry.MOPHashRefEntry;
import rvmonitorrt.ref.MOPMultiTagWeakReference;
import rvmonitorrt.ref.MOPTagWeakReference;
import rvmonitorrt.ref.MOPWeakReference;

public class MOPMultiTagRefMap extends MOPBasicRefMap {
	int taglen = 1;
	
	static public MOPMultiTagWeakReference NULRef = new MOPMultiTagWeakReference(1, null);

	protected MOPMultiTagWeakReference cachedValue = NULRef;

	protected MOPMultiTagWeakReference[] cachedValue2 = new MOPMultiTagWeakReference[ref_locality_cache_size];

	public MOPMultiTagRefMap() {
		super();
		
		for (int i = 0; i < ref_locality_cache_size; i++) {
			cachedValue2[i] = NULRef;
		}
	}

	public MOPMultiTagRefMap(int taglen) {
		super();
		
		for (int i = 0; i < ref_locality_cache_size; i++) {
			cachedValue2[i] = NULRef;
		}

		this.taglen = taglen;
	}
	
	@Override
	public MOPMultiTagWeakReference getMultiTagRef(Object key, int joinPointId) {
		if (key == cachedKey && cachedValue != NULRef) {
			return cachedValue;
		}

		int cacheIndex = joinPointId & (ref_locality_cache_size - 1);

		if (key == cachedKey2[cacheIndex] && cachedValue2[cacheIndex] != NULRef) {
			cachedKey = cachedKey2[cacheIndex];
			cachedValue = cachedValue2[cacheIndex];
			return cachedValue;
		}

		cachedKey = key;
		cachedKey2[cacheIndex] = key;

		MOPHashRefEntry[] data = this.data;

		int hashCode = System.identityHashCode(key);
		int index = hashIndex(hashCode, data.length);
		MOPHashRefEntry entry = data[index];

		while (entry != null) {
			if (key == entry.key.get()) {
				cachedValue = (MOPMultiTagWeakReference) entry.key;
				cachedValue2[cacheIndex] = (MOPMultiTagWeakReference) entry.key;

				return cachedValue;
			}
			entry = entry.next;
		}

		// create new weakreference
		MOPMultiTagWeakReference keyref = new MOPMultiTagWeakReference(taglen, key, hashCode);
		cachedValue = keyref;

		for (int i = 0; i < ref_locality_cache_size; i++) {
			if (cachedKey2[i] == key)
				cachedValue2[i] = keyref;
		}

		if (multicore && data.length > DEFAULT_THREADED_CLEANUP_THREASHOLD) {
			putIndex = hashIndex(hashCode, data.length);

			while (this.newdata != null) {
				putIndex = -1;
				while (this.newdata != null) {
					Thread.yield();
				}
				data = this.data;
				putIndex = hashIndex(hashCode, data.length);
			}

			while (cleanIndex == putIndex) {
				Thread.yield();
			}

			MOPHashRefEntry newentry = new MOPHashRefEntry(data[putIndex], keyref);
			data[putIndex] = newentry;
			addedMappings++;

			putIndex = -1;

			if (!isCleaning && this.nextInQueue == null && addedMappings - deletedMappings >= data.length / 2 && addedMappings - deletedMappings - lastsize > data.length / 10) {
				this.isCleaning = true;
				if (MOPMapManager.treeQueueTail == this) {
					this.repeat = true;
				} else {
					MOPMapManager.treeQueueTail.nextInQueue = this;
					MOPMapManager.treeQueueTail = this;
				}
			}
		} else {
			MOPHashRefEntry newentry = new MOPHashRefEntry(data[index], keyref);
			data[index] = newentry;
			addedMappings++;

			if (multicore)
				checkCapacityNoOneIter();
			else
				checkCapacity();
		}

		return keyref;
	}

	@Override
	public MOPMultiTagWeakReference getMultiTagRefNonCreative(Object key, int joinPointId) {
		if (key == cachedKey) {
			return cachedValue;
		}

		int cacheIndex = joinPointId & (ref_locality_cache_size - 1);

		if (key == cachedKey2[cacheIndex]) {
			cachedKey = cachedKey2[cacheIndex];
			cachedValue = cachedValue2[cacheIndex];
			return cachedValue;
		}

		cachedKey = key;
		cachedKey2[cacheIndex] = key;

		MOPHashRefEntry[] data = this.data;

		int hashCode = System.identityHashCode(key);
		int index = hashIndex(hashCode, data.length);
		MOPHashRefEntry entry = data[index];

		while (entry != null) {
			if (key == entry.key.get()) {
				cachedValue = (MOPMultiTagWeakReference) entry.key;
				cachedValue2[cacheIndex] = (MOPMultiTagWeakReference) entry.key;

				return cachedValue;
			}
			entry = entry.next;
		}
		cachedValue = NULRef;
		cachedValue2[cacheIndex] = NULRef;

		return NULRef;
	}

	@Override
	public MOPMultiTagWeakReference getMultiTagRef(Object key) {
		if (key == cachedKey && cachedValue != NULRef) {
			return cachedValue;
		}

		cachedKey = key;

		MOPHashRefEntry[] data = this.data;

		int hashCode = System.identityHashCode(key);
		int index = hashIndex(hashCode, data.length);
		MOPHashRefEntry entry = data[index];

		while (entry != null) {
			if (key == entry.key.get()) {
				cachedValue = (MOPMultiTagWeakReference) entry.key;

				return cachedValue;
			}
			entry = entry.next;
		}

		// create new weakreference
		MOPMultiTagWeakReference keyref = new MOPMultiTagWeakReference(taglen, key, hashCode);
		cachedValue = keyref;

		if (multicore && data.length > DEFAULT_THREADED_CLEANUP_THREASHOLD) {
			putIndex = hashIndex(hashCode, data.length);

			while (this.newdata != null) {
				putIndex = -1;
				while (this.newdata != null) {
					Thread.yield();
				}
				data = this.data;
				putIndex = hashIndex(hashCode, data.length);
			}

			while (cleanIndex == putIndex) {
				Thread.yield();
			}

			MOPHashRefEntry newentry = new MOPHashRefEntry(data[putIndex], keyref);
			data[putIndex] = newentry;
			addedMappings++;

			putIndex = -1;

			if (!isCleaning && this.nextInQueue == null && addedMappings - deletedMappings >= data.length / 2 && addedMappings - deletedMappings - lastsize > data.length / 10) {
				this.isCleaning = true;
				if (MOPMapManager.treeQueueTail == this) {
					this.repeat = true;
				} else {
					MOPMapManager.treeQueueTail.nextInQueue = this;
					MOPMapManager.treeQueueTail = this;
				}
			}
		} else {
			MOPHashRefEntry newentry = new MOPHashRefEntry(data[index], keyref);
			data[index] = newentry;
			addedMappings++;

			if (multicore)
				checkCapacityNoOneIter();
			else
				checkCapacity();
		}

		return keyref;
	}

	@Override
	public MOPMultiTagWeakReference getMultiTagRefNonCreative(Object key) {
		if (key == cachedKey) {
			return cachedValue;
		}

		cachedKey = key;

		MOPHashRefEntry[] data = this.data;

		int hashCode = System.identityHashCode(key);
		int index = hashIndex(hashCode, data.length);
		MOPHashRefEntry entry = data[index];

		while (entry != null) {
			if (key == entry.key.get()) {
				cachedValue = (MOPMultiTagWeakReference) entry.key;

				return cachedValue;
			}
			entry = entry.next;
		}
		cachedValue = NULRef;

		return NULRef;
	}
}