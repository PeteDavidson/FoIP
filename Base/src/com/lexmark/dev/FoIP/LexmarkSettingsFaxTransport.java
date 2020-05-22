package com.lexmark.dev.FoIP;

/**
 * Created by Pete on 4/5/2018.
 */
public enum LexmarkSettingsFaxTransport {
    ANALOG("0"),
    T38("1"),
    G711("2"),
    UNUSED("3"),
    HTTPS("4");

    private final String text;

    LexmarkSettingsFaxTransport(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
