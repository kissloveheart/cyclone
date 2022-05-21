package com.gmm.bot.model;

import com.gmm.bot.enumeration.GemModifier;
import com.gmm.bot.enumeration.GemType;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Setter
public class Grid {
    private static final List<GemModifier> MODIFIER_LIST = Arrays.asList(GemModifier.EXPLODE_SQUARE, GemModifier.EXPLODE_HORIZONTAL, GemModifier.EXPLODE_VERTICAL);
    private static final List<GemModifier> MODIFIER_LIST_ALL = Arrays.asList(GemModifier.EXPLODE_VERTICAL, GemModifier.MANA,GemModifier.BUFF_ATTACK,GemModifier.HIT_POINT);
    private List<Gem> gems = new ArrayList<>();
    private Set<GemType> gemTypes = new HashSet<>();
    private List<GemType> myHeroGemType;
    private List<GemSwapInfo> listMatchGem;


    public Grid(ISFSArray gemsCode) {
        updateGems(gemsCode, null);
    }

    public void updateGems(ISFSArray gemsCode, ISFSArray gemModifiers) {
        gems.clear();
        gemTypes.clear();
        if (gemModifiers != null) {
            for (int i = 0; i < gemsCode.size(); i++) {
                Gem gem = new Gem(i, GemType.from(gemsCode.getByte(i)), GemModifier.from(gemModifiers.getByte(i)));
                gems.add(gem);
                gemTypes.add(gem.getType());
            }
        } else {
            for (int i = 0; i < gemsCode.size(); i++) {
                Gem gem = new Gem(i, GemType.from(gemsCode.getByte(i)));
                gems.add(gem);
                gemTypes.add(gem.getType());
            }
        }
        listMatchGem = suggestMatch();
        log.info("List match gem:" +listMatchGem.size());
    }

    public Pair<Integer> recommendSwap5Gem() {
        if (listMatchGem.isEmpty()) {
            return new Pair<>(true);
        }
        // check modifier extra turn
        Optional<GemSwapInfo> matchGemExtraTurn =
                listMatchGem.stream().filter(gemMatch -> gemMatch.checkModifier(GemModifier.EXTRA_TURN)).findFirst();
        if (matchGemExtraTurn.isPresent()) {
            return matchGemExtraTurn.get().getIndexSwapGem();
        }
        // check 5 gem sword
        Optional<GemSwapInfo> matchGemSizeThanFourSword =
                listMatchGem.stream().filter(gemMatch -> gemMatch.getSizeMatch() > 4 && gemMatch.checkType(GemType.SWORD)).findFirst();
        if (matchGemSizeThanFourSword.isPresent()) {
            return matchGemSizeThanFourSword.get().getIndexSwapGem();
        }
        // check 5 gem normal has modifier
        for (GemModifier modifier : GemModifier.values()) {
            Optional<GemSwapInfo> matchGemSizeThanFour =
                    listMatchGem.stream().filter(gemMatch -> gemMatch.getSizeMatch() > 4 && gemMatch.checkModifier(modifier)).findFirst();
            if (matchGemSizeThanFour.isPresent()) {
                return matchGemSizeThanFour.get().getIndexSwapGem();
            }
        }
        // check 5 gem normal
        Optional<GemSwapInfo> matchGemSizeThanFour =
                listMatchGem.stream().filter(gemMatch -> gemMatch.getSizeMatch() > 4).findFirst();
        if (matchGemSizeThanFour.isPresent()) {
            return matchGemSizeThanFour.get().getIndexSwapGem();
        }
        return new Pair<>(true);
    }

    public Pair<Integer> recommendSwapGemSword(){
        Optional<GemSwapInfo> firstSword = listMatchGem.stream().filter(gemMatch -> gemMatch.checkType(GemType.SWORD)).findFirst();
        if(firstSword.isPresent()){
            return firstSword.get().getIndexSwapGem();
        }
        return new Pair<>(true);
    }

    public Pair<Integer> recommendSwapGem4Sword(){
        Optional<GemSwapInfo> firstSword = listMatchGem.stream().filter(gemMatch ->gemMatch.getSizeMatch()>3 && gemMatch.checkType(GemType.SWORD)).findFirst();
        if(firstSword.isPresent()){
            return firstSword.get().getIndexSwapGem();
        }
        return new Pair<>(true);
    }

    public Pair<Integer> recommendSwapGem() {
        if (listMatchGem.isEmpty()) {
            return new Pair<>(true);
        }
        // check gems my mana and has modifier and size 4
        for (GemModifier modifier : MODIFIER_LIST) {
            for (GemType type : myHeroGemType) {
                Optional<GemSwapInfo> matchGem =
                        listMatchGem.stream().filter(gemMatch -> gemMatch.getSizeMatch() > 3 && gemMatch.checkModifier(modifier) && gemMatch.checkType(type)).findFirst();
                if (matchGem.isPresent()) {
                    return matchGem.get().getIndexSwapGem();
                }
            }
        }
        // check gems my mana and size 4
        for (GemType type : myHeroGemType) {
            Optional<GemSwapInfo> matchGem =
                    listMatchGem.stream().filter(gemMatch -> gemMatch.getSizeMatch() > 3 && gemMatch.checkType(type)).findFirst();
            if (matchGem.isPresent()) {
                return matchGem.get().getIndexSwapGem();
            }
        }
        // check gems my mana has modifier
        for (GemModifier modifier : MODIFIER_LIST) {
            for (GemType type : myHeroGemType) {
                Optional<GemSwapInfo> matchGem =
                        listMatchGem.stream().filter(gemMatch -> gemMatch.checkModifier(modifier) && gemMatch.checkType(type)).findFirst();
                if (matchGem.isPresent()) {
                    return matchGem.get().getIndexSwapGem();
                }
            }
        }
        // check 3 gems my mana
        for (GemType type : myHeroGemType) {
            Optional<GemSwapInfo> matchGem =
                    listMatchGem.stream().filter(gemMatch -> gemMatch.checkType(type)).findFirst();
            if (matchGem.isPresent()) {
                return matchGem.get().getIndexSwapGem();
            }
        }
        // check 3 gems sword
        Optional<GemSwapInfo> matchGemSword =
                listMatchGem.stream().filter(gemMatch -> gemMatch.getType() == GemType.SWORD).findFirst();
        if (matchGemSword.isPresent()) {
            return matchGemSword.get().getIndexSwapGem();
        }
        // check 3 gem anu and has modifier
        for (GemModifier modifier : MODIFIER_LIST_ALL) {
            Optional<GemSwapInfo> matchGem =
                    listMatchGem.stream().filter(gemMatch -> gemMatch.checkModifier(modifier)).findFirst();
            if (matchGem.isPresent()) {
                return matchGem.get().getIndexSwapGem();
            }
        }
        // check 3 gems any
        Optional<GemSwapInfo> matchGemAny = listMatchGem.stream().findAny();
        if (matchGemAny.isPresent()) {
            return matchGemAny.get().getIndexSwapGem();
        }
        return listMatchGem.get(0).getIndexSwapGem();
    }

    public List<GemSwapInfo> suggestMatch() {
        List<GemSwapInfo> listMatchGem = new ArrayList<>();
        for (Gem currentGem : gems) {
            Gem swapGem = null;
            // If x > 0 => swap left & check
            if (currentGem.getX() > 0) {
                swapGem = gems.get(getGemIndexAt(currentGem.getX() - 1, currentGem.getY()));
                checkMatchSwapGem(listMatchGem, currentGem, swapGem);
            }
            // If x < 7 => swap right & check
            if (currentGem.getX() < 7) {
                swapGem = gems.get(getGemIndexAt(currentGem.getX() + 1, currentGem.getY()));
                checkMatchSwapGem(listMatchGem, currentGem, swapGem);
            }
            // If y < 7 => swap up & check
            if (currentGem.getY() < 7) {
                swapGem = gems.get(getGemIndexAt(currentGem.getX(), currentGem.getY() + 1));
                checkMatchSwapGem(listMatchGem, currentGem, swapGem);
            }
            // If y > 0 => swap down & check
            if (currentGem.getY() > 0) {
                swapGem = gems.get(getGemIndexAt(currentGem.getX(), currentGem.getY() - 1));
                checkMatchSwapGem(listMatchGem, currentGem, swapGem);
            }
        }
        return listMatchGem;
    }

    private void checkMatchSwapGem(List<GemSwapInfo> listMatchGem, Gem currentGem, Gem swapGem) {
        swap(currentGem, swapGem, gems);
        Set<Gem> matchGems = matchesAt(currentGem.getX(), currentGem.getY());
        Set<GemModifier> collectModifier = matchGems.stream().map(Gem::getModifier).collect(Collectors.toSet());
        swap(currentGem, swapGem, gems);
        if (!matchGems.isEmpty()) {
            listMatchGem.add(new GemSwapInfo(currentGem.getIndex(), swapGem.getIndex(), matchGems.size(), currentGem.getType(), collectModifier));
        }
    }

    private int getGemIndexAt(int x, int y) {
        return x + y * 8;
    }

    private void swap(Gem a, Gem b, List<Gem> gems) {
        int tempIndex = a.getIndex();
        int tempX = a.getX();
        int tempY = a.getY();

        // update reference
        gems.set(a.getIndex(), b);
        gems.set(b.getIndex(), a);

        // update data of element
        a.setIndex(b.getIndex());
        a.setX(b.getX());
        a.setY(b.getY());

        b.setIndex(tempIndex);
        b.setX(tempX);
        b.setY(tempY);
    }

    private Set<Gem> matchesAt(int x, int y) {
        Set<Gem> res = new HashSet<>();
        Gem center = gemAt(x, y);
        if (center == null) {
            return res;
        }

        // check horizontally
        List<Gem> hor = new ArrayList<>();
        hor.add(center);
        int xLeft = x - 1, xRight = x + 1;
        while (xLeft >= 0) {
            Gem gemLeft = gemAt(xLeft, y);
            if (gemLeft != null) {
                if (!gemLeft.sameType(center)) {
                    break;
                }
                hor.add(gemLeft);
            }
            xLeft--;
        }
        while (xRight < 8) {
            Gem gemRight = gemAt(xRight, y);
            if (gemRight != null) {
                if (!gemRight.sameType(center)) {
                    break;
                }
                hor.add(gemRight);
            }
            xRight++;
        }
        if (hor.size() >= 3) res.addAll(hor);

        // check vertically
        List<Gem> ver = new ArrayList<>();
        ver.add(center);
        int yBelow = y - 1, yAbove = y + 1;
        while (yBelow >= 0) {
            Gem gemBelow = gemAt(x, yBelow);
            if (gemBelow != null) {
                if (!gemBelow.sameType(center)) {
                    break;
                }
                ver.add(gemBelow);
            }
            yBelow--;
        }
        while (yAbove < 8) {
            Gem gemAbove = gemAt(x, yAbove);
            if (gemAbove != null) {
                if (!gemAbove.sameType(center)) {
                    break;
                }
                ver.add(gemAbove);
            }
            yAbove++;
        }
        if (ver.size() >= 3) res.addAll(ver);

        return res;
    }

    // Find Gem at Position (x, y)
    private Gem gemAt(int x, int y) {
        for (Gem g : gems) {
            if (g != null && g.getX() == x && g.getY() == y) {
                return g;
            }
        }
        return null;
    }

    private void printArrayGems() {
        int width = 8;
        int height = (gems.size() - 1) / width;
        for (int i = height; i >= 0; i--) {
            for (int j = 0; j < 8; j++) {
                System.out.print((gems.get(j + i * width).getType().getCode() + "\t"));
            }
            System.out.println();
        }
        System.out.println();
    }
}
