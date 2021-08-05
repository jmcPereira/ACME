<template>
  <tbody v-cloak>
  <tr v-for="s in stores" :key="s.id">
    <td class="col-1">
      <info-or-null :condition="s.id">{{ s.id }}</info-or-null>
    </td>
    <td class="store-code col-1">
      <info-or-null :condition="s.code">{{ s.code }}</info-or-null>
    </td>
    <td class="col-1">
      <info-or-null :condition="s.description">
        <ReadMore :description="s.description"></ReadMore>
      </info-or-null>
    </td>
    <td class="col-3">
      <info-or-null :condition="s.name">
        <div class="store-edit-name row d-flex justify-content-center">
          <input class="col-8 text-center" v-model="s.name">
          <div class="col-4">
            <button class="btn btn-sm btn-outline-dark" @click="update(s)">Update</button>
          </div>
        </div>
      </info-or-null>
    </td>
    <td class="col-2">
      <info-or-null :condition="s.openingDate"><p class="cen">{{ s.openingDate }}</p>
      </info-or-null>
    </td>
    <td class="col-1 text-center">
      <info-or-null :condition="s.storeType">{{ s.storeType }}</info-or-null>
    </td>
    <td class="col-1">
      <info-or-null :condition="s.seasons.length">
        <ul>
          <li v-for="season in s.seasons" :key="season.id">
            {{ season.season }}
          </li>
        </ul>
      </info-or-null>
    </td>
    <td class="col-2">
      <info-or-null :condition="hasAdditionalInformation(s)">
        <ul>
          <li v-if="s.additionalInfo.specialField1">
            <p>{{ s.additionalInfo.specialField1 }}</p>
          </li>
          <li v-if="s.additionalInfo.specialField2">
            <p>{{ s.additionalInfo.specialField2 }}</p>
          </li>
        </ul>
      </info-or-null>
    </td>
  </tr>
  </tbody>
</template>

<script lang="ts">
import {Vue, Options} from 'vue-class-component';
import Store from "@/components/interfaces/Store";
import InfoOrNull from "@/components/InfoOrNull.vue";
import ReadMore from "@/components/ReadMore.vue";

@Options({
  props :['stores'],
  components:{
    InfoOrNull,
    ReadMore
  }
})
export default class StoresPage extends Vue {
  stores = new Array<Store>();

  hasAdditionalInformation(store: Store) {
    return store.additionalInfo && (store.additionalInfo.specialField1 || store.additionalInfo.specialField2);
  }

  update(store: Store) {
    fetch(`http://localhost:3000/api/updateStore`, {method: 'PUT', body: JSON.stringify(store), headers:{'Content-Type' : 'application/json'}})
        .then(response => console.log(response.text()));
  }
}
</script>

<style scoped>
.store-code {
  max-width: 50px;
}

.store-edit-name {
  max-width: 200px;
}
</style>