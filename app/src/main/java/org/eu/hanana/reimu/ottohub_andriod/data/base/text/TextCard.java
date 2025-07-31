package org.eu.hanana.reimu.ottohub_andriod.data.base.text;

import org.eu.hanana.reimu.lib.ottohub.api.im.MessageResult;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class TextCard {
    public final String text;
    @Setter
    public Object extra;
}
