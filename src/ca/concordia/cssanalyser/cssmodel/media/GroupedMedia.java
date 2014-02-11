package ca.concordia.cssanalyser.cssmodel.media;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GroupedMedia extends Media implements Iterable<SingleMedia> {

	private final List<SingleMedia> listOfAtomicMedia;

	public GroupedMedia() {
		this(-1, -1);
	}

	public GroupedMedia(int line, int coloumn) {
		super(line, coloumn);
		listOfAtomicMedia = new ArrayList<SingleMedia>();
	}

	public void addMedia(String name) {
		listOfAtomicMedia.add(new SingleMedia(name));
	}

	public void addMedia(SingleMedia media) {
		listOfAtomicMedia.add(media);
	}

	public void addAllMedia(GroupedMedia groupedMedia) {
		for (SingleMedia atomicMedia : groupedMedia) {
			addMedia(atomicMedia);
		}
	}
	
	public void removeMedia(String item) {
		for (int i = 0; i < listOfAtomicMedia.size(); i++) {
			SingleMedia atomicMedia = listOfAtomicMedia.get(i);
			if (atomicMedia.getMediaName().equals(item))
				listOfAtomicMedia.remove(i);
		}
	}
	
	public int size() {
		return listOfAtomicMedia.size();
	}


	public SingleMedia getAtomicMedia(int index) {
		return listOfAtomicMedia.get(index);
	}

	@Override
	public Iterator<SingleMedia> iterator() {
		return listOfAtomicMedia.iterator();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof GroupedMedia))
			return false;
		GroupedMedia otherObj = (GroupedMedia) obj;
		if (listOfAtomicMedia.size() == otherObj.listOfAtomicMedia.size()) {
			for (SingleMedia atomicMediaFromMe : listOfAtomicMedia)
				if (!otherObj.listOfAtomicMedia.contains(atomicMediaFromMe))
					return false;
			return true;
		} else {
			return false;
		}
	}
	
	public GroupedMedia clone() {
		GroupedMedia newOne = new GroupedMedia();
		for (SingleMedia media : listOfAtomicMedia) {
			newOne.addMedia(media.getMediaName());
		}
		return newOne;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
		// TODO later;
	}

	@Override
	public String toString() {
		String result = "";
		for (SingleMedia atomicMedia : listOfAtomicMedia)
			result += atomicMedia + ", ";
		result = result.substring(0, result.length() - 2);
		return result;
	}
}
