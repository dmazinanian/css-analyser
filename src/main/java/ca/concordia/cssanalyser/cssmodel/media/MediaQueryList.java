package ca.concordia.cssanalyser.cssmodel.media;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.CSSModelObject;

public class MediaQueryList extends CSSModelObject implements Iterable<MediaQuery> {

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


	/**
	 * Compares two MediaQueryList's only based on
	 * their media queries and {@link MediaQuery#mediaQueryEquals()},
	 * The order of media queries is not important.
	 * @param otherMediaQuery
	 * @return
	 */
	public boolean mediaQueryListEquals(MediaQueryList otherMediaQueryList) {
		if (listOfMediaQueries == null) {
			if (otherMediaQueryList.listOfMediaQueries != null)
				return false;
		} else {
			Set<MediaQuery> alreadyMatchedQueryInOther = new HashSet<>();
			if (listOfMediaQueries.size() != otherMediaQueryList.listOfMediaQueries.size())
				return false;
			for (MediaQuery mediaQuery : listOfMediaQueries) {
				boolean found = false;
				for (MediaQuery mediaQuery2 : otherMediaQueryList.listOfMediaQueries) {
					if (!alreadyMatchedQueryInOther.contains(mediaQuery2) &&
							mediaQuery.mediaQueryEquals(mediaQuery2)) {
						alreadyMatchedQueryInOther.add(mediaQuery2);
						found = true;
						break;
					}
				}
				if (!found)
					return false;
			}
		}
		return true;
	}

	/**
	 * Compute hashCode from
	 * their media queries and {@link MediaQuery#mediaQueryHashCode()},
	 * The order of media queries is not important.
	 * @return
	 */
	public int mediaQueryListHashCode() {
		if (listOfMediaQueries == null)
            return 0;
        else {
            int hashCode = 0;
			for (MediaQuery mediaQuery : listOfMediaQueries) {
                hashCode += mediaQuery.mediaQueryHashCode();
			}
            return hashCode;
		}
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
