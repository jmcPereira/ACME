<template>
  <a @click="flip()" href="#">
    <p v-if="isActive"> read less... </p>
    <p v-else> read more... </p>
  </a>
  <span v-if="!isActive">{{ description.slice(0, 100) }}</span>
  <span v-else v-html="description"></span>
</template>

<script lang="ts">
import {Vue, Options} from 'vue-class-component';

@Options({
  props: ['description']
})
export default class ReadMore extends Vue{
  description = "";
  isActive = false;
  flip(){
    this.isActive = !this.isActive;
  }
}
</script>

<style scoped>
</style>