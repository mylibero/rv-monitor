package rvmonitorrt.map;

import rvmonitorrt.RVMMonitor;
import rvmonitorrt.map.hashentry.RVMHashEntry;
import rvmonitorrt.ref.RVMWeakReference;

public class RVMMapOfMonitor extends RVMAbstractMapSolo {

	public RVMMapOfMonitor(int idnum) {
		super();
		this.idnum = idnum;
	}

	@Override
	public Object getNode(RVMWeakReference key) {
		return get_1(key);
	}

	@Override
	public boolean putNode(RVMWeakReference key, Object value) {
		return put_1(key, value);
	}

	/* ************************************************************************************ */

	@Override
	protected void endObject(int idnum) {
		this.isDeleted = true;
		for (int i = data.length - 1; i >= 0; i--) {
			RVMHashEntry entry = data[i];
			data[i] = null;
			while (entry != null) {
				RVMHashEntry next = entry.next;

				RVMMonitor monitor = (RVMMonitor) entry.value;
				if (!monitor.RVM_terminated)
					monitor.endObject(idnum);

				entry.next = null;
				entry = next;
			}
		}

		this.deletedMappings = this.addedMappings;
	}

	@Override
	protected void cleanupchunkiter() {
		if (cleancursor < 0)
			cleancursor = data.length - 1;

		for (int i = 0; i < cleanup_piece && cleancursor >= 0; i++) {
			RVMHashEntry previous = null;
			RVMHashEntry entry = data[cleancursor];

			while (entry != null) {
				RVMHashEntry next = entry.next;

				RVMMonitor monitor = (RVMMonitor) entry.value;

				if (entry.key.get() == null) {
					if (previous == null) {
						data[cleancursor] = entry.next;
					} else {
						previous.next = entry.next;
					}

					if (!monitor.RVM_terminated)
						monitor.endObject(idnum);

					entry.next = null;
					this.deletedMappings++;
				} else if (monitor.RVM_terminated) {
					if (previous == null) {
						data[cleancursor] = entry.next;
					} else {
						previous.next = entry.next;
					}

					entry.next = null;
					this.deletedMappings++;
				} else {
					previous = entry;
				}
				entry = next;
			}
			cleancursor--;
		}
	}

	@Override
	protected void cleanupiter() {
		for (int i = data.length - 1; i >= 0; i--) {
			RVMHashEntry entry = data[i];
			RVMHashEntry previous = null;
			while (entry != null) {
				RVMHashEntry next = entry.next;

				RVMMonitor monitor = (RVMMonitor) entry.value;

				if (entry.key.get() == null) {
					if (previous == null) {
						data[i] = entry.next;
					} else {
						previous.next = entry.next;
					}

					if (!monitor.RVM_terminated)
						monitor.endObject(idnum);

					entry.next = null;
					this.deletedMappings++;
				} else if (monitor.RVM_terminated) {
					if (previous == null) {
						data[i] = entry.next;
					} else {
						previous.next = entry.next;
					}

					entry.next = null;
					this.deletedMappings++;
				} else {
					previous = entry;
				}
				entry = next;
			}
		}
	}
}