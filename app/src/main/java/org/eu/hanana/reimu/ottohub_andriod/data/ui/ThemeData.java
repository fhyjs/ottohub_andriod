package org.eu.hanana.reimu.ottohub_andriod.data.ui;

import androidx.annotation.ColorInt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ThemeData {
    public static final ThemeData DEFAULT = new ThemeData("default",0xFF81D4FA,0xFF018786,0xFF0a0a0b,0xFF666666,0xFFfefefe,0xFFf3edf7);
    public static final ThemeData DARK = new ThemeData("dark",0xFFff9800,0xFFb56a02,0xFFfdfdfd,0xFF767676,0xFF0a0a0a,0xFF212121);
    public static final ThemeData SANAE = new ThemeData("sanae",0xFF8bc24b,0xFF56782e,0xFF0a0a0b,0xFF666666,0xFFd4dec8,0xFFa8de8c);
    private String name;
    @ColorInt
    private int colorPrimary;
    @ColorInt
    private int colorPrimaryVariant;
    @ColorInt
    private int colorOnPrimary;
    @ColorInt
    private int colorOnPrimarySecond;
    @ColorInt
    private int colorBackground;
    @ColorInt
    private int colorActionBar;
}
