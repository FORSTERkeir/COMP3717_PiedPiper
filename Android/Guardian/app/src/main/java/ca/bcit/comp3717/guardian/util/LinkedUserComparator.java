package ca.bcit.comp3717.guardian.util;

import java.util.Comparator;
import ca.bcit.comp3717.guardian.model.LinkedUser;

public class LinkedUserComparator {

    public static class Ascending implements Comparator<LinkedUser> {
        @Override
        public int compare(LinkedUser o1, LinkedUser o2) {
            return o1.getNameTarget().compareTo(o2.getNameTarget());
        }
    }
}
