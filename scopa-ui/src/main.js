import Vue from 'vue'
import App from './App.vue'
import vuetify from './plugins/vuetify'
import store from './store/index'
import axios from 'axios'

Vue.config.productionTip = false

const app = new Vue({
  vuetify,
  store,
  render: h => h(App)
}).$mount('#app')

window.app = app
window.axios = axios
