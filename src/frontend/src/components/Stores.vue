<template>
  <div class="container">
    <div style="min-width:1115px"  class="card mt-5">
      <h2 class="card-header text-center">Store Information</h2>
      <div style="text-align: center" class="m-3" v-cloak>
        <p v-if="stores">showing {{stores.length}} items</p>
        <p v-if="!stores">No Store information to display!</p>
        <a href="/test.csv" >Download information as CSV</a>
      </div>
      <div style="padding-bottom: 20px" v-if="stores" class="d-flex justify-content-center"><v-pagination
          v-model="page"
          :pages="numberOfPages"
          :range-size="0"
          active-color="#DCEDFF"
          @update:modelValue="updateHandler"
      /></div>
      <div id="spinner" class="loader">Loading...</div>
      <table class="table table-striped">
        <thead class="thead-light">
          <th class="text-center">Store Id</th>
          <th class="text-center">Store Code</th>
          <th class="text-center">Store Description</th>
          <th class="text-center col-3">
            <div class="row d-flex justify-content-center" style="max-width: 250px">
              <span>Store Name</span>
              <input class="col-6 text-center" v-model="nameFilter" placeholder="name filter">
              <div class="col-3">
                <button class="btn btn-sm btn-outline-dark" @click="fetchStoresPage(page)">Apply</button>
              </div>
              <div class="col-2">
                <button style="padding: 10px" class="btn btn-close btn-sm" @click="resetFilter"></button>
              </div>
            </div>
          </th>
          <th class="text-center">Store Opening Date</th>
          <th class="text-center">Store Type</th>
          <th class="text-center">Seasons</th>
          <th class="text-center">Additional Information</th>
        </thead>
        <tbody v-cloak>
          <tr v-for="s in stores" :key="s.id">
            <td class="col-1">
              <info-or-null :condition="s.id">{{s.id}}</info-or-null>
            </td>
            <td class="col-1" style="max-width: 50px">
              <info-or-null :condition="s.code">{{s.code}}</info-or-null>
            </td>
            <td class="col-1">
              <info-or-null :condition="s.description">
                <a class="" v-if="!s.readMoreActivated" @click="activateReadMore(s)" href="#">
                  read more...
                </a>
                <a v-if="s.readMoreActivated" @click="activateReadMore(s)" href="#">
                  read less...
                </a>
                <span v-if="!s.readMoreActivated">{{s.description.slice(0, 100)}}</span>
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
              <info-or-null :condition="s.openingDate">{{s.openingDate}}</info-or-null>
            </td>
            <td class="col-1" style="text-align: center">
              <info-or-null  :condition="s.storeType">{{s.storeType}}</info-or-null>
            </td>
            <td class="col-1"  style="text-align: center">
              <info-or-null :condition="hasSeasons(s)">
                <ul>
                  <li v-for="season in s.seasons" :key="season.id">
                    {{season.season}}
                  </li>
                </ul>
              </info-or-null>
            </td>
            <td class="col-2">
              <info-or-null style="text-align: center" :condition="hasAdditionalInformation(s)">
                <ul>
                  <li v-cloak v-if="isValidField(s.additionalInfo.specialField1)">
                    {{s.additionalInfo.specialField1}}
                  </li>
                  <li v-cloak v-if="isValidField(s.additionalInfo.specialField2)">
                    {{s.additionalInfo.specialField2}}
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

<script>
import VPagination from "@hennge/vue3-pagination";
import "@hennge/vue3-pagination/dist/vue3-pagination.css";
import InfoOrNull from "./InfoOrNull"

export default {
  name: 'Stores',
  computed: {
  },
  components :{
    VPagination,
    InfoOrNull
  },
  methods: {
    resetFilter: function(){
      this.nameFilter = ""
      this.fetchStoresPage(this.page)
    },
    hasSeasons: function(store){
      return Array.isArray(store.seasons) && store.seasons.length
    },
    isValidField: function (f) {
      return f && f.length > 0
    },
    hasAdditionalInformation: function(store){

      return store.additionalInfo && (this.isValidField(store.additionalInfo.specialField1) || this.isValidField(store.additionalInfo.specialField2))
    },
    activateReadMore(store){
      store.readMoreActivated = !store.readMoreActivated;
    },
    updateHandler: function(page) {
      this.fetchStoresPage(page)
    },
    fetchStoresPage: function(page) {
      this.stores = []
      document.getElementById("spinner").style.visibility = "visible"
      fetch(`/api/getStores?page=${page-1}&storeNameFilter=${encodeURIComponent(this.nameFilter)}`)
          .then(response =>response.json())
          .then(data => {
            console.log(data.storesPage)
            this.stores = data.storesPage;
            this.totalStores = data.totalStores;
            this.numberOfPages = Math.round(this.totalStores/this.itemsPerPage) + 1
            console.log(this.numberOfPages)
          });
      document.getElementById("spinner").style.visibility = "hidden"
    },
    update: function(store){
      fetch(`/api/updateStore`, {method: 'PUT', body : JSON.stringify(store)})
          .then(response =>console.log(response.text()))
    }
  },
  data() {
    return {
      stores: [],
      page : 1,
      numberOfPages : 1,
      itemsPerPage : 20,
      totalStores : 0,
      nameFilter : "",
      fetchingData : false
    }
  },
  mounted() {
    this.fetchStoresPage(this.page)
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
body {
  background-color: #eee
}
.loader:before,
.loader:after,
.loader {
  visibility: hidden;
  border-radius: 50%;
  width: 2.5em;
  height: 2.5em;
  -webkit-animation-fill-mode: both;
  animation-fill-mode: both;
  -webkit-animation: load7 1.8s infinite ease-in-out;
  animation: load7 1.8s infinite ease-in-out;
}
.loader {
  color: #cccccc;
  font-size: 10px;
  margin: 80px auto;
  position: relative;
  text-indent: -9999em;
  -webkit-transform: translateZ(0);
  -ms-transform: translateZ(0);
  transform: translateZ(0);
  -webkit-animation-delay: -0.16s;
  animation-delay: -0.16s;
}
.loader:before {
  left: -3.5em;
  -webkit-animation-delay: -0.32s;
  animation-delay: -0.32s;
}
.loader:after {
  left: 3.5em;
}
.loader:before,
.loader:after {
  content: '';
  position: absolute;
  top: 0;
}
@-webkit-keyframes load7 {
  0%,
  80%,
  100% {
    box-shadow: 0 2.5em 0 -1.3em;
  }
  40% {
    box-shadow: 0 2.5em 0 0;
  }
}
@keyframes load7 {
  0%,
  80%,
  100% {
    box-shadow: 0 2.5em 0 -1.3em;
  }
  40% {
    box-shadow: 0 2.5em 0 0;
  }
}
</style>