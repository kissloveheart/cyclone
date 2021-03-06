package com.gmm.bot.model;


import com.gmm.bot.enumeration.GemType;
import com.gmm.bot.enumeration.HeroIdEnum;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@Slf4j
@Getter
@Setter
public class Hero {
    private int playerId;
    private HeroIdEnum id;
    private String name;
    private List<GemType> gemTypes = new ArrayList<>();
    private int maxHp; // Hp
    private int maxMana; // Mp
    private int attack;
    private int hp;
    private int mana;

    public Hero(ISFSObject objHero) {
        this.playerId = objHero.getInt("playerId");
        this.id = HeroIdEnum.from(objHero.getUtfString("id"));
        this.name = id.name();
        this.attack = objHero.getInt("attack");
        this.hp = objHero.getInt("hp");
        this.mana = objHero.getInt("mana");
        this.maxMana = objHero.getInt("maxMana");
        ISFSArray arrGemTypes = objHero.getSFSArray("gemTypes");
        for (int i = 0; i < arrGemTypes.size(); i++) {
            this.gemTypes.add(GemType.from(arrGemTypes.getUtfString(i)));
        }
    }

    public void updateHero(ISFSObject objHero) {
        this.attack = objHero.getInt("attack");
        this.hp = objHero.getInt("hp");
        this.mana = objHero.getInt("mana");
        this.maxMana = objHero.getInt("maxMana");
        log.info("Update hero"+ this.getId()+ " hp:"+ this.getHp()+ " attack: "+this.getAttack()+ " mana: "+ this.getMana());
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public boolean isFullMana() {
        return mana >= maxMana && hp > 0;
    }

    public boolean noFullMana(){
        return mana < maxMana && hp > 0;
    }

    public boolean isHeroSelfSkill() {
        return Arrays.asList(HeroIdEnum.SEA_SPIRIT).contains(id);
    }
}
