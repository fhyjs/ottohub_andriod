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
    public static final ThemeData BILI = new ThemeData("bili",0xFFfb7299,0xFFa34a63,0xFF0d090a,0xFF666666,0xFFfff0f4,0xFFff94b2);
    public static final ThemeData PURPLE_MD2 = new ThemeData(
            "purple_md2",       // name
            0xFF9C27B0,         // colorPrimary
            0xFF7B1FA2,         // colorPrimaryVariant
            0xFF0c0c0c,         // colorOnPrimary
            0xFFE1BEE7,         // colorOnPrimarySecond
            0xFFF3E5F5,         // colorBackground
            0xFF673AB7          // colorActionBar
    );
    public static final ThemeData CHINA_RED = new ThemeData(
            "china_red",        // name
            0xFFE53935,         // colorPrimary
            0xFFC62828,         // colorPrimaryVariant
            0xFF0c0c0c,         // colorOnPrimary
            0xFFB71C1C,         // colorOnPrimarySecond
            0xFFFFEBEE,         // colorBackground
            0xFFD32F2F          // colorActionBar
    );
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
