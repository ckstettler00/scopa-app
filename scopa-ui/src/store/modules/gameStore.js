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

    sendEvents(context, ws) {
        var list = [...state.events_out]
        var e = list.shift()
        while (e != null) {
            console.info("sendEvents: event: " + JSON.stringify(e))
            if (e != null) {
                console.info("sendEvents: " + JSON.stringify(e))
                var msg = {
                    messageType : e.eventType,
                    payload : JSON.stringify(e)
                  }
                console.info("sentEvents: " + JSON.stringify(msg))
                ws.send(JSON.stringify(msg))
            }
            e = list.shift()
        }
        if (list.length > 0) {
            context.commit("CLEAR_EVENTS")
        }
    },
    addEventOut(context, event) {
        console.info("addEventOut:"+JSON.stringify(event))
        context.commit("ADD_EVENT_OUT", event)
    },
    addEventIn(context, event) {
        console.info("addEventIn:"+JSON.stringify(event))
        context.commit("ADD_EVENT_IN", event)
        var e = JSON.parse(event.payload)
        if (e.eventType == "STATUS") {
            console.info("addEventIn detected status "+JSON.stringify(e))
            context.commit("SET_LAST_STATUS", e)
        }
    }
}

const mutations = {
    ADD_EVENT_IN(state, event) {
        console.info("ADD_EVENT_IN: " + JSON.stringify(event))
        state.events_in.push(event)
        state.events_in = [...state.events_in]
    },
    ADD_EVENT_OUT(state, event) {
        console.info("ADD_EVENT_OUT: "+JSON.stringify(event))
        state.events_out.push(event)
        state.events_out = [...state.events_out]
        console.info("ADD_EVENT_OUT:" + JSON.stringify(state.events_out))
    },
    SET_LAST_STATUS(state, status) {
        state.lastStatus = status
    },
    CLEAR_EVENTS(state) {
        console.info("clear_events")
        state.events_out = []
    },
    SET_GAMELIST(state, list) {
          console.info("SET_GAMELIST: "+ JSON.stringify(list))
          var games  = []
          for (var i = 0; i < list.length; i++) {
              games.push({})
              games[i].gameId = list[i].gameId
              games[i].gameState = list[i].gameState
              games[i].canJoin = false
              if (list[i].playerList.length >= 1) {
                  games[i].owner = list[i].playerList[0]
              }
              if (list[i].playerList.length > 1) {
                  games[i].opponent = list[i].playerList[1]
              }
              if (list[i].playerList.length == 1) {
                  games[i].canJoin = true
              }
          }
          state.gamelist = [...games]
          console.info("gameStore.set_gamelist " + JSON.stringify(state.gamelist))
    },
}

export default {
    state,
    getters,
    actions,
    mutations
};

