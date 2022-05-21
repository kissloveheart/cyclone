package com.gmm.bot.model;

import com.gmm.bot.enumeration.GemType;
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
    private List<GemType> manaTypeHeroes;
    private boolean isBirdCastSkill;
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
        log.info(" Dog full mana:" +dog.isFullMana()+ " Bird full mana:" +bird.isFullMana() + " Fire full mana:" +fire.isFullMana());
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
        } else if(bird.noFullMana() && fire.noFullMana()){
            manaTypeHeroes.addAll(bird.getGemTypes());
            manaTypeHeroes.addAll(fire.getGemTypes());
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
            heroes.add(new Hero(hero.getSFSObject(i)));
            if(i == 0){
                bird = heroes.get(i);
            } else if (i == 1){
                fire = heroes.get(i);
            } else {
                dog = heroes.get(i);
            }
        }
    }

    public List<Hero> getAliveHero(){
         return heroes.stream().filter(Hero::isAlive).collect(Collectors.toList());
    }


}
