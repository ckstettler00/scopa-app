import Vue from 'vue'
import App from './App.vue'
import vuetify from './plugins/vuetify'
import store from './store/index'
import axios from 'axios'
import { mapGetters } from 'vuex';


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
  watch: {
      getEventsOut: function() {
             console.info("send all events to server")
             this.$store.dispatch('sendEvents', ws)
      }
  }
}).$mount('#app')

window.app = app
window.axios = axios
