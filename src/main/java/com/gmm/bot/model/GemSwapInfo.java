package com.gmm.bot.model;

import com.gmm.bot.enumeration.GemModifier;
import com.gmm.bot.enumeration.GemType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.stream.Collectors;


@Getter
@Setter
public class GemSwapInfo {
    private int index1;
    private int index2;
    private int sizeMatch;
    private GemType type;
    private Set<GemModifier> modifiers;

    public GemSwapInfo(int index1, int index2, int sizeMatch, GemType type, Set<GemModifier> modifiers) {
        this.index1 = index1;
        this.index2 = index2;
        this.sizeMatch = sizeMatch;
        this.type = type;
        this.modifiers = modifiers;
    }

    public Pair<Integer> getIndexSwapGem() {
        return new Pair<>(index1, index2);
    }
    public boolean checkType(GemType gemType){
        return  type.equals(gemType);
    }

    public boolean checkModifier(GemModifier modifier){
        return modifiers.contains(modifier);
    }
}
