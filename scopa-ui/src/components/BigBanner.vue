<template>
    <v-dialog
      v-model="alert"
       align-center
       max-width="50%"
    >
      <v-card color="pink lighten-5">
        <div class="text-h3 text-center">
        {{errorText}}
        </div>
        <v-card-actions>
          <v-btn
            color="red lighten-4"
            @click="end_game()"
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
          alert: false,
          playerId: null
        }
      },

    computed: {
        ...mapGetters(['getScopa','getGameOver','getFinalScore','getLastStatus'])
    },

    created() {
    },

    watch: {
        getScopa : function() {
            console.info("BigBanner scopa change detected: " + this.getScopa)
            if (this.getScopa) {
                this.errorText = "Scopa!!"
                this.$confetti.start()
                setTimeout(this.confetti_stop, 3000)
                this.alert=true
            }

        },
        getGameOver : function() {
            if (this.getGameOver) {
                console.info("BigBanner gameOver detected")
                this.$confetti.start()
                setTimeout(this.confetti_stop, 2000)
                this.alert=true
            }
        },
        getFinalScore: function() {
            this.errorText = "Gameover: You "
            console.info("finalScore: winner:" + this.getFinalScore.winningPlayer.playerId + " current:"+this.playerId)
            if (this.getFinalScore.winningPlayer.playerId == this.playerId) {
                this.errorText = this.errorText + "win!!"
            } else {
                this.errorText = this.errorText + "lose. :-("
            }
            this.errorText = this.errorText + " " + this.getFinalScore.winningScore + " to " + this.getFinalScore.losingScore
        },
        getLastStatus: function() {
            this.playerId = this.getLastStatus.status.playerDetails.playerId
        }

    },

    methods: {
        confetti_stop: function() {
            this.$confetti.stop()
            if (this.getScopa) {
                console.info("Reset SET_SCOPA")
                this.$store.dispatch("clearScopa")
                this.alert = false
            }
         },
         end_game: function() {
             this.alert = false
             if (this.getGameOver) {
                 this.$store.dispatch("clearGameOver")
                 this.$router.push("/join")
             }
         }
    },
  }
</script>
