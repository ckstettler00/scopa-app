import Vue from 'vue'
import App from './App.vue'
import vuetify from './plugins/vuetify'
import store from './store/index'
import axios from 'axios'

Vue.config.productionTip = false

const ws = new WebSocket('ws://localhost:8090/scopaevents');

const app = new Vue({
  vuetify,
  store,
  render: h => h(App),
  mounted: function(){
        connection.onmessage = (event) => {
          this.$store.dispatch('add_event_in', event.data);
         }
  },
  watch: {
      getEventsOut: function() {
         this.getEventsOut.forEach((e) => {
             console.info("send all events to server")
             this.$store.dispatch('send_events', ws)
         })
      }
  }
}).$mount('#app')

window.app = app
window.axios = axios
