<template>
  <v-app>
    <v-app-bar
        app
        color="primary"
        dark
    >
      <div class="d-flex align-center">
        <v-img
            alt="Scopa Logo"
            class="shrink mr-2"
            contain
            src="scopa.jpeg"
            transition="scale-transition"
            max-height="65"
            width="65"
        />
        <span class="mr-2">Scopa</span>
      </div>

      <v-spacer></v-spacer>

    <v-dialog
      v-model="alert"
       align-center
       max-width="50%"
    >
      <v-card color="pink lighten-5">
        <v-textarea class="pa-4"
          name="input-7-1"
          :value="errorText"
          readonly
        ></v-textarea>

        <v-card-actions>
          <v-spacer></v-spacer>

          <v-btn
            color="red lighten-4"
            @click="alert = false"
          >
            Ok
          </v-btn>

        </v-card-actions>
      </v-card>
    </v-dialog>
    </v-app-bar>
    <v-main>
        <v-snackbar
            v-model="snackbar"
            timeout=3000
            centered=true
            color=primary
            class="[text-h1]"
            >
            Scopa!!
         </v-snackbar>

         <router-view></router-view>
    </v-main>
  </v-app>
</template>

<script>
import Vue from 'vue'
import VueConfetti from 'vue-confetti'
import { mapGetters } from 'vuex';

Vue.use(VueConfetti)

export default {
  name: 'App',

  created() {
      this.$router.push("/join")
  },
  methods: {
        confetti_stop: function() {
            this.$confetti.stop()
        }
  },
  computed: {
      ...mapGetters(['getLastError']),
  },
  watch: {
        getLastError : function() {
            console.info("error handler:"+JSON.stringify(this.getLastError))
           // this.alert=true
            this.errorText = this.getLastError.message
            this.$confetti.start()
            setTimeout(this.confetti_stop, 2000)
            this.snackbar=true
        }
    },
  data: () => ({
        snackbar: false,
        alert: false,
        errorText: 'UNKNOWN ERROR'
  }),
};
</script>
