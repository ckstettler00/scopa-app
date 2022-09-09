// gameStore
import axios from 'axios'
//const clonedeep = require('lodash.clonedeep')
//const uuidv1 = require('uuid/v1')

const state = {
    gamelist : [],
    events_in : [],
    events_out: [],
    lastStatus : [],
    lastError: [],
}

const getters = {
    getGameList: (state) => state.gamelist,
    getEventsIn: (state) => state.events_in,
    getEventsOut: (state) => state.events_out,
    getLastStatus: (state) => state.lastStatus,
    getLastError: (state) => state.lastError
}

const actions = {

    fetchGameList(context) {
       console.info("fetchGameList")
        axios
        .get('http://localhost:8090/scopa/gamelist')
        .then((response) => {
          console.info("data:"+JSON.stringify(response.data))
          context.commit("SET_GAMELIST", response.data)
        })
        .catch(error => {
          console.log(error)
          this.errored = true
        })
    },

//    fetchGameStatus(context,  gameId) {
//
//    }
}

const mutations = {
    SET_GAMELIST(state, list) {
          console.info("SET_GAMELIST: "+ JSON.stringify(list))
          var games  = []
          for (var i = 0; i < list.length; i++) {
              games.push({})
              games[i].gameId = list[i].gameId
              games[i].gameState = list[i].gameState
              games[i].canJoin = false
              if (list[i].playerList.length > 1) {
                  games[i].owner = list[i].playerList[0]
              }
              if (list[i].playerList.length > 1) {
                  games[i].opponent = list[i].playerList[1]
              }
              if (list[i].playerList.length == 1) {
                  games[i].canJoin = true
              }
          }
          state.gamelist = games
          console.debug("gameStore.set_gamelist " + JSON.stringify(state.gamelist))
    },
}

export default {
    state,
    getters,
    actions,
    mutations
};

