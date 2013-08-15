package CSSModel.media;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GroupedMedia extends Media implements Iterable<AtomicMedia> {

	private final List<AtomicMedia> listOfAtomicMedia;

	public GroupedMedia() {
		this(-1, -1);
	}

	public GroupedMedia(int line, int coloumn) {
		super(line, coloumn);
		listOfAtomicMedia = new ArrayList<AtomicMedia>();
	}

	public void addMedia(String name) {
		listOfAtomicMedia.add(new AtomicMedia(name));
	}

	public void addMedia(AtomicMedia media) {
		listOfAtomicMedia.add(media);
	}

	public void addAllMedia(GroupedMedia groupedMedia) {
		for (AtomicMedia atomicMedia : groupedMedia) {
			addMedia(atomicMedia);
		}
	}
	
	public void removeMedia(String item) {
		for (int i = 0; i < listOfAtomicMedia.size(); i++) {
			AtomicMedia atomicMedia = listOfAtomicMedia.get(i);
			if (atomicMedia.getMediaName().equals(item))
				listOfAtomicMedia.remove(i);
		}
	}
	
	public int size() {
		return listOfAtomicMedia.size();
	}


	public AtomicMedia getAtomicMedia(int index) {
		return listOfAtomicMedia.get(index);
	}

	@Override
	public Iterator<AtomicMedia> iterator() {
		return listOfAtomicMedia.iterator();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof GroupedMedia))
			return false;
		GroupedMedia otherObj = (GroupedMedia) obj;
		if (listOfAtomicMedia.size() == otherObj.listOfAtomicMedia.size()) {
			for (AtomicMedia atomicMediaFromMe : listOfAtomicMedia)
				if (!otherObj.listOfAtomicMedia.contains(atomicMediaFromMe))
					return false;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return super.hashCode();
		// TODO later;
	}

	@Override
	public String toString() {
		String result = "";
		for (AtomicMedia atomicMedia : listOfAtomicMedia)
			result += atomicMedia + ", ";
		result = result.substring(0, result.length() - 2);
		return result;
	}
}
