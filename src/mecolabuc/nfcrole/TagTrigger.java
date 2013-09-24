package mecolabuc.nfcrole;

import java.util.ArrayList;
import java.util.List;

public class TagTrigger {

	static TagTrigger singleton;
	List<Triggerable> Trigger;

	public static TagTrigger getTagTrigger() {
		if (singleton == null) {
			singleton = new TagTrigger();
		}
		return singleton;
	}

	private TagTrigger() {
		Trigger = new ArrayList<Triggerable>();
	};

	public void register(Triggerable o) {
		Trigger.add(o);
	}

	public void unregister(Triggerable o) {
		Trigger.remove(o);
	}

	public void trigger(String cardID) {
		for (int i = 0; i < Trigger.size(); i++) {
			Trigger.get(i).Trigger(cardID);
		}
	}

}
