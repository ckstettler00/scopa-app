<template>
    <v-card
    :loading = "loading"
    >
    <v-card-title>Games in Progress</v-card-title>
    <v-card-text>Join a game in progress or start a new game.
    </v-card-text>

    <v-data-table
      :headers="headerArray"
      :items="getGameList"
      item-key="gameId"
      class="elevation-1"
    >

    <template v-slot:[`item.actions`]="{ item }">
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
  import { mapGetters, mapActions } from 'vuex';
  import store from '../store/index';


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

    computed: {
        ...mapGetters(['getGameList'])
    },

    created() {
        store.dispatch('fetchGameList')
            .then(() => {
        console.info("store:"+JSON.stringify(this.$store.getters))
                this.loading = false
              })

    },

    watch: {
    },

    methods: {
      ...mapActions(['fetchGameList']),



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
