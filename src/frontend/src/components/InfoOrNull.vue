<template>
    <slot v-if="condition"></slot>
    <p style="overflow-wrap: break-word" v-else>NULL</p>
</template>

<script>

export default {
  name: 'InfoOrNull',
  props : ['condition']
}
</script>