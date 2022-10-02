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
    newConnFlag: false,
    socket: null
}
 const JoinGameEvent ={
      "@type" : "RegisterEvent",
      eventType : "REGISTER",
      details : {
          screenHandle : null,
          emailAddr : null,
          playerToken : null,
          playerSecret : null
      },
      "gameId" : null
 }

const getters = {
    getGameList: (state) => state.gamelist,
    getEventsIn: (state) => state.events_in,
    getEventsOut: (state) => state.events_out,
    getLastStatus: (state) => state.lastStatus,
    getLastError: (state) => state.lastError,
    getNewConnFlag: (state) => state.newConnFlag,
    getSocket: (state) => state.socket
}

const actions = {

    fetchGameList(context) {
       var api_url = window.location.protocol + "//"+window.location.hostname + ":8090/scopa/gamelist"
       console.info("server url: "+ api_url)

       console.info("fetchGameList")
        axios
        .get(api_url)
        .then((response) => {
          console.info("data:"+JSON.stringify(response.data))
          context.commit("SET_GAMELIST", response.data)
        })
        .catch(error => {
          console.log(error)
          this.errored = true
        })
    },

    connectToServer(context) {
        const ws_url = ((window.location.protocol.endsWith('s:'))?"wss":"ws") + "://"+window.location.hostname+":8090" + "/scopaevents"
        console.info("websocket url: ["+ws_url+"]")
        var timerId = 0;
        var ws = {}

        console.info("connect -> connecting")
        ws = new WebSocket(ws_url);
        console.info("connect socket:"+JSON.stringify(ws))
        context.commit("SOCKET", ws)

        ws.onopen = function(){
            keepAlive()
            console.info("New Connection Detected:")

              console.info("watch detected new connection:"+JSON.stringify(state.lastStatus))
              if (state.lastStatus.status.gameId != null &&
                  state.lastStatus.status.playerDetails.playerId != null) {
                  var joinEvent = JoinGameEvent
                  joinEvent.gameId = state.lastStatus.status.gameId
                  joinEvent.details.playerId = state.lastStatus.status.playerDetails.playerId
                  joinEvent.details.screenHandle = state.lastStatus.status.playerDetails.screenHandle
                  joinEvent.details.playerSecret = state.lastStatus.status.playerDetails.playerSecret
                  console.info("Rejoining game in progress:"+JSON.stringify(joinEvent))
                  context.dispatch('addEventOut', joinEvent)
              } else {
                  console.info("New connection but game not in progress.")
              }
        }

        ws.onmessage = (event) => {
            context.dispatch('addEventIn', JSON.parse(event.data));
        }

        ws.onclose = function(){
            cancelKeepAlive()
            context.commit("SOCKET", null)
        }

        const keepAlive = () => {
            var timeout = 20000;
            if (ws.readyState == ws.OPEN) {
                console.info("keepalive")
                ws.send('');
            }
            timerId = setTimeout(keepAlive, timeout);
        }
        const cancelKeepAlive = () => {
            console.info("keepalive - cancel")
            if (timerId) {
                clearTimeout(timerId);
            }
        }
    },

    sendEvents(context) {
        var list = [...state.events_out]
        console.info("sendEvents - all events:"+JSON.stringify(list))
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
                state.socket.send(JSON.stringify(msg))
            }
            e = list.shift()
        }
        if (state.events_out.length > 0) {
            context.commit("CLEAR_EVENTS")
        }
    },
    changeConnFlag(context, value) {
        console.info("changeConnFlag:"+value)
        context.commit("CHANGE_CONN_FLAG", value)
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
        if (e.eventType == "ERROR") {
            console.info("addEventIn detected error "+JSON.stringify(e))
            context.commit("SET_LAST_ERROR", e)
        }
    }
}

const mutations = {
    CHANGE_CONN_FLAG(state, value) {
        console.info("CHANGE_CONN_FLAG:" + value)
        state.newConnFlag = true
    },
    SOCKET(state, value) {
        console.info("Setting socket:"+JSON.stringify(value))
        state.socket = value
    },
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
    SET_LAST_ERROR(state, event) {
        state.lastError = event
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
              if (list[i].playerList.length >= 1) {
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

