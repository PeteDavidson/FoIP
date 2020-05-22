package com.lexmark.dev.FoIP;

/**
 * Created by Pete on 3/31/2018.
 */
public class UIOutputHandler {
    private static UIOutputHandler instance = null;
    private UIOutput Handler;

    private UIOutputHandler() {
    }

    public static UIOutputHandler getInstance() {
        if (instance == null) {
            instance = new UIOutputHandler();
        }
        return instance;
    }

    public void SetHandler(UIOutput UIO) {
        Handler = UIO;
    }

    public UIOutput GetHandler() {
        return Handler;
    }
}

