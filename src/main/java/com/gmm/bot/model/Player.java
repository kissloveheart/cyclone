package com.gmm.bot.model;

import com.gmm.bot.enumeration.GemType;
import com.gmm.bot.enumeration.HeroIdEnum;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;
@Slf4j
@Setter
@Getter
public class Player {
    private int id;
    private String displayName;
    private List<Hero> heroes;
    private Hero bird;
    private Hero fire;
    private Hero dog;
    private Hero cow;
    private List<GemType> manaTypeHeroes;
    private boolean isBirdCastSkill;
    private boolean hasCow;
     public Player(int id, String displayName) {
        this.id = id;
        this.displayName = displayName;
        this.heroes = new ArrayList<>();
        this.manaTypeHeroes = new ArrayList<>();
        this.isBirdCastSkill = false;
    }


    public boolean hasHeroFullMana(){
        return heroes.stream().anyMatch(hero -> hero.isAlive() && hero.isFullMana());
    }

    public Hero firstHeroAlive() {
        return heroes.stream().filter(Hero::isAlive).findFirst().orElse(null);
    }

    public List<GemType> getRecommendGemType() {
         manaTypeHeroes.clear();
         log.info("Before get recommend type gem to swap");
        log.info("Gem type recommend: "+ Arrays.toString(manaTypeHeroes.toArray())+" bird:" + isBirdCastSkill);
        log.info(" Dog not full mana:" +dog.noFullMana()+ " Bird not full mana:" +bird.noFullMana() + " Fire not full mana:" +fire.noFullMana());
         if(!bird.isAlive()){
             isBirdCastSkill = true;
         }
         if(!isBirdCastSkill){
             if(dog.noFullMana() && bird.noFullMana() && fire.noFullMana()){
                 manaTypeHeroes.addAll(dog.getGemTypes());
                 manaTypeHeroes.addAll(bird.getGemTypes());
                 manaTypeHeroes.addAll(fire.getGemTypes());
                 log.info("Gem type recommend case 1: "+ Arrays.toString(manaTypeHeroes.toArray())+" bird:" + isBirdCastSkill);
                 return manaTypeHeroes;
             } else if(dog.noFullMana() && bird.noFullMana()) {
                 manaTypeHeroes.addAll(dog.getGemTypes());
                 manaTypeHeroes.addAll(bird.getGemTypes());
                 log.info("Gem type recommend case 2: "+ Arrays.toString(manaTypeHeroes.toArray())+" bird:" + isBirdCastSkill);
                 return manaTypeHeroes;
             } else if(dog.noFullMana() && fire.noFullMana()) {
                 manaTypeHeroes.addAll(dog.getGemTypes());
                 manaTypeHeroes.addAll(fire.getGemTypes());
                 log.info("Gem type recommend case 3: "+ Arrays.toString(manaTypeHeroes.toArray())+" bird:" + isBirdCastSkill);
                 return manaTypeHeroes;
             } else if(bird.noFullMana() && fire.noFullMana()){
                 manaTypeHeroes.addAll(bird.getGemTypes());
                 manaTypeHeroes.addAll(fire.getGemTypes());
                 log.info("Gem type recommend case 4: "+ Arrays.toString(manaTypeHeroes.toArray())+" bird:" + isBirdCastSkill);
                 return manaTypeHeroes;
             } else if(dog.noFullMana()){
                 manaTypeHeroes.addAll(dog.getGemTypes());
                 log.info("Gem type recommend case 5: "+ Arrays.toString(manaTypeHeroes.toArray())+" bird:" + isBirdCastSkill);
                 return manaTypeHeroes;
             }  else if(bird.noFullMana()){
                 manaTypeHeroes.addAll(bird.getGemTypes());
                 log.info("Gem type recommend case 6: "+ Arrays.toString(manaTypeHeroes.toArray())+" bird:" + isBirdCastSkill);
                 return manaTypeHeroes;
             }  else if(fire.noFullMana()){
                 manaTypeHeroes.addAll(fire.getGemTypes());
                 log.info("Gem type recommend case 7: "+ Arrays.toString(manaTypeHeroes.toArray())+" bird:" + isBirdCastSkill);
                 return manaTypeHeroes;
             }
         }
         if(!dog.isAlive() && !fire.isAlive()){
             return Collections.emptyList();
         }
        if(dog.noFullMana() && bird.noFullMana() && fire.noFullMana()){
            manaTypeHeroes.addAll(dog.getGemTypes());
            manaTypeHeroes.addAll(fire.getGemTypes());
            manaTypeHeroes.addAll(bird.getGemTypes());
            log.info("Gem type recommend case 1: "+ Arrays.toString(manaTypeHeroes.toArray())+" bird:" + isBirdCastSkill);
            return manaTypeHeroes;
        } else if(dog.noFullMana() && fire.noFullMana()) {
            manaTypeHeroes.addAll(dog.getGemTypes());
            manaTypeHeroes.addAll(fire.getGemTypes());
            log.info("Gem type recommend case 2: "+ Arrays.toString(manaTypeHeroes.toArray())+" bird:" + isBirdCastSkill);
            return manaTypeHeroes;
        } else if(dog.noFullMana() && bird.noFullMana()) {
            manaTypeHeroes.addAll(dog.getGemTypes());
            manaTypeHeroes.addAll(bird.getGemTypes());
            log.info("Gem type recommend case 3: "+ Arrays.toString(manaTypeHeroes.toArray())+" bird:" + isBirdCastSkill);
            return manaTypeHeroes;
        } else if(fire.noFullMana() && bird.noFullMana()){
            manaTypeHeroes.addAll(fire.getGemTypes());
            manaTypeHeroes.addAll(bird.getGemTypes());
            log.info("Gem type recommend case 4: "+ Arrays.toString(manaTypeHeroes.toArray())+" bird:" + isBirdCastSkill);
            return manaTypeHeroes;
        } else if(dog.noFullMana()){
            manaTypeHeroes.addAll(dog.getGemTypes());
            log.info("Gem type recommend case 5: "+ Arrays.toString(manaTypeHeroes.toArray())+" bird:" + isBirdCastSkill);
            return manaTypeHeroes;
        }  else if(fire.noFullMana()){
            manaTypeHeroes.addAll(fire.getGemTypes());
            log.info("Gem type recommend case 6: "+ Arrays.toString(manaTypeHeroes.toArray())+" bird:" + isBirdCastSkill);
            return manaTypeHeroes;
        }  else if(bird.noFullMana()){
            manaTypeHeroes.addAll(bird.getGemTypes());
            log.info("Gem type recommend case 7: "+ Arrays.toString(manaTypeHeroes.toArray())+" bird:" + isBirdCastSkill);
            return manaTypeHeroes;
        }
        log.info("Gem type recommend: "+ Arrays.toString(manaTypeHeroes.toArray())+" bird:" + isBirdCastSkill+ "No match any case");
        return manaTypeHeroes;
    }

    public void initialHeroes(ISFSArray hero) {
        for (int i = 0; i < hero.size(); i++) {
            Hero hero1 = new Hero(hero.getSFSObject(i));
            heroes.add(hero1);
            if(hero1.getId() == HeroIdEnum.SEA_GOD){
                this.hasCow = true;
            }
            switch (hero1.getId()){
                case SEA_SPIRIT:
                    bird = hero1;
                    break;
                case CERBERUS:
                    dog = hero1;
                    break;
                case FIRE_SPIRIT:
                    fire = hero1;
                    break;
                case SEA_GOD:
                    cow = hero1;
                    break;
            }
        }
    }

    public List<Hero> getAliveHero(){
         return heroes.stream().filter(Hero::isAlive).collect(Collectors.toList());
    }


}
