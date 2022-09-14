<template>
    <v-card width="70%" style="padding: 1%">
      <v-item-group tag="opponent-group">
        <v-row class="fill-height ma-0"
               align="center"
               justify="center"
               style="padding: 1%"
               width="100%">
          <v-card style="padding: 1%">
            <v-text-field label="Opponent" outlined :value="opponentName" clearable/>
            <v-text-field label="Score" outlined :value="opponentScore" disabled/>
          </v-card>
          <v-spacer max-width="10%"/>
          <v-item v-for="n in 3" :key="n" v-slot="{active, toggle}">
            <v-card :key="n" width="15%" style="padding: 1%"
                    @click="toggle"
                    :color="active?'primary':''">
              <v-img
                  :src="opponentCard(n)"
                  :lazy-src="opponentCard(n)"
                  aspect-ratio="16/9"
                  width="100%"
                  class="grey lighten-2"
                  style="padding: 1%"
              >
                <template v-slot:placeholder>
                  <v-row
                      class="fill-height ma-0"
                      align="center"
                      justify="center"
                  >
                    <v-progress-circular
                        indeterminate
                        color="grey lighten-5"
                    ></v-progress-circular>
                  </v-row>
                </template>
              </v-img>
            </v-card>
          </v-item>
          <v-spacer width="35%"/>
        </v-row>
      </v-item-group>
      <v-item-group tag="table-group" multiple>
        <v-row class="fill-height ma-0"
               align="center"
               justify="center"
               style="padding: 1%"
               width="100%"
        >
          <v-card width="15%" style="padding: 1%" elevation="20">
            <v-card style="padding: 1%">
              <v-img
                  src="@/assets/scopa.jpg"
                  lazy-src="@/assets/scopa.jpg"
                  aspect-ratio="16/9"
                  width="100%"
                  class="grey lighten-2"
                  style="padding: 1%"
              >
                <template v-slot:placeholder>
                  <v-row
                      class="fill-height ma-0"
                      align="center"
                      justify="center"
                  >
                    <v-progress-circular
                        indeterminate
                        color="grey lighten-5"
                    ></v-progress-circular>
                  </v-row>
                </template>
              </v-img>
            </v-card>
            <v-divider style="padding: 5%"/>
            <v-text-field style="padding: 5%" dense outlined label="Cards" :value="cardsLeft"/>
          </v-card>
          <v-spacer width="10%"/>
          <v-card width="60%" elevation="10" style="padding: 2%">
            <v-row v-for="r in 2"
                   :key="r"
                   width="100%" style="padding:1%">
              <v-item v-for="i in 5"
                      :key="i"
                      v-slot="{active, toggle}">
                <v-card width="20%"
                :color="active ? 'primary' : ''"
                        style="padding: 2%"
                @click="toggle">
              <v-img
                  :src="tableCard((r-1)*5+i)"
                  :lazy-src="tableCard((r-1)*5+i)"
                  aspect-ratio="16/9"
                  width="100%"
                  class="grey lighten-2">
                <template v-slot:placeholder>
                  <v-row
                      class="fill-height ma-0"
                      align="center"
                      justify="center"
                  >
                    <v-progress-circular
                        indeterminate
                        color="grey lighten-5"
                    ></v-progress-circular>
                  </v-row>
                </template>
              </v-img>
                </v-card>
              </v-item>
            </v-row>
          </v-card>
          <v-spacer width="20%"/>
        </v-row>
      </v-item-group>
      <v-item-group tag="player-group">
      <v-row class="fill-height ma-0"
             align="center"
             justify="center"
             style="padding: 1%"
             width="100%"
      >
        <v-card style="padding: 1%">
          <v-text-field label="You" outlined :value="playerName" clearable/>
          <v-text-field label="Score" outlined :value="playerScore" disabled/>
        </v-card>
        <v-spacer max-width="10%"/>
        <v-item v-for="n in 3" :key="n" v-slot="{active,toggle}">
        <v-card width="15%" style="padding: 1%"
        @click="toggle"
        :color="active?'primary':''">
          <v-img
              :src="playerCard(n)"
              :lazy-src="playerCard(n)"
              aspect-ratio="16/9"
              width="100%"
              class="grey lighten-2"
          >
            <template v-slot:placeholder>
              <v-row
                  class="fill-height ma-0"
                  align="center"
                  justify="center"
              >
                <v-progress-circular
                    indeterminate
                    color="grey lighten-5"
                ></v-progress-circular>
              </v-row>
            </template>
          </v-img>
        </v-card>
        </v-item>
        <v-spacer width="35%"/>
      </v-row>
      </v-item-group>
    </v-card>
</template>

<script>
import { mapGetters } from 'vuex';
const cardfaces = {
   "empty" : require("@/assets/empty.jpg"),
   "1-swords" : require("@/assets/1-swords.jpg"),
   "2-swords" : require("@/assets/2-swords.jpg"),
   "3-swords" : require("@/assets/3-swords.jpg"),
   "4-swords" : require("@/assets/4-swords.jpg"),
   "5-swords" : require("@/assets/5-swords.jpg"),
   "6-swords" : require("@/assets/6-swords.jpg"),
   "7-swords" : require("@/assets/7-swords.jpg"),
   "8-swords" : require("@/assets/8-swords.jpg"),
   "9-swords" : require("@/assets/9-swords.jpg"),
   "10-swords" : require("@/assets/10-swords.jpg"),
   "1-coins" : require("@/assets/1-coins.jpg"),
   "2-coins" : require("@/assets/2-coins.jpg"),
   "3-coins" : require("@/assets/3-coins.jpg"),
   "4-coins" : require("@/assets/4-coins.jpg"),
   "5-coins" : require("@/assets/5-coins.jpg"),
   "6-coins" : require("@/assets/6-coins.jpg"),
   "7-coins" : require("@/assets/7-coins.jpg"),
   "8-coins" : require("@/assets/8-coins.jpg"),
   "9-coins" : require("@/assets/9-coins.jpg"),
   "10-coins" : require("@/assets/10-coins.jpg"),
   "1-cups" : require("@/assets/1-cups.jpg"),
   "2-cups" : require("@/assets/2-cups.jpg"),
   "3-cups" : require("@/assets/3-cups.jpg"),
   "4-cups" : require("@/assets/4-cups.jpg"),
   "5-cups" : require("@/assets/5-cups.jpg"),
   "6-cups" : require("@/assets/6-cups.jpg"),
   "7-cups" : require("@/assets/7-cups.jpg"),
   "8-cups" : require("@/assets/8-cups.jpg"),
   "9-cups" : require("@/assets/9-cups.jpg"),
   "10-cups" : require("@/assets/10-cups.jpg"),
   "1-scepters" : require("@/assets/1-scepters.jpg"),
   "2-scepters" : require("@/assets/2-scepters.jpg"),
   "3-scepters" : require("@/assets/3-scepters.jpg"),
   "4-scepters" : require("@/assets/4-scepters.jpg"),
   "5-scepters" : require("@/assets/5-scepters.jpg"),
   "6-scepters" : require("@/assets/6-scepters.jpg"),
   "7-scepters" : require("@/assets/7-scepters.jpg"),
   "8-scepters" : require("@/assets/8-scepters.jpg"),
   "9-scepters" : require("@/assets/9-scepters.jpg"),
   "10-scepters" : require("@/assets/10-scepters.jpg"),
   "cardback" : require("@/assets/cardback.jpg"),
   "scopa" : require("@/assets/scopa.jpg"),
}

export default {
  name: 'GameBoard',
  computed: {
      ...mapGetters(['getLastStatus'])
  },
 watch: {
        getLastStatus : function() {
            console.info("getLastStatus changed: " + JSON.stringify(this.getLastStatus))
            this.refreshGameInfo()
        }
    },
  methods: {
      refreshGameInfo: function() {
            console.log("refreshGameInfo: " + JSON.stringify(this.getLastStatus))
            this.numCardsInOpponentsHand = this.getLastStatus.status.opponentCardCount
            this.cardsLeft = this.getLastStatus.status.cardsRemaining
            this.gameId = this.getLastStatus.status.gameId
            this.myhand = this.getLastStatus.status.playerHand
            this.tableCards = this.getLastStatus.status.table
            this.opponentName = 'TBD'
            this.playerName = this.getLastStatus.status.playerDetails.screenHandle

      },
      opponentCard: function(idx) {
          console.log("opponentCard idx:" + idx)

          var file = "empty"
          console.log("opponentCard: "+this.numCardsInOpponentsHand)
          if (idx <= this.numCardsInOpponentsHand)
          {
              file = "scopa"
          }
          return cardfaces[file]
      },
      tableCard: function(idx) {
          console.log("tableCard: idx:" + idx + " list: " + JSON.stringify(this.tableCards))
          return this.createAssetName(this.tableCards, idx)
      },
      playerCard: function(idx) {
          console.log("playerCard: idx:" + idx + " list: " + JSON.stringify(this.myhand))
          return this.createAssetName(this.myhand, idx)
      },
      createAssetName(list, idx) {
          console.log("createAssetName: idx:"+idx+" list:"+JSON.stringify(list))
          var file = "empty"
          if (idx <= list.length) {
              file = list[idx-1].val + "-" + list[idx-1].suit.toLowerCase()
          }

          console.log('createAssetName:['+cardfaces[file]+']')

          return cardfaces[file]
      }

  },
  created() {
      console.log("created")
      this.refreshGameInfo()
  },

  data: () => ({
        numCardsInOpponentsHand: 0,
        playerName: 'unknown',
        opponentName: 'unknown',
        opponentScore: 0,
        playerScore: 0,
        cardsLeft: 0,
        gameId: null,
        myhand: [],
        tableCards:[],

  }),
}
</script>
