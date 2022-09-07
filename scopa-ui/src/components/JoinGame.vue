<template>
    <v-card>
    <v-card-title>Games in Progress</v-card-title>
    <v-card-text>Join a game in progress or start a new game.
    </v-card-text>

    <v-data-table
      :headers="headerArray"
      :items="itemsArray"
      :loading="isLoading"
      item-key="gameId"
      class="elevation-1"
    >

      <template
        v-if="false"
        v-slot:progress
      >
        <v-progress-linear
          color="purple"
          :height="10"
          indeterminate
        ></v-progress-linear>
      </template>


    <template v-slot:item.actions="{ item }">
          <v-btn color="primary"
           v-if="isGameAvailable(item)">
            {{item.label}}
          </v-btn>
    </template>


    </v-data-table>
    </v-card>
</template>

<script>
  const games = [
     {
            gameId: null,
            Owner: null,
            opponent: null,
            status: null,
            label: 'New Game',

     },
     {
      gameId: '111111',
      owner: 'chip',
      opponent: 'someone',
      status: 'wait_4_player1',
    },
    {
            gameId: '222222',
            Owner: 'chip',
            opponent: null,
            status: 'not started',
            label: 'Join',
     },


  ]

  export default {
    data () {
      return {
        enabled: null,
        itemsArray: games,
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
          { text: 'Status', value: 'status' },
          { text: 'Actions', value: 'actions' },
        ],
      }
    },

    computed: {
      isLoading () {
        return false
      },
    },

    watch: {
    },

    methods: {
      isGameAvailable(item) {
          console.info('Chip:'+JSON.stringify(item))
          return item.opponent == null
      },
      joinGame (item) {
        console.info("chip joinGame:"+JSON.stringify(item))
               return item
              },
              newGame() {
              },

    },
  }
</script>
