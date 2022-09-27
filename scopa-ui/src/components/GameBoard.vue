<template>
<v-container>
  <v-layout class="pa-3" row>
    <v-flex class="pa-2" md2>
        <v-card class="pa-2">
            <v-text-field label="Opponent" outlined :value="opponentName" clearable/>
            <v-text-field label="Score" outlined :value="opponentScore" disabled/>
        </v-card>
    </v-flex>
    <v-flex md3>
    </v-flex>
    <v-flex md6>
        <v-layout row>
            <v-flex class="pa-2" md2  v-for="n in 3" :key="n">
                <v-card  class="pa-2"
                    outlined
                    shaped
                    tile>
                    <v-img
                      :src="opponentCard(n)"
                      class="grey lighten-2 pa-2"
                    >
                    </v-img>
                </v-card>
            </v-flex>
        </v-layout>
        <v-layout row>
            <v-flex fill-height class="pa-2" md6>
                <div></div>
            </v-flex>
        </v-layout>
    </v-flex>
    <v-flex md1></v-flex>
  </v-layout>
  <v-layout class="pa-2">
    <v-divider></v-divider>
  </v-layout>
  <v-layout class="pa-3" row>
    <v-flex class="pa-2" md2>
        <v-layout class="pa-2" row>
            <v-flex class="pa-2" md6>
                <v-card>
                  <v-img
                      src="@/assets/scopa.jpg"
                      class="grey lighten-2"
                  >
                  </v-img>
                  <v-text-field class="pa-2" dense outlined label="Cards" :value="cardsLeft"/>
                </v-card>
            </v-flex>
        </v-layout>
        <v-layout class ="pa-2" row>
            <v-flex class="pa-2" md6>
                <div fill-height ></div>
            </v-flex>
        </v-layout>
    </v-flex>
    <v-flex md2>
    </v-flex>
    <v-flex class="pa-2" md6>
      <v-item-group tag="table" v-model="tableSelection" multiple>
            <v-layout v-for="r in 2" :key="r">
              <v-flex  class="pa-2" md2 v-for="c in 6" :key="c">
                  <v-item  v-slot="{active, toggle}">
                      <v-card class="pa-2"
                         :color="active ? 'primary' : ''"

                          @click="toggle"
                          v-on:click="toggleTableCards()"
                    :disabled="!isCardVisible(tableCards,(r-1)*6+c)">
                          <v-img :src="tableCard((r-1)*6+c)"
                              class="grey lighten-2">
                          </v-img>
                      </v-card>
                  </v-item>
              </v-flex>
            </v-layout>
        </v-item-group>
    </v-flex>
    <v-flex md2>
    </v-flex>
  </v-layout>
  <v-layout class="pa-2">
    <v-divider></v-divider>
  </v-layout>
  <v-layout row class="pa-3">
    <v-flex class="pa-2" md2>
        <v-card class="pa-2">
          <v-text-field label="You" outlined :value="playerName" clearable/>
          <v-text-field label="Score" outlined :value="playerScore" disabled/>
        </v-card>
    </v-flex>
    <v-flex align="center" md3>
        <v-layout class="pa-2" row>
            <v-flex fill-height>
            </v-flex>
        </v-layout>
        <v-layout class="pa-2" row>
            <v-flex md3 lg3>
                <div>
                                <v-img v-if="isPlayerTurn"
                                  src="@/assets/right-arrow.png"
                                  class="grey lighten-2"
                                  style="transform: rotate(0deg)"
                                >
                                </v-img>
                </div>
            </v-flex>
        </v-layout>
        <v-layout class="pa-2">
            <v-flex fill-height>
            </v-flex>
        </v-layout>
    </v-flex>
    <v-flex md6>
        <v-item-group tag="player-group" v-model="myhandSelection">
            <v-layout class="pa-2" row>
                    <v-flex class="pa-2" fill-height md6>
                        <div align="center" >
                             <v-btn
                                v-on:click="makePlay()"
                                rounded
                                color="primary"
                                v-show="isPlayerTurn"
                                :disabled="!isPlayButtonEnabled"
                            >
                                {{playButtonText}}
                            </v-btn>
                        </div>
                    </v-flex>
            </v-layout>
            <v-layout class="pa-2" row>
                <v-flex  class="pa-2" md2  v-for="n in 3" :key="n">
                <v-item  v-slot="{active, toggle}">
                    <v-card  class="pa-2"
                        @click="toggle"
                        v-on:click = "toggleHandCards()"
                        :color="active?'primary':''"
                        outlined
                        shaped
                        tile
                        :disabled="!isCardVisible(myhand, n)"
                        >
                        <v-img
                          :src="playerCard(n)"
                          class="grey lighten-2"
                        >
                        </v-img>
                    </v-card>
                    </v-item>
                </v-flex>
            </v-layout>
        </v-item-group>
    </v-flex>
    <v-flex md1></v-flex>
  </v-layout>
</v-container>
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

const Discard = {
                 "@type" : "Discard",
                 "type" : "DISCARD",
                 "discarded" : {
                   "val" : null,
                   "suit" : null,
                 }
               }
const Pickup = {
                 "@type" : "Pickup",
                 "type" : "PICKUP",
                 "playerCard" : {
                   "val" : null,
                   "suit" : null,
                 },
                 "tableCards" : []
               }

const PlayResponseEvent = {
                            "@type" : "PlayResponseEvent",
                            "eventType" : "PLAY_RESP",
                            "gameId" : null,
                            "playerId" : null,
                            "move" : null,
                          }
export default {
  name: 'GameBoard',
  computed: {
      ...mapGetters(['getLastStatus','getLastError'])
  },
 watch: {
        getLastStatus : function() {
            console.info("getLastStatus changed: " + JSON.stringify(this.getLastStatus))
            this.refreshGameInfo()
        },
        getLastError : function() {
            console.info("error handler")
            this.snackbar = true
            this.errorText = JSON.stringify(this.getLastError())
        }
    },
  methods: {
      refreshGameInfo: function() {
            console.log("refreshGameInfo: " + JSON.stringify(this.getLastStatus))
            this.numCardsInOpponentsHand = this.getLastStatus.status.opponentCardCount
            this.cardsLeft = this.getLastStatus.status.cardsRemaining
            this.gameId = this.getLastStatus.status.gameId

            console.info("playerHand:"+JSON.stringify(this.myhand))
            var hand = []
            this.getLastStatus.status.playerHand.forEach(function(c) {
                console.info("playerHand card:"+JSON.stringify(c))
                hand.push({card: c, active: false})
            })
            this.myhand = hand

            console.info("playerHand: "+JSON.stringify(this.myhand))

            hand = []
            this.getLastStatus.status.table.forEach(function(c) {
                hand.push({card: c, active: false})
            })
            this.tableCards = hand
            console.info("tableHand: "+JSON.stringify(this.tableCards))

            this.opponentName = this.getLastStatus.status.opponentDetails.screenHandle
            this.opponentScore = this.getLastStatus.status.opponentScore
            this.playerName = this.getLastStatus.status.playerDetails.screenHandle
            this.playerScore = this.getLastStatus.status.playerScore


            if (this.getLastStatus.status.playerDetails.playerId == this.getLastStatus.status.currentPlayerId) {
                this.isPlayerTurn = true
            } else {
                this.isPlayerTurn = false
            }

            this.updatePlayText()

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
              file = list[idx-1].card.val + "-" + list[idx-1].card.suit.toLowerCase()
          }

          console.log('createAssetName:['+cardfaces[file]+']')

          return cardfaces[file]
      },
      toggleHandCards() {
          console.info("toggleHandCards:"+JSON.stringify(this.myhandSelection))
          this.myhand.forEach(c => c.active = false)
          if (this.myhandSelection >= 0) {
              this.myhand[this.myhandSelection].active = true
          }
          console.info("toggleHandCards: "+JSON.stringify(this.myhand))
          this.updatePlayText()
      },
      toggleTableCards() {
          console.info("toggleTableCards:"+JSON.stringify(this.tableSelection))
          this.tableCards.forEach( c => c.active = false)
          if (this.tableSelection != null) {
              this.tableSelection.forEach(idx => this.tableCards[idx].active = true)
          }
          console.info("toggleTableCards: "+JSON.stringify(this.tableCards))
          this.updatePlayText()
      },
      isCardVisible(list, idx) {
          var val = (idx <= list.length)?true:false
          console.info("isCardVisible: idx:"+ idx + "ret:" + val + " list:"+JSON.stringify(list))
          return val
      },
      updatePlayText() {
          var text = "Select Cards"
          var enabled = false

          this.myhand.forEach(function(i) {
              if (i.active) {
                  text = "Discard"
                  enabled = true
              }
          })
          this.tableCards.forEach(function(i) {
              if (i.active) {
                  text = "Pickup Cards"
                  enabled = true
              }
          })
          this.playButtonText = text
          this.isPlayButtonEnabled = enabled
          console.info("button text:" + this.playButtonText + " enabled:" + this.isPlayButtonEnabled)
      },
      makePlay() {
                var event = PlayResponseEvent
                event.gameId = this.getLastStatus.status.gameId
                event.playerId = this.getLastStatus.status.playerDetails.playerId

                var move = Discard

                var pc = null
                console.info("makePlay: myhand:"+JSON.stringify(this.myhand))
                this.myhand.forEach(function(c) {
                    console.info("add play player card:"+JSON.stringify(c))
                    if (c.active) {
                        pc = { val: c.card.val, suit: c.card.suit}
                    }
                })

                var cl = []
                this.tableCards.forEach(function(c) {
                    console.info("add play table card:"+JSON.stringify(c))
                    if (c.active) {
                        cl.push({val: c.card.val, suit: c.card.suit})
                    }
                })

                if (cl.length > 0) {
                    move = Pickup
                    move.playerCard = pc
                    move.tableCards = cl
                } else {
                    move.discarded = pc
                }
                event.move = move

                // Clear selection
                this.myhandSelection = null
                this.tableSelection = null

                console.info("makePlay adding play event: "+JSON.stringify(event))
                this.$store.dispatch("addEventOut", event)

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
        myhandSelection: null,
        tableSelection: null,
        tableCards:[],
        playButtonText: 'Discard',
        isPlayerTurn: false,
        isPlayButtonEnabled: false,


  }),
}
</script>
