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

const ws = new WebSocket('ws://localhost:8090/scopaevents');

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
