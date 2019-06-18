package com.leihwelt.thedailypicture.model;

import java.util.Iterator;
import java.util.LinkedHashSet;

public class DiaryList extends LinkedHashSet<Diary> {

    private static final long serialVersionUID = 1281690821973494621L;

    public DiaryList() {
    }

    public Diary get(String lastSelectedDiary) {

        Iterator<Diary> iterator = this.iterator();

        while (iterator.hasNext()) {

            Diary d = iterator.next();

            if (d.getLabel().equals(lastSelectedDiary))
                return d;

        }

        return null;
    }

}
