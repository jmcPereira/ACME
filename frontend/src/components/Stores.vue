<template>
  <div class="container">
    <div style="min-width:1115px" class="card mt-5">
      <h2 class="card-header text-center">Store Information</h2>
      <div style="text-align: center" class="m-3" v-cloak>
        <p v-if="stores.length > 0">showing {{ stores.length }} items</p>
        <p v-if="!dataIsAvailable">Server side data is not ready yet. Should auto refresh soon!</p>
        <a v-if="dataIsAvailable" href="/api/acme.csv">Download information as CSV</a>
      </div>
      <div style="padding-bottom: 20px" v-if="dataIsAvailable && stores" class="d-flex justify-content-center">
        <v-pagination
            v-model="page"
            :pages="numberOfPages"
            :range-size="0"
            active-color="#DCEDFF"
            @update:modelValue="updatePageHandler"
        />
      </div>
      <table v-if="dataIsAvailable" class="table table-striped">
        <thead class="thead-light text-center">
        <tr>
          <th>Store Id</th>
          <th>Store Code</th>
          <th>Store Description</th>
          <th>
            <div class="row d-flex justify-content-center" style="max-width: 250px">
              <span>Store Name</span>
              <input class="col-6" v-model="nameFilter" placeholder="name filter">
              <div class="col-3">
                <button class="btn btn-sm btn-outline-dark" @click="fetchStoresPage(page)">Apply</button>
              </div>
              <div class="col-2">
                <button style="padding: 10px" class="btn btn-close btn-sm" @click="resetFilter"></button>
              </div>
            </div>
          </th>
          <th>Store Opening Date</th>
          <th>Store Type</th>
          <th>Seasons</th>
          <th>Additional Information</th>
        </tr>
        </thead>
        <tbody v-cloak>
        <tr v-for="s in stores" :key="s.id">
          <td class="col-1">
            <info-or-null :condition="s.id">{{ s.id }}</info-or-null>
          </td>
          <td class="col-1" style="max-width: 50px">
            <info-or-null :condition="s.code">{{ s.code }}</info-or-null>
          </td>
          <td class="col-1">
            <info-or-null :condition="s.description">
              <a class="" v-if="!s.readMoreActivated" @click="activateReadMore(s)" href="#">
                read more...
              </a>
              <a v-if="s.readMoreActivated" @click="activateReadMore(s)" href="#">
                read less...
              </a>
              <span v-if="!s.readMoreActivated">{{ s.description.slice(0, 100) }}</span>
              <span v-if="s.readMoreActivated" v-html="s.description"></span>
            </info-or-null>
          </td>
          <td class="col-3">
            <info-or-null :condition="s.name">
              <div class="row d-flex justify-content-center" style="max-width: 200px">
                <input class="col-8 text-center" v-model="s.name">
                <div class="col-4">
                  <button class="btn btn-sm btn-outline-dark" @click="update(s)">Update</button>
                </div>
              </div>
            </info-or-null>
          </td>
          <td class="col-2">
            <info-or-null :condition="s.openingDate"><p style="text-align: center">{{ s.openingDate }}</p>
            </info-or-null>
          </td>
          <td class="col-1" style="text-align: center">
            <info-or-null :condition="s.storeType">{{ s.storeType }}</info-or-null>
          </td>
          <td class="col-1">
            <info-or-null :condition="hasSeasons(s)">
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
                <li v-if="isValidField(s.additionalInfo.specialField1)">
                  <p>{{ s.additionalInfo.specialField1 }}</p>
                </li>
                <li v-if="isValidField(s.additionalInfo.specialField2)">
                  <p>{{ s.additionalInfo.specialField2 }}</p>
                </li>
              </ul>
            </info-or-null>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script lang="ts">
import VPagination from "@hennge/vue3-pagination";
import "@hennge/vue3-pagination/dist/vue3-pagination.css";
import InfoOrNull from "./InfoOrNull"

export default {
  name: 'Stores',
  components: {
    VPagination,
    InfoOrNull
  },
  methods: {
    resetFilter: function () {
      this.nameFilter = ""
      this.fetchStoresPage(this.page)
    },
    hasSeasons: function (store) {
      return Array.isArray(store.seasons) && store.seasons.length
    },
    isValidField: function (f) {
      return f && f.length > 0
    },
    hasAdditionalInformation: function (store) {
      return store.additionalInfo && (this.isValidField(store.additionalInfo.specialField1) || this.isValidField(store.additionalInfo.specialField2))
    },
    activateReadMore(store) {
      store.readMoreActivated = !store.readMoreActivated;
    },
    updatePageHandler: function (page) {
      this.fetchStoresPage(page)
    },
    fetchStoresPage: function (page) {
      this.stores = []
      fetch(`/api/getStores?page=${page - 1}&storeNameFilter=${encodeURIComponent(this.nameFilter)}`)
          .then(response => response.json())
          .then(data => {
            this.stores = data.storesPage;
            this.numberOfPages = data.totalPages;
            this.dataIsAvailable = true
          });
    },
    update: function (store) {
      fetch(`/api/updateStore`, {method: 'PUT', body: JSON.stringify(store)})
          .then(response => console.log(response.text()))
    },
    isDataAvailable: async function () {
      let result = await (await fetch(`/api/isDataAvailable`)).text() == "true";
      this.dataIsAvailable = result;
      return result;
    }
  },
  data() {
    return {
      stores: [],
      page: 1,
      numberOfPages: 1,
      nameFilter: "",
      dataIsAvailable: false
    }
  },
  created() {
    this.isDataAvailable()
        .then(result => {
          if (result) {
            this.fetchStoresPage(this.page)
          } else {
            const interval = setInterval(function () {
              this.isDataAvailable()
                  .then(isAvailable => {
                    if (isAvailable) {
                      this.fetchStoresPage(this.page)
                      clearInterval(interval)
                    }
                  })
            }.bind(this), 5000);
          }
        })
  }
}
</script>

<style>
[v-cloak] {
  display: none;
}

.Page {
  padding: 15px;
}
</style>