<template>
    <slot v-if="condition"></slot>
    <p style="overflow-wrap: break-word; text-align: center" v-else>NULL</p>
</template>

<script lang="ts">
export default {
  name: 'InfoOrNull',
  props : ['condition']
}
</script>