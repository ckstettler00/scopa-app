<template>
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
</template>

<script>
  import Vue from 'vue'
  import { mapGetters } from 'vuex';
  import VueConfetti from 'vue-confetti'

  Vue.use(VueConfetti)

  export default {
      data () {
        return {
          games: [],
          errorText: "Nothing",
          alert: false
        }
      },

    computed: {
        ...mapGetters(['getScopa','getGameOver'])
    },

    created() {
    },

    watch: {
        getScopa : function() {
            console.info("BigBanner scopa change detected: " + this.getScopa)
            if (this.getScopa) {
                this.$confetti.start()
                setTimeout(this.confetti_stop, 2000)
                this.alert=true
            }

        },
        getGameOver : function() {
            if (this.gameOver) {
                console.info("BigBanner gameOver detected")
                this.$confetti.start()
                setTimeout(this.confetti_stop, 2000)
                this.alert=true
            } else {
                console.info("BigBanner gameOver detected but false")
            }
        },
    },

    methods: {
        confetti_stop: function() {
            this.$confetti.stop()
            this.alert = false
        }
    },
  }
</script>
