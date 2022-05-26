package com.gmm.bot.ai;

import com.gmm.bot.enumeration.BattleMode;
import com.gmm.bot.enumeration.GemType;
import com.gmm.bot.enumeration.HeroIdEnum;
import com.gmm.bot.model.*;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.IEventListener;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.entities.Room;
import sfs2x.client.entities.User;
import sfs2x.client.requests.ExtensionRequest;
import sfs2x.client.requests.LoginRequest;
import sfs2x.client.util.ConfigData;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.gmm.bot.ai.ConstantCommand.LOBBY_FIND_GAME;

@Slf4j
@Getter
@Component
public  class BaseBot implements IEventListener {
    private final int ENEMY_PLAYER_ID = 0;
    private final int BOT_PLAYER_ID = 2;
    private final int SNAPSHOT_SIZE = 1;
    @Autowired
    protected ThreadPoolTaskScheduler taskScheduler;
    @Value("${smartfox.host}")
    protected String host;
    @Value("${smartfox.zone}")
    protected String zone;
    @Value("${smartfox.port}")
    protected int port;
    @Value("${gemswap.delay}")
    protected int delaySwapGem;
    @Value("${find.game.delay}")
    protected int delayFindGame;
    protected SmartFox sfsClient;
    protected Room room;
    protected Player botPlayer;
    protected Player enemyPlayer;
    protected int currentPlayerId;
    protected Grid grid;
    protected volatile boolean isJoinGameRoom;
    protected String username;
    protected String password;
    protected String token;
    protected SFSObject data;
    protected boolean disconnect;
    private Scanner keyboard;

    public void start() {
        try {
            this.logStatus("init", "Initializing");
            this.init();
            this.connect();
        } catch (Exception e) {
            this.log("Init bot error =>" + e.getMessage());
        }
    }

    private void init() {
        username = "hiep.nguyenvan1";
        password = "123456";
        sfsClient = new SmartFox();
        data = new SFSObject();
        isJoinGameRoom = false;
        disconnect = false;
        this.token = "bot";
        keyboard = new Scanner(System.in);
        this.sfsClient.addEventListener(SFSEvent.CONNECTION, this);
        this.sfsClient.addEventListener(SFSEvent.CONNECTION_LOST, this);
        this.sfsClient.addEventListener(SFSEvent.LOGIN, this);
        this.sfsClient.addEventListener(SFSEvent.LOGIN_ERROR, this);
        this.sfsClient.addEventListener(SFSEvent.ROOM_JOIN, this);
        this.sfsClient.addEventListener(SFSEvent.ROOM_JOIN_ERROR, this);
        this.sfsClient.addEventListener(SFSEvent.EXTENSION_RESPONSE, this);
    }

    protected void connect() {
        this.logStatus("connecting", " => Connecting to smartfox server " + host + "|" + port + " zone: " + zone);

        ConfigData cf = new ConfigData();
        cf.setHost(host);
        cf.setPort(port);
        cf.setUseBBox(true);
        cf.setZone(zone);

        try {
            this.sfsClient.connect(cf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        this.logStatus("disconnect|", " manual called disconnect from client");
        try {
            sfsClient.disconnect();
            disconnect = true;
        } catch (Exception e) {
            log.error("disconnect|" + this.username + "|error =>" + e.getMessage());
        }
    }

    public void dispatch(BaseEvent event) {
        String eventType = event.getType();

        switch (eventType) {
            case SFSEvent.CONNECTION:
                this.onConnection(event);
                break;
            case SFSEvent.CONNECTION_LOST:
                this.onConnectionLost(event);
                break;
            case SFSEvent.LOGIN:
                this.onLoginSuccess(event);
                break;
            case SFSEvent.LOGIN_ERROR:
                this.onLoginError(event);
                break;
            case SFSEvent.ROOM_JOIN:
                this.onRoomJoin(event);
                break;
            case SFSEvent.ROOM_JOIN_ERROR:
                this.onRoomJoinError(event);
                break;
            case SFSEvent.EXTENSION_RESPONSE:
                this.onExtensionResponse(event);
                break;
            default:
        }
    }

    private void showError(SFSObject params) {
        String error = params.getUtfString("message");
        log(error);
    }

    private void onConnection(BaseEvent event) {
        if (event.getArguments().get("success").equals(true)) {
            this.logStatus("try-login", "Connected to smartfox|" + event.getArguments().toString());
            this.login();
        } else {
            this.logStatus("onConnection|success == false", "Failed to connect");
        }
    }

    protected void onConnectionLost(BaseEvent event) {
        this.logStatus("onConnectionLost", "userId connection lost server: " + event.getArguments().toString());
        disconnect = true;
        sfsClient.removeAllEventListeners();
    }


    protected void onLoginError(BaseEvent event) {
        this.logStatus("login-error", "Login failed");
        disconnect();
    }

    protected void onRoomJoin(BaseEvent event) {
        logStatus("Join-room", "Joined room " + this.sfsClient.getLastJoinedRoom().getName());
        room = (Room) event.getArguments().get("room");
        if (room.isGame()) {
            return;
        }
        String start = "";
        System.out.print("======> Enter an \"1\" to start find game: ");
        do{
            start = keyboard.nextLine();
            System.out.println();
        } while (!start.equals("1"));
        findGame();

    }

    private void findGame() {
        data.putUtfString("type", "");
        data.putUtfString("adventureId", "");
        sendZoneExtensionRequest(LOBBY_FIND_GAME, data);
        log("Send request Find game from lobby");
    }

    protected void onRoomJoinError(BaseEvent event) {
        if (this.sfsClient.getLastJoinedRoom() != null) {
            this.logStatus("join-room", "Joined room " + this.sfsClient.getLastJoinedRoom().getName());
        }
       findGame();
    }

    protected void onExtensionResponse(BaseEvent event) {
        String cmd = event.getArguments().containsKey("cmd") ? event.getArguments().get("cmd").toString() : "";
        SFSObject params = (SFSObject) event.getArguments().get("params");

        logStatus("onExtensionResponse", cmd);
        switch (cmd) {
            case ConstantCommand.START_GAME:
                ISFSObject gameSession = params.getSFSObject("gameSession");
                startGame(gameSession, room);
                break;
            case ConstantCommand.END_GAME:
                endGame(params);
                break;
            case ConstantCommand.START_TURN:
                startTurn(params);
                break;
            case ConstantCommand.ON_SWAP_GEM:
                swapGem(params);
                break;
            case ConstantCommand.ON_PLAYER_USE_SKILL:
                handleGems(params);
                break;
            case ConstantCommand.PLAYER_JOINED_GAME:
                sendExtensionRequest(ConstantCommand.I_AM_READY, new SFSObject());
                break;
            case ConstantCommand.SEND_ALERT:
                showError(params);
                break;

        }
    }

    private void endGame(ISFSObject params) {
        Integer winner = params.getInt("winner");
        if(winner == botPlayer.getId()){
            log("The winner is my bot :"+ botPlayer.getDisplayName() + " id: "+botPlayer.getId());
        }
        if(winner == enemyPlayer.getId()){
            log("The winner is enemy :"+ enemyPlayer.getDisplayName() + " id: "+enemyPlayer.getId());
        }
        isJoinGameRoom = false;
    }

    protected void assignPlayers(Room room) {
        List<User> users = room.getPlayerList();
        User user1 = users.get(0);
        log("id user1: " + user1.getPlayerId() + " name:"+ user1.getName());
        if(users.size() == 1){
            if (user1.isItMe()) {
                botPlayer = new Player(user1.getPlayerId(), "player1");
                enemyPlayer = new Player(ENEMY_PLAYER_ID, "player2");
            } else {
                botPlayer = new Player(BOT_PLAYER_ID, "player2");
                enemyPlayer = new Player(ENEMY_PLAYER_ID, "player1");
            }
            return;
        }
        User user2 = users.get(1);
        log("id user2: " + user2.getPlayerId()+ " name:"+user2.getName());
        if (user1.isItMe()) {
            botPlayer = new Player(user1.getPlayerId(), "player"+user1.getPlayerId());
            enemyPlayer = new Player(user2.getPlayerId(), "player"+user2.getPlayerId());
        } else {
            botPlayer = new Player(user2.getPlayerId(), "player"+user2.getPlayerId());
            enemyPlayer = new Player(user1.getPlayerId(), "player"+user1.getPlayerId());
        }
    }

    protected void logStatus(String status, String logMsg) {
        log.info(this.username + "|" + status + "|" + logMsg + "\n");
    }

    protected void log(String msg) {
        log.info(this.username + "|" + msg);
    }

    private void onLoginSuccess(BaseEvent event) {
            log("onLogin()|" + event.getArguments().toString());
    }

    protected void login() {
        log("login()");
        getTokenLogin();
        SFSObject parameters = new SFSObject();
        parameters.putUtfString(ConstantCommand.BATTLE_MODE, BattleMode.NORMAL.name());
        parameters.putUtfString(ConstantCommand.ID_TOKEN, this.token);
        parameters.putUtfString(ConstantCommand.NICK_NAME,"CYCLONE");
        this.sfsClient.send(new LoginRequest(username, "", zone, parameters));
    }

    public void sendExtensionRequest(String extCmd, ISFSObject params) {
        this.sfsClient.send(new ExtensionRequest(extCmd, params, room));
    }

    public void sendZoneExtensionRequest(String extCmd, ISFSObject params) {
        this.sfsClient.send(new ExtensionRequest(extCmd, params));
    }

    protected void swapGem(SFSObject params) {
        boolean isValidSwap = params.getBool("validSwap");
        if (!isValidSwap) {
            return;
        }
        handleGems(params);
    }


    protected void handleGems(ISFSObject params) {
        ISFSObject gameSession = params.getSFSObject("gameSession");
        currentPlayerId = gameSession.getInt("currentPlayerId");
        //get last snapshot
        ISFSArray snapshotSfsArray = params.getSFSArray("snapshots");
        ISFSObject lastSnapshot = snapshotSfsArray.getSFSObject(snapshotSfsArray.size() - 1);
        boolean needRenewBoard = params.containsKey("renewBoard");
        // update information of hero
        handleHeroes(lastSnapshot);
        if (needRenewBoard) {
            grid.updateGems(params.getSFSArray("renewBoard"),null);
            taskScheduler.schedule(new FinishTurn(false), getStartTime(1));
            return;
        }
        // update gem
        grid.setMyHeroGemType(botPlayer.getRecommendGemType());
        ISFSArray gemCodes = lastSnapshot.getSFSArray("gems");
        ISFSArray gemModifiers = lastSnapshot.getSFSArray("gemModifiers");
        grid.updateGems(gemCodes,gemModifiers);
        taskScheduler.schedule(new FinishTurn(false), getStartTime(snapshotSfsArray.size()));
    }


    protected void startTurn(ISFSObject params) {
        currentPlayerId = params.getInt("currentPlayerId");
        if (!isBotTurn()) {
            return;
        }
        // check 5gem
        Pair<Integer> swap5GemPair = grid.recommendSwap5Gem();
        if(!swap5GemPair.isNull()){
            taskScheduler.schedule(new SendRequestSwapGem(swap5GemPair), getStartTime(SNAPSHOT_SIZE));
            return;
        }
        // check skill dog can skill all
        if(botPlayer.getDog().isFullMana()){
            boolean allMatchCanSkill = enemyPlayer.getAliveHero().stream().allMatch(hero -> botPlayer.getDog().getAttack() + 2 > hero.getHp());
            if(allMatchCanSkill){
                taskScheduler.schedule(new SendReQuestSkill(botPlayer.getDog()), getStartTime(SNAPSHOT_SIZE));
                return;
            }
        }

        // check hero enemy size == 1 can skill instant
        List<Hero> aliveHero = enemyPlayer.getAliveHero();
        if(aliveHero.size() <=2){
            Pair<Integer> integerPairSword = grid.recommendSwapGemSword();
            if(!integerPairSword.isNull()){
                Hero heroFirst = botPlayer.firstHeroAlive();
                if(heroFirst.getAttack() > aliveHero.get(0).getHp()){
                    taskScheduler.schedule(new SendRequestSwapGem(integerPairSword), getStartTime(SNAPSHOT_SIZE));
                    return;
                }
            }

        }
        // check skill dog
        if(botPlayer.getDog().isFullMana() && botPlayer.isBirdCastSkill()){
            taskScheduler.schedule(new SendReQuestSkill(botPlayer.getDog()), getStartTime(SNAPSHOT_SIZE));
            return;
        }
        if(botPlayer.getBird().isFullMana()){
            botPlayer.setBirdCastSkill(true);
            if(botPlayer.getDog().isAlive()){
                taskScheduler.schedule(new SendReQuestSkill(botPlayer.getBird(),botPlayer.getDog().getId() ), getStartTime(SNAPSHOT_SIZE));
                return;
            }
            taskScheduler.schedule(new SendReQuestSkill(botPlayer.getBird(),botPlayer.getBird().getId() ), getStartTime(SNAPSHOT_SIZE));
            return;
        }
        // check skill dog if enemy has buffalo
        if(botPlayer.getDog().isFullMana() && enemyPlayer.isHasCow()){
            if(enemyPlayer.getCow().isFullMana()){
                taskScheduler.schedule(new SendReQuestSkill(botPlayer.getDog()), getStartTime(SNAPSHOT_SIZE));
                return;
            }
        }
        //todo check skill fire 1 size
        if(botPlayer.getFire().isFullMana()){
            int countGemRed = (int) grid.getGems().stream().filter(gem -> gem.getType() == GemType.RED).count();
            List<Hero> heroCanDie = enemyPlayer.getHeroes().stream().filter(hero -> hero.isAlive() && (hero.getAttack() + countGemRed) > hero.getHp()).collect(Collectors.toList());
            if(!heroCanDie.isEmpty()){
                chooseTargetHero(heroCanDie);
                return;
            }
            List<Hero> collectHero = enemyPlayer.getHeroes().stream().filter(hero -> hero.isAlive() && (hero.getAttack() + countGemRed) > 14).collect(Collectors.toList());
            if(!collectHero.isEmpty()){
                chooseTargetHero(collectHero);
                return;
            }
            if(botPlayer.getAliveHero().size() == 1 || enemyPlayer.getAliveHero().size() == 1){
                chooseTargetHero(enemyPlayer.getAliveHero());
                return;
            }
        }
        // check skill fire if enemy has buffalo
        if(botPlayer.getFire().isFullMana() && enemyPlayer.isHasCow()){
            if(enemyPlayer.getCow().isFullMana()){
                chooseTargetHero(enemyPlayer.getAliveHero());
                return;
            }
        }
        //check 4 sword
        Pair<Integer> integerSwordFour = grid.recommendSwapGem4Sword();
        if(!integerSwordFour.isNull()){
            taskScheduler.schedule(new SendRequestSwapGem(integerSwordFour), getStartTime(SNAPSHOT_SIZE));
            return;
        }
        //check gem mana
        Pair<Integer> swapGemPair = grid.recommendSwapGem();
        if(!swapGemPair.isNull()){
            taskScheduler.schedule(new SendRequestSwapGem(swapGemPair), getStartTime(SNAPSHOT_SIZE));
            return;
        }
        log("Error: No case to send server, please checkkkkkkkkkkkkkkkkkkkkk");

    }

    private void chooseTargetHero(List<Hero> heroCanDie) {
        Optional<Hero> heroDieFullMana = heroCanDie.stream().filter(Hero::isFullMana).findFirst();
        if(heroDieFullMana.isPresent()){
            taskScheduler.schedule(new SendReQuestSkill(botPlayer.getFire(),heroDieFullMana.get().getId()), getStartTime(SNAPSHOT_SIZE));
            return;
        }
        Hero heroCanDieMaxAttack = heroCanDie.stream().max(Comparator.comparingInt(Hero::getAttack)).get();
        taskScheduler.schedule(new SendReQuestSkill(botPlayer.getFire(),heroCanDieMaxAttack.getId()), getStartTime(SNAPSHOT_SIZE));
    }

    private Date getStartTime(int sizeSnapshot) {
        return new Date(System.currentTimeMillis() + (long) delaySwapGem *sizeSnapshot);
    }

    private void handleHeroes(ISFSObject params) {
        ISFSArray heroesBotPlayer = params.getSFSArray(botPlayer.getDisplayName());
        for (int i = 0; i < botPlayer.getHeroes().size(); i++) {
            botPlayer.getHeroes().get(i).updateHero(heroesBotPlayer.getSFSObject(i));
        }

        ISFSArray heroesEnemyPlayer = params.getSFSArray(enemyPlayer.getDisplayName());
        for (int i = 0; i < enemyPlayer.getHeroes().size(); i++) {
            enemyPlayer.getHeroes().get(i).updateHero(heroesEnemyPlayer.getSFSObject(i));
        }
    }


    protected void startGame(ISFSObject gameSession, Room room) {
        // Assign Bot player & enemy player
        assignPlayers(room);

        // Player & Heroes
        ISFSObject objBotPlayer = gameSession.getSFSObject(botPlayer.getDisplayName());
        ISFSObject objEnemyPlayer = gameSession.getSFSObject(enemyPlayer.getDisplayName());

        ISFSArray botPlayerHero = objBotPlayer.getSFSArray("heroes");
        ISFSArray enemyPlayerHero = objEnemyPlayer.getSFSArray("heroes");

        botPlayer.initialHeroes(botPlayerHero);
        enemyPlayer.initialHeroes(enemyPlayerHero);
        // Gems
        grid = new Grid(gameSession.getSFSArray("gems"));
        grid.setMyHeroGemType(botPlayer.getRecommendGemType());
        currentPlayerId = gameSession.getInt("currentPlayerId");
        // set data skill
        data.putUtfString("gemIndex", String.valueOf(ThreadLocalRandom.current().nextInt(64)));
        data.putBool("isTargetAllyOrNot",false);
        data.putUtfString("targetId", enemyPlayer.firstHeroAlive().getId().toString());
        log("Initial game ");
        taskScheduler.schedule(new FinishTurn(true), getStartTime(SNAPSHOT_SIZE));
    }



    protected GemType selectGem() {
        return botPlayer.getRecommendGemType().stream().filter(gemType -> grid.getGemTypes().contains(gemType)).findFirst().orElseGet(null);
    }

    protected boolean isBotTurn() {
        return botPlayer.getId() == currentPlayerId;
    }

    private class FinishTurn implements Runnable {
        private final boolean isFirstTurn;

        public FinishTurn(boolean isFirstTurn) {
            this.isFirstTurn = isFirstTurn;
        }

        @Override
        public void run() {
            SFSObject data = new SFSObject();
            data.putBool("isFirstTurn", isFirstTurn);
            log("sendExtensionRequest()|room:" + room.getName() + "|extCmd:" + ConstantCommand.FINISH_TURN + " first turn " + isFirstTurn);
            sendExtensionRequest(ConstantCommand.FINISH_TURN, data);
        }
    }

    private class SendReQuestSkill implements Runnable {
        private final Hero heroCastSkill;
        private final HeroIdEnum targetHero;
        public SendReQuestSkill(Hero heroCastSkill,HeroIdEnum heroIdEnum) {
            this.heroCastSkill = heroCastSkill;
            this.targetHero = heroIdEnum;
        }

        public SendReQuestSkill(Hero heroCastSkill) {
            this(heroCastSkill,null);
        }

        @Override
        public void run() {
            data.putUtfString("casterId", heroCastSkill.getId().toString());
            if(targetHero != null){
                data.putUtfString("targetId", targetHero.toString());
            }
            log("sendExtensionRequest()|room:" + room.getName() + "|extCmd:" + ConstantCommand.USE_SKILL + "|Hero cast skill: " + heroCastSkill.getName() + " targetId: "+targetHero);
            sendExtensionRequest(ConstantCommand.USE_SKILL, data);
        }

    }

    private class SendRequestSwapGem implements Runnable {
        private Pair<Integer> indexSwap;

        public SendRequestSwapGem(Pair<Integer> indexSwap) {
            this.indexSwap = indexSwap;
        }
        @Override
        public void run() {
            data.putInt("index1", indexSwap.getParam1());
            data.putInt("index2", indexSwap.getParam2());
            log("sendExtensionRequest()|room:" + room.getName() + "|extCmd:" + ConstantCommand.SWAP_GEM + "|index1: " + indexSwap.getParam1() + " index1: " + indexSwap.getParam2());
            sendExtensionRequest(ConstantCommand.SWAP_GEM, data);
        }
    }

    private void getTokenLogin(){
        HttpEntity<Account> request = new HttpEntity<>(new Account(username,password));
        String URL ="http://172.16.100.112:8081/api/v1/user/authenticate";
        RestTemplate restTemplate = new RestTemplate();
        Object response= restTemplate.postForObject(URL,request,Object.class);
        this.token=response.toString().split("=")[1].replace("}","");
    }

}