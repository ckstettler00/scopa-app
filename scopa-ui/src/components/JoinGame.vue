<template>
    <v-card
    :loading = "loading"
    >
    <v-card-title>Games in Progress</v-card-title>
    <v-card-text>Join a game in progress or start a new game.
    </v-card-text>


    <v-data-table
      :headers="headerArray"
      :items="games"
      item-key="gameId"
      class="elevation-1"
    >

    <template v-slot:[`item.actions`]="{ item }">
          <v-btn color="primary"
          @click="openDialog(item)"
           v-if="item.canJoin">
            {{item.label}}
          </v-btn>
    </template>


    </v-data-table>
    <v-dialog
      v-model="dialog"
      persistent
      max-width="600px"
    >

      <v-card
      :loading="waitForStart">
        <v-card-title>
          <span class="text-h5">User Profile</span>
        </v-card-title>
        <v-card-text>
          <v-container>
            <v-row>
              <v-col
                cols="12"
                sm="6"
                md="4"
              >
                <v-text-field
                  v-model="screenHandle"
                  label="Screen Handle*"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="12">
                <v-text-field
                  v-model="emailAddr"
                  label="Email"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="12">
                <v-text-field
                  v-model="secret"
                  label="Password*"
                  type="password"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
          </v-container>
          <small>*indicates required field</small>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="blue darken-1"
            text
            @click="dialog = false"
          >
            Cancel
          </v-btn>
          <v-btn
            color="blue darken-1"
            text
            @click="startOrJoin()"
          >
            {{this.dialogLabel}}
          </v-btn>
          <template slot="progress">
              <v-progress-circular
                indeterminate
                color="primary"
                 ></v-progress-circular>
          </template>
        </v-card-actions>
      </v-card>
    </v-dialog>
    </v-card>
</template>

<script>
  import { mapGetters, mapActions } from 'vuex';

  const NEWGAME_ENTRY = {
      canJoin: true,
      label: "New Game",
  }

  const NewGameEvent = {
    "@type" : "NewGameEvent",
    eventType : "NEWGAME",
    gameId : null,
    playerId : "all",
    details : {
        screenHandle : null,
        emailAddr : null,
        playerToken : null,
        playerSecret : null
    }
  }

  const JoinGameEvent ={
      "@type" : "RegisterEvent",
      eventType : "REGISTER",
      details : {
          screenHandle : null,
          emailAddr : null,
          playerToken : null,
          playerSecret : null
      },
      "gameId" : null
  }

  export default {
      data () {
        return {
          games: [],
          errored: false,
          currentGameId: null,
          loading: true,
          waitForStart: false,
          secret: null,
          emailAddr: null,
          screenHandle: null,
          dialog: false,
          dialogLabel: 'Create',
          enabled: null,
          search: null,
          headerArray: [
            {
              text: 'Game ID',
              align: 'start',
              sortable: false,
              value: 'gameId',
            },
            { text: 'Owner', value: 'owner' },
            { text: 'Opponent', value: 'opponent' },
            { text: 'Status', value: 'gameState' },
            { text: 'Actions', value: 'actions' },
          ],
        }
      },

    computed: {
        ...mapGetters(['getGameList','getLastStatus'])
    },

    created() {
        this.$store.dispatch('fetchGameList')
            .then(() => {
                this.loading = false
              })
    },

    watch: {
        getGameList : function() {
            var tmpList = [NEWGAME_ENTRY]
            this.getGameList.forEach( (g) => {
                var t = g
                if (t.canJoin) {
                    t.label = "Join"
                }
                tmpList.push(t)
            })
            this.games = tmpList
        },
        getLastStatus : function() {
            console.info("getLastStatus changed: " + JSON.stringify(this.getLastStatus))
            this.waitForStart = false
            this.dialog = false
            this.$store.dispatch('fetchGameList')
            this.$router.push("/play")
        }
    },

    methods: {
      ...mapActions(['fetchGameList']),

      startOrJoin() {
          console.info(this.dialogLabel + " startOrJoin Game: " +
          this.screenHandle + " " +
          this.emailAddr + " " +
          this.secret)

          var event = NewGameEvent
          if (this.dialogLabel == "Join") {
              event = JoinGameEvent
              event.gameId = this.currentGameId
          }

          event.details.screenHandle = this.screenHandle
          event.details.emailAddr = this.emailAddr
          event.details.playerSecret = this.secret

          console.info("Adding event: "+JSON.stringify(event))
          this.$store.dispatch("addEventOut", event)

          this.waitForStart = true
      },
      openDialog (item) {
          this.screenHandle=""
          this.emailAddr=""
          this.secret=""
          this.currentGameId = item.gameId
          this.dialog = true
          this.dialogLabel = item.label
      },
    },
  }
</script>
