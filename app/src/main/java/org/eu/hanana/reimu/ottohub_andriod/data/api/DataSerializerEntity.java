package org.eu.hanana.reimu.ottohub_andriod.data.api;

import com.google.gson.JsonElement;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DataSerializerEntity {
    public final String className;
    public final JsonElement data;
}
