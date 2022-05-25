package com.gmm.bot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pair<T> {
    private T param1;
    private T param2;
    private  boolean isNull;

    public Pair(T param1, T param2) {
        this.param1 = param1;
        this.param2 = param2;
        this.isNull = false;
    }

    public Pair(boolean isNull) {
        this.isNull = isNull;
    }
}
