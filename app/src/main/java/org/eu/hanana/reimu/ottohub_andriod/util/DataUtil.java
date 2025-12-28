package org.eu.hanana.reimu.ottohub_andriod.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataUtil {
    public static <T> List<T> subListSafe(List<T> list, int offset, int num) {
        if (list == null || list.isEmpty() || num <= 0) {
            return Collections.emptyList();
        }

        int size = list.size();

        if (offset < 0) offset = 0;
        if (offset >= size) return Collections.emptyList();

        int end = Math.min(offset + num, size);

        return new ArrayList<>(list.subList(offset, end));
    }
}
