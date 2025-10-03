package org.eu.hanana.reimu.ottohub_andriod.data.base.text;

import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class TextCard {
    public final String text;
    @Setter
    public Object extra;
}
