package org.eu.hanana.reimu.ottohub_andriod.util;

import androidx.annotation.Nullable;

import org.eu.hanana.reimu.lib.ottohub.api.profile.ProfileResult;
import org.eu.hanana.reimu.lib.ottohub.api.user.UserResult;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassUtil {
    public static <T,E> void copyFields(Class<T> targetC, Class<E> srcC,T t,E s,boolean tryAccess) throws IllegalAccessException {
        var fields = getAllFields(targetC);
        for (Field fieldTarget : fields) {
            Field fieldSrc;
            fieldSrc = getFieldByName(srcC,fieldTarget.getName());
            if (fieldSrc==null) continue;
            if (tryAccess){
                fieldTarget.setAccessible(true);
                fieldSrc.setAccessible(true);
            }
            fieldTarget.set(t,fieldSrc.get(s));
        }
    }
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            Field[] declaredFields = clazz.getDeclaredFields();
            fields.addAll(Arrays.asList(declaredFields));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
    @Nullable
    public static Field getFieldByName(Class<?> clazz, String name) {
        while (clazz != null && clazz != Object.class) {
            try {
                // 只查当前类
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass(); // 往父类查
            }
        }
        return null; // 没找到
    }
}
