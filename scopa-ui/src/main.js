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

const reconn = () => {
  console.info("Forcing a background reconnect.")
  store.dispatch('connectToServer')
  clearTimeout(timerId)
}

const app = new Vue({
  vuetify,
  store,
  render: h => h(App),

  mounted: function(){
     this.$store.dispatch('connectToServer')
  },

  computed: {
    ...mapGetters(['getEventsOut', 'getSocket'])
  },

  router,

  watch: {
      getEventsOut: function() {
             console.info("send all events to server")
             this.$store.dispatch('sendEvents')
      },
      getSocket: function () {
          if (this.getSocket == null) {
              console.info("watching getSocket --> requires reconnect")
              timerId = setTimeout(reconn, 1000)
              console.info("Timer id:"+ timerId)
          } else {
              console.info("Good connection made.")
          }
      }
  }
}).$mount('#app')

window.app = app
window.axios = axios
