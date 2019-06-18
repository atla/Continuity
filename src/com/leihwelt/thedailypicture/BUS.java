package com.leihwelt.thedailypicture;

import com.squareup.otto.Bus;

public enum BUS {
    INSTANCE;

    private Bus bus = new Bus();

    public Bus get() {
        return bus;
    }

    public void post(Object message) {
        bus.post(message);
    }

}
