package com.stettler.scopa.scopaserver.service;

import com.stettler.scopa.scopaserver.model.GameEntry;
import com.stettler.scopa.scopaserver.utils.GameRegistry;
import com.stettler.scopa.statemachine.GameControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component

public class GameService {
    @Autowired
    GameRegistry registry;

    /**
     * Return a list of games currently registered in the game registry.
     * @return
     */
    public List<GameEntry> getGameList(){

        List<String> idList = registry.getGameIds();
        List<GameEntry> entryList = new ArrayList<>();


        for(int i = 0; i < idList.size(); i++){

            GameControl temp = registry.findGame(idList.get(i));
            List<String> names = new ArrayList<>();

            if(temp.getPlayer1() != null){
                names.add(temp.getPlayer1().getDetails().getScreenHandle());
            }
            if(temp.getPlayer2() != null){
                names.add(temp.getPlayer2().getDetails().getScreenHandle());
            }
            entryList.add(new GameEntry(names, idList.get(i), temp.getCurrentState()));
        }
        return entryList;
    }
 }
