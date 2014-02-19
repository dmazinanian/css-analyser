package ca.concordia.cssanalyser.cssmodel.media;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class MediaQueryList implements Iterable<MediaQuery> {

	private final Set<MediaQuery> listOfMediaQueries;

	public MediaQueryList() {
		listOfMediaQueries = new LinkedHashSet<MediaQuery>();
	}

	public void addMediaQuery(MediaQuery mediaQuery) {
		listOfMediaQueries.add(mediaQuery);
	}

	public void addAllMediaQueries(MediaQueryList otherList) {
		for (MediaQuery atomicMedia : otherList) {
			addMediaQuery(atomicMedia);
		}
	}
	
	public void removeMediaQuery(MediaQuery media) {
		listOfMediaQueries.remove(media);
	}
	
	public void removeAllMediaQueries(MediaQueryList mediaQueryList) {
		for (MediaQuery mediaQuery : mediaQueryList)
			listOfMediaQueries.remove(mediaQuery);
	}
	
	public int size() {
		return listOfMediaQueries.size();
	}


	@Override
	public Iterator<MediaQuery> iterator() {
		return listOfMediaQueries.iterator();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((listOfMediaQueries == null) ? 0 : listOfMediaQueries
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MediaQueryList other = (MediaQueryList) obj;
		if (listOfMediaQueries == null) {
			if (other.listOfMediaQueries != null)
				return false;
		} else if (!listOfMediaQueries.equals(other.listOfMediaQueries))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder toReturn = new StringBuilder("@media ");
		for (Iterator<MediaQuery> iterator = listOfMediaQueries.iterator(); iterator.hasNext();) {
			MediaQuery query = iterator.next();
			toReturn.append(query);
			if (iterator.hasNext())
				toReturn.append(", ");
		}
		
		return toReturn.toString();
	}
	
	public MediaQueryList clone() {
		MediaQueryList newOne = new MediaQueryList();
		for (MediaQuery query : listOfMediaQueries) {
			newOne.addMediaQuery(query);
		}
		return newOne;
	}
}
