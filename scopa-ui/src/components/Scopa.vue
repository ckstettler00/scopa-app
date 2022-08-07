<template>
  <v-container style="padding: 5%">
    <v-card width="70%" style="padding: 1%">
      <v-item-group tag="player1-group">
        <v-row class="fill-height ma-0"
               align="center"
               justify="center"
               style="padding: 1%"
               width="100%">
          <v-card style="padding: 1%">
            <v-text-field label="Player1 Name" outlined value="Player1" clearable/>
            <v-text-field label="Score" outlined value="0" disabled/>
          </v-card>
          <v-spacer max-width="10%"/>
          <v-item v-for="n in 3" :key="n" v-slot="{active, toggle}">
            <v-card :key="n" width="15%" style="padding: 1%"
                    @click="toggle"
                    :color="active?'primary':''">
              <v-img
                  src="../assets/scopa.jpeg"
                  lazy-src="../assets/scopa.jpeg"
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
                  src="../assets/scopa.jpeg"
                  lazy-src="../assets/scopa.jpeg"
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
            <v-text-field style="padding: 5%" dense outlined label="Cards" value="40"/>
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
                  src="../assets/10-swords.jpg"
                  lazy-src="../assets/10-swords.jpg"
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
      <v-item-group tag="player2-group">
      <v-row class="fill-height ma-0"
             align="center"
             justify="center"
             style="padding: 1%"
             width="100%"
      >
        <v-card style="padding: 1%">
          <v-text-field label="Player2 Name" outlined value="Player2" clearable/>
          <v-text-field label="Score" outlined value="0" disabled/>
        </v-card>
        <v-spacer max-width="10%"/>
        <v-item v-for="n in 3" :key="n" v-slot="{active,toggle}">
        <v-card width="15%" style="padding: 1%"
        @click="toggle"
        :color="active?'primary':''">
          <v-img
              src="../assets/scopa.jpeg"
              lazy-src="../assets/scopa.jpeg"
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
  </v-container>
</template>

<script>
export default {
  name: 'Scopa',

  data: () => ({
    maxWidth: 90,
    ecosystem: [
      {
        text: 'vuetify-loader',
        href: 'https://github.com/vuetifyjs/vuetify-loader',
      },
      {
        text: 'github',
        href: 'https://github.com/vuetifyjs/vuetify',
      },
      {
        text: 'awesome-vuetify',
        href: 'https://github.com/vuetifyjs/awesome-vuetify',
      },
    ],
    importantLinks: [
      {
        text: 'Documentation',
        href: 'https://vuetifyjs.com',
      },
      {
        text: 'Chat',
        href: 'https://community.vuetifyjs.com',
      },
      {
        text: 'Made with Vuetify',
        href: 'https://madewithvuejs.com/vuetify',
      },
      {
        text: 'Twitter',
        href: 'https://twitter.com/vuetifyjs',
      },
      {
        text: 'Articles',
        href: 'https://medium.com/vuetify',
      },
    ],
    whatsNext: [
      {
        text: 'Explore components',
        href: 'https://vuetifyjs.com/components/api-explorer',
      },
      {
        text: 'Select a layout',
        href: 'https://vuetifyjs.com/getting-started/pre-made-layouts',
      },
      {
        text: 'Frequently Asked Questions',
        href: 'https://vuetifyjs.com/getting-started/frequently-asked-questions',
      },
    ],
  }),
}
</script>
