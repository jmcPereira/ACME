<template>
    <slot v-if="condition"></slot>
    <p class="null-info" v-else>NULL</p>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-class-component'
@Options({
  props : ['condition']
})
export default class InfoOrNull extends Vue{
  condition = false;
}
</script>

<style scoped>
.null-info{
  overflow-wrap: break-word; text-align: center
}
</style>