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

    <template v-slot:item.actions="{ item }">
          <v-btn color="primary"
          @click="openDialog(item)"
           v-if="isGameAvailable(item)">
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
                  label="Secret*"
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
  import axios from 'axios'
  var games2 = [
     {
            gameId: null,
            owner: null,
            opponent: null,
            gameState: null,
            label: 'New Game',

     },
  ]


  export default {
      data () {
        return {
          games: [],
          errored: false,
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
    mounted() {
        console.info("On mounted called.")
        this.games = [{
                    gameId: null,
                    owner: null,
                    opponent: null,
                    gameState: null,
                    label: 'New Game',},]
        axios
        .get('http://localhost:8090/scopa/gamelist')
        .then((response) => {
          console.info("data:"+JSON.stringify(response.data))

          for (var i = 0; i < response.data.length; i++) {
              this.games.push({})
              this.games[i+1].gameId = response.data[i].gameId
              this.games[i+1].gameState = response.data[i].gameState
              this.games[i+1].label = "Create New"
              if (response.data[i].playerList.length > 0) {
                  this.games[i+1].owner = response.data[i].playerList[0]
                  this.games[i+1].label = "Join"
              }
              if (response.data[i].playerList.length > 1) {
                  this.games[i+1].opponent = response.data[i].playerList[1]
                  this.games[i+1].label = null
              }
          }
        })
        .catch(error => {
          console.log(error)
          this.errored = true
        })
        .finally(() => this.loading = false)
    },



    computed: {
    },

    watch: {
    },

    methods: {
      isGameAvailable(item) {
          return item.opponent == null
      },
      startOrJoin() {
          console.info("Join Game: " +
          this.screenHandle + " " +
          this.emailAddr + " " +
          this.secret)
          this.waitForStart = true
      },
      openDialog (item) {
          this.screenHandle=""
          this.emailAddr=""
          this.secret=""
          this.dialog = true
          this.dialogLabel = item.label
      },
    },
  }
</script>
