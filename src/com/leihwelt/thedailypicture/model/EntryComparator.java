package com.leihwelt.thedailypicture.model;

import java.util.Comparator;

public class EntryComparator implements Comparator<Entry> {

    @Override
    public int compare(Entry lhs, Entry rhs) {

        // 0 should never arise as we only have ONE entry per day
        if (lhs.getDate().isBefore(rhs.getDate())) {
            return 1;
        } else {
            return -1;
        }
    }

}
