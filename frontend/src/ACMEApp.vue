<template>
  <Stores/>
</template>

<script lang="ts">
import { Options, Vue } from 'vue-class-component';
import Stores from "@/components/Stores.vue";

@Options({
  components: {
    Stores,
  },
})
export default class ACMEApp extends Vue {}
</script>

<style>
</style>
