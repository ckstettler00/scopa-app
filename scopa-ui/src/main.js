import Vue from 'vue'
import App from './App.vue'
import vuetify from './plugins/vuetify'
import store from './store/index'
import axios from 'axios'
import { mapGetters } from 'vuex';
import VueRouter from 'vue-router'

import JoinGame from './components/JoinGame'
import GameBoard from './components/GameBoard'

const routes = [
  { path: '/join', component: JoinGame },
  { path: '/play', component: GameBoard }
]

Vue.use(VueRouter)

const router = new VueRouter({
  routes // short for `routes: routes`
})

Vue.config.productionTip = false

var timerId = 0;
function keepAlive() {
    var timeout = 20000;
    if (ws.readyState == ws.OPEN) {
        console.info("keepalive")
        ws.send('');
    }
    timerId = setTimeout(keepAlive, timeout);
}
function cancelKeepAlive() {
    console.info("keepalive - cancel")
    if (timerId) {
        clearTimeout(timerId);
    }
}
const ws = new WebSocket('ws://10.0.0.31:8090/scopaevents');
ws.onopen = function(){
    keepAlive()
}


ws.onclose = function(){
    cancelKeepAlive()
}

const app = new Vue({
  vuetify,
  store,
  render: h => h(App),

  mounted: function(){
        ws.onmessage = (event) => {
          this.$store.dispatch('addEventIn', JSON.parse(event.data));
         }
  },

  computed: {
    ...mapGetters(['getEventsOut'])
  },

  router,

  watch: {
      getEventsOut: function() {
             console.info("send all events to server")
             this.$store.dispatch('sendEvents', ws)
      }
  }
}).$mount('#app')

window.app = app
window.axios = axios
