package com.nullsink.domob;

import java.util.Comparator;
import com.nullsink.domob.objects.Album;

public class AlbumComparator implements Comparator<Album> {
  @Override
  public int compare(Album a1, Album a2) {
      return a2.getYear().compareTo(a1.getYear());
  }
}
