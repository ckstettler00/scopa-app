import Vuex from 'vuex'
import Vue from 'vue'
import store from './modules/gameStore'

Vue.use(Vuex)

export default new Vuex.Store({
    modules: {
        store,
    },

})