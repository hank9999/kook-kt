package com.github.hank9999.kook.connect

class Type {
    enum class Status {
        CONNECTING,
        CONNECTED,
        INIT,
        RECONNECT,
        CLOSED
    }
}