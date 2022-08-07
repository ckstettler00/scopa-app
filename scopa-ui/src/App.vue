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
    </v-app-bar>

    <v-main>
      <Scopa/>
    </v-main>
  </v-app>
</template>

<script>
import Scopa from './components/Scopa';

export default {
  name: 'App',

  components: {
    Scopa,
  },

  data: () => ({
    //
  }),
};
</script>
