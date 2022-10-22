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
          readonly=true
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
         <router-view></router-view>
    </v-main>
  </v-app>
</template>

<script>
import { mapGetters } from 'vuex';
export default {
  name: 'App',

  created() {
      this.$router.push("/join")
  },
  computed: {
      ...mapGetters(['getLastError'])
  },
  watch: {
        getLastError : function() {
            console.info("error handler:"+JSON.stringify(this.getLastError))
            this.alert=true
            this.errorText = this.getLastError.message
        }
    },
  data: () => ({
        snackbar: false,
        alert: false,
        errorText: 'UNKNOWN ERROR'
  }),
};
</script>
